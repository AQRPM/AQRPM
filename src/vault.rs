use failure::Error;
use rmp_serde;
use sodiumoxide::crypto::{aead, pwhash};
use std::fs::File;
use std::io::prelude::*;
use std::path::Path;

#[derive(Debug, Fail)]
pub enum EncryptError {
    #[fail(display = "decryption failed")]
    DecryptError,
    #[fail(display = "hash failed")]
    HashError
}

#[derive(Deserialize, Serialize, PartialEq, Eq)]
pub struct Vault {
    #[serde(skip_serializing,skip_deserializing)]
    key: [u8; aead::KEYBYTES],

    salt: [u8; pwhash::SALTBYTES],
    entries: Vec<([u8; aead::NONCEBYTES], Vec<u8>)>
}
#[derive(Deserialize, Serialize, PartialEq, Eq)]
pub struct Entry {
    name: String,
    password: String
}

/// Hash a password with salt
pub fn pwhash(password: &[u8], salt: &pwhash::Salt) -> Result<[u8; aead::KEYBYTES], EncryptError> {
    let mut key = [0; aead::KEYBYTES];
    pwhash::derive_key(&mut key, password, salt, pwhash::OPSLIMIT_INTERACTIVE, pwhash::MEMLIMIT_SENSITIVE)
        .map_err(|_| EncryptError::HashError)?;
    Ok(key)
}

impl Vault {
    /// Create a new vault
    pub fn new(password: &[u8]) -> Result<Self, Error> {
        let salt = pwhash::gen_salt();

        Ok(Vault {
            key: pwhash(password, &salt)?,

            salt: salt.0,
            entries: Vec::new()
        })
    }
    /// Open an existing vault from a file
    pub fn open<P: AsRef<Path>>(path: P, password: &[u8]) -> Result<Self, Error> {
        Self::open_stream(File::open(path)?, password)
    }
    /// Open an existing vault from a reader
    pub fn open_stream<R: Read>(mut stream: R, password: &[u8]) -> Result<Self, Error> {
        let mut data = Vec::new();
        stream.read_to_end(&mut data)?;

        let mut vault: Self = rmp_serde::from_slice(&data)?;
        vault.key = pwhash(password, &pwhash::Salt(vault.salt))?;
        Ok(vault)
    }
    /// Write vault to a file
    pub fn write<P: AsRef<Path>>(&self, path: P) -> Result<(), Error> {
        let file = File::create(path)?;
        self.write_stream(file)
    }
    /// Write vault to a writer
    pub fn write_stream<W: Write>(&self, mut stream: W) -> Result<(), Error> {
        let data = rmp_serde::to_vec(&self)?;
        stream.write_all(&data).map_err(Error::from)
    }

    /// Generates and applies a new salt.
    /// Warning: None of the entries are re-encrypted.
    /// You are encouraged to do so with `entry_recrypt`.
    pub fn gen_salt(&mut self) -> pwhash::Salt {
        let salt = pwhash::gen_salt();
        self.salt = salt.0;
        salt
    }

    /// Get the entry length.
    pub fn entry_len(&self) -> usize {
        self.entries.len()
    }
    /// Decrypt entry. Please drop the output of this function ASAP.
    /// Never keep passwords in memory.
    pub fn entry_decrypt(&self, index: usize) -> Result<Entry, Error> {
        let entry = &self.entries[index];

        let key   = aead::Key::from_slice(&self.key).unwrap();
        let nonce = aead::Nonce(entry.0);
        let data  = &entry.1;

        let plaintext = aead::open(&data, None, &nonce, &key).map_err(|_| EncryptError::DecryptError)?;
        rmp_serde::from_slice(&plaintext).map_err(Error::from)
    }
    /// Encrypt an entry with a specific key (which should be generated by `pwhash`).
    /// Also see `entry_recrypt`.
    pub fn entry_encrypt_with_key(&self, entry: &Entry, key: &[u8]) -> Result<([u8; aead::NONCEBYTES], Vec<u8>), Error> {
        let key   = aead::Key::from_slice(&key).unwrap();
        let nonce = aead::gen_nonce();
        let data  = rmp_serde::to_vec(entry)?;

        Ok((nonce.0, aead::seal(&data, None, &nonce, &key)))
    }
    /// Encrypt an entry with the default key. Also see `entry_write`.
    pub fn entry_encrypt(&self, entry: &Entry) -> Result<([u8; aead::NONCEBYTES], Vec<u8>), Error> {
        self.entry_encrypt_with_key(entry, &self.key)
    }
    /// Save an entry to the vault
    pub fn entry_write(&mut self, index: usize, entry: &Entry) -> Result<(), Error> {
        self.entries[index] = self.entry_encrypt(entry)?;
        Ok(())
    }
    /// Push a new entry to the vault
    pub fn entry_push(&mut self, entry: &Entry) -> Result<(), Error> {
        let entry = self.entry_encrypt(entry)?;
        self.entries.push(entry);
        Ok(())
    }
    /// Change the entry's key. Useful for changing master password.
    pub fn entry_recrypt(&mut self, index: usize, key: &[u8]) -> Result<(), Error> {
        let entry = self.entry_decrypt(index)?;
        self.entries[index] = self.entry_encrypt_with_key(&entry, key)?;
        Ok(())
    }
}

#[cfg(test)]
#[test]
fn test() {
    println!();

    let file = "test.aqrpm";

    let entry = Entry {
        name: String::from("yee"),
        password: String::from("woot")
    };

    // Create entry

    let mut vault = Vault::new(b"password").unwrap();
    vault.entry_push(&entry).unwrap();
    vault.write(file).unwrap();

    // Read entry

    let mut vault = Vault::open(file, b"password").unwrap();
    let entry2 = vault.entry_decrypt(0).unwrap();

    assert!(entry == entry2); // assert_eq! requires Debug

    // Change master password

    let salt = vault.gen_salt();
    let key  = pwhash(b"new_password", &salt).unwrap();

    for entry in 0..vault.entry_len() {
        vault.entry_recrypt(entry, &key).unwrap();
    }

    vault.write(file).unwrap();

    // Read entry again

    let vault = Vault::open(file, b"new_password").unwrap();
    let entry3 = vault.entry_decrypt(0).unwrap();

    assert!(entry == entry3); // assert_eq! requires Debug
}
