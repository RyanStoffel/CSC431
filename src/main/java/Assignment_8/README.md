# Secure Password Management System

## Overview

A command-line Java application that allows users to create accounts with securely hashed passwords, log in, and reset passwords via security questions. Passwords are hashed using PBKDF2WithHmacSHA256 with a unique random salt per account.

## How to Run

```
java Passwords.java
```

## How to Use

The program presents a menu with four options:

1. **Create Account** - Enter a username, password, security question, and answer. The password is hashed with a random salt and stored in memory.

2. **Login** - Enter your username and password. The system hashes your input with the stored salt and compares it against the stored hash.

3. **Reset Password** - Enter your username and answer your security question. If correct, you can set a new password.

4. **Exit** - Closes the program.

## Dependencies

None. Uses only the Java standard library (`javax.crypto` and `java.security` packages).

## Security Details

- **Algorithm**: PBKDF2WithHmacSHA256
- **Iterations**: 65,536
- **Key Length**: 256 bits
- **Salt**: 16 bytes, randomly generated per account using SecureRandom

## Files

- `Passwords.java` - Main source code
- `Test_Cases.txt` - Three test cases demonstrating successful login, failed login, and password reset