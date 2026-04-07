## Biometric Authentication System

Simulates a biometric authentication system in Java. Users enroll with a username and simulated biometric data (a string pattern). The data is hashed with a unique salt using SHA-256 before storage. Authentication supports fuzzy matching via Levenshtein distance, allowing up to 2 characters of variation.

### How to Run

```
java Biometrics.java
```

No external dependencies required. Java 21+ is needed.
