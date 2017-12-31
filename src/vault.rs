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

#[derive(Deserialize, Serialize, PartialEq, Eq, Debug)]
pub struct Vault {
    #[serde(skip_serializing,skip_deserializing)]
    key: [u8; aead::KEYBYTES],

    salt: [u8; pwhash::SALTBYTES],
    entries: Vec<([u8; aead::NONCEBYTES], Vec<u8>)>
}
#[derive(Deserialize, Serialize, PartialEq, Eq, Debug)]
pub struct Entry {
    name: String,
    password: String
}
impl Vault {
    pub fn pwhash(password: &[u8], salt: &pwhash::Salt) -> Result<[u8; aead::KEYBYTES], EncryptError> {
        let mut key = [0; aead::KEYBYTES];
        pwhash::derive_key(&mut key, password, salt, pwhash::OPSLIMIT_INTERACTIVE, pwhash::MEMLIMIT_SENSITIVE)
            .map_err(|_| EncryptError::HashError)?;
        Ok(key)
    }
    pub fn new(password: &[u8]) -> Result<Self, Error> {
        let salt = pwhash::gen_salt();

        Ok(Vault {
            key: Self::pwhash(password, &salt)?,

            salt: salt.0,
            entries: Vec::new()
        })
    }
    pub fn open<P: AsRef<Path>>(path: P, password: &[u8]) -> Result<Self, Error> {
        Self::open_stream(File::open(path)?, password)
    }
    pub fn open_stream<R: Read>(mut stream: R, password: &[u8]) -> Result<Self, Error> {
        let mut data = Vec::new();
        stream.read_to_end(&mut data)?;

        let mut vault: Self = rmp_serde::from_slice(&data)?;
        vault.key = Self::pwhash(password, &pwhash::Salt(vault.salt))?;
        Ok(vault)
    }
    pub fn write<P: AsRef<Path>>(&self, path: P) -> Result<(), Error> {
        let file = File::create(path)?;
        self.write_stream(file)
    }
    fn write_stream<W: Write>(&self, mut stream: W) -> Result<(), Error> {
        let data = rmp_serde::to_vec(&self)?;
        stream.write_all(&data).map_err(Error::from)
    }

    pub fn entry_read(&self, entry: usize) -> Result<Entry, Error> {
        let entry = &self.entries[entry];

        let key   = aead::Key::from_slice(&self.key).unwrap();
        let nonce = aead::Nonce(entry.0);
        let data  = &entry.1;

        let plaintext = aead::open(&data, None, &nonce, &key).map_err(|_| EncryptError::DecryptError)?;
        rmp_serde::from_slice(&plaintext).map_err(Error::from)
    }
    pub fn entry_encrypt(&self, entry: &Entry) -> Result<([u8; aead::NONCEBYTES], Vec<u8>), Error> {
        let key   = aead::Key::from_slice(&self.key).unwrap();
        let nonce = aead::gen_nonce();
        let data  = rmp_serde::to_vec(entry)?;

        Ok((nonce.0, aead::seal(&data, None, &nonce, &key)))
    }
    pub fn entry_write(&mut self, entry: usize, value: &Entry) -> Result<(), Error> {
        self.entries[entry] = self.entry_encrypt(value)?;
        Ok(())
    }
    pub fn entry_push(&mut self, entry: &Entry) -> Result<(), Error> {
        let entry = self.entry_encrypt(entry)?;
        self.entries.push(entry);
        Ok(())
    }
}

#[cfg(test)]
#[test]
fn test() {
    println!();

    let entry = Entry {
        name: String::from("yee"),
        password: String::from("woot")
    };

    let mut vault = Vault::new(b"password").unwrap();
    println!("Key 1: {:?}", vault.key);
    vault.entry_push(&entry).unwrap();
    vault.write("test.aqrpm").unwrap();

    let vault = Vault::open("test.aqrpm", b"password").unwrap();
    println!("Key 2: {:?}", vault.key);
    let entry2 = vault.entry_read(0).unwrap();
    assert_eq!(entry, entry2);
}
