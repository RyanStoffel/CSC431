# Hashing

A Java program that computes and compares cryptographic hash values for user-provided inputs using MD5, SHA-1, and SHA-256.

## Description

The program accepts one or more plaintext strings and displays their hash values in hexadecimal format. It tracks all inputs and their hashes to detect collisions, and includes an option to simulate a collision for demonstration purposes. An algorithm comparison is printed at startup summarizing the strengths and weaknesses of each hashing algorithm.

## How to Run

```bash
java Hashing.java
```

## Features

- Hashes input strings using MD5, SHA-1, and SHA-256
- Detects and reports collisions across multiple inputs
- Option to simulate a collision
- Handles empty input with a warning
- Prints a comparison of algorithm strengths and weaknesses