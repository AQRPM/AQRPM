use failure::Error;
use rmp_serde;
use sodiumoxide::crypto::{aead, pwhash};
use std::fs::File;
use std::io::prelude::*;
use std::path::Path;

#[derive(Debug, Fail)]
enum EncryptError {
    #[fail(display = "decryption failed")]
    DecryptError,
    #[fail(display = "hash failed")]
    HashError
}

#[derive(Deserialize, Serialize)]
struct Vault {
    #[serde(skip_serializing,skip_deserializing)]
    key: Vec<u8>,

    entries: Vec<([u8; aead::NONCEBYTES], Vec<u8>)>
}
#[derive(Deserialize, Serialize)]
struct Entry {
    name: String,
    password: String
}
impl Vault {
    fn pwhash(password: &[u8]) -> Result<Vec<u8>, EncryptError> {
        pwhash::pwhash(password, pwhash::OPSLIMIT_INTERACTIVE, pwhash::MEMLIMIT_SENSITIVE)
            .map(|inner| inner.0.to_vec())
            .map_err(|_| EncryptError::HashError)
    }
    fn new(password: &[u8]) -> Result<Self, Error> {
        Ok(Vault {
            key: Self::pwhash(password)?,
            entries: Vec::new()
        })
    }
    fn open<P: AsRef<Path>>(path: P, password: &[u8]) -> Result<Self, Error> {
        Self::open_stream(File::open(path)?, password)
    }
    fn open_stream<R: Read>(mut stream: R, password: &[u8]) -> Result<Self, Error> {
        let mut data = Vec::new();
        stream.read_to_end(&mut data)?;

        let mut vault: Self = rmp_serde::from_slice(&data)?;
        vault.key = Self::pwhash(password)?;
        Ok(vault)
    }
    fn write<P: AsRef<Path>>(&self, path: P) -> Result<(), Error> {
        self.write_stream(File::create(path)?)
    }
    fn write_stream<W: Write>(&self, mut stream: W) -> Result<(), Error> {
        let data = rmp_serde::to_vec(&self)?;
        stream.write_all(&data).map_err(Error::from)
    }

    fn entry_read(&self, entry: usize) -> Result<Vec<u8>, Error> {
        let entry = &self.entries[entry];

        let key   = aead::Key::from_slice(&self.key).unwrap();
        let nonce = aead::Nonce(entry.0);
        let data  = &entry.1;

        let plaintext = aead::open(&data, None, &nonce, &key).map_err(|_| EncryptError::DecryptError)?;
        rmp_serde::from_slice(&plaintext).map_err(Error::from)
    }
    fn entry_write(&mut self, entry: usize, value: Entry) -> Result<(), Error> {
        let entry = &mut self.entries[entry];

        let key   = aead::Key::from_slice(&self.key).unwrap();
        let nonce = aead::gen_nonce();
        let data  = rmp_serde::to_vec(&value)?;

        entry.1 = aead::seal(&data, None, &nonce, &key);
        entry.0 = nonce.0;
        Ok(())
    }
}
