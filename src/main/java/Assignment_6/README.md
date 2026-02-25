# RSA Encryption Program

A Java program that demonstrates asymmetric encryption using the RSA algorithm. It generates a 2048-bit RSA key pair, encrypts messages with the public key, and decrypts them with the private key. Includes optional features for saving/loading keys to files and digital signatures.

## Features

- 2048-bit RSA key pair generation
- Encrypt plaintext messages (displayed in Base64)
- Decrypt ciphertext back to plaintext
- Digital signatures (sign and verify messages)
- Save and load keys to/from files
- Input validation and error handling

## Dependencies

- Java 8 or higher (JDK)
- No external libraries required (uses `java.security` and `javax.crypto`)

## How to Run
Run:

```
java RSAEncryption.java
```

The program presents a menu where you can encrypt messages, decrypt ciphertext, sign messages, verify signatures, generate new keys, or save keys to files.

## Notes

- RSA with 2048-bit keys can encrypt messages up to 245 bytes.
- Saved keys are stored in a `keys/` directory as `public.key` and `private.key`.
- The program automatically loads saved keys on startup if they exist.