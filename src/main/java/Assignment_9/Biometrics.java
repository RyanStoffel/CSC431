package Assignment_9;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Biometrics {

    // In-memory database mapping usernames to their stored User records.
    private static final Map<String, User> userDatabase = new HashMap<>();

    // Maximum Levenshtein distance allowed for fuzzy biometric matching.
    private static final int MATCH_TOLERANCE = 2;

    void main() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=== Biometric Authentication System ===");

        // Main menu loop for enrollment, authentication, or exit.
        while (running) {
            System.out.println("\n1. Enroll User");
            System.out.println("2. Authenticate User");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> enrollUser(scanner);
                case "2" -> authenticateUser(scanner);
                case "3" -> {
                    System.out.println("Exiting. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Error: Invalid option. Please enter 1, 2, or 3.");
            }
        }

        scanner.close();
    }

    private static void enrollUser(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        // Validate that the username is not empty.
        if (username.isEmpty()) {
            System.out.println("Error: Username cannot be empty.");
            return;
        }

        // Check if the username is already enrolled.
        if (userDatabase.containsKey(username)) {
            System.out.println("Error: Username '" + username + "' is already enrolled.");
            return;
        }

        System.out.print("Enter biometric data (simulated fingerprint pattern): ");
        String biometricData = scanner.nextLine().trim();

        // Validate that biometric data is not empty.
        if (biometricData.isEmpty()) {
            System.out.println("Error: Biometric data cannot be empty.");
            return;
        }

        // Create a new User with salted+hashed biometric data and store it.
        User user = new User(username, biometricData);
        userDatabase.put(username, user);
        System.out.println("User '" + username + "' enrolled successfully.");
    }

    private static void authenticateUser(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        // Validate that the username is not empty.
        if (username.isEmpty()) {
            System.out.println("Error: Username cannot be empty.");
            return;
        }

        // Check if the user exists in the database.
        if (!userDatabase.containsKey(username)) {
            System.out.println("Authentication failed: No account found for '" + username + "'.");
            return;
        }

        System.out.print("Enter biometric data: ");
        String biometricData = scanner.nextLine().trim();

        // Validate that biometric data is not empty.
        if (biometricData.isEmpty()) {
            System.out.println("Error: Biometric data cannot be empty.");
            return;
        }

        User user = userDatabase.get(username);

        // Use fuzzy matching to authenticate the user.
        if (user.authenticate(biometricData)) {
            System.out.println("Authentication successful! Welcome, " + username + ".");
        } else {
            System.out.println("Authentication failed: Biometric data does not match.");
        }
    }

    static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }


    static class User {
        private final String username;
        private final byte[] salt;
        private final String hashedBiometricData;
        private final String originalBiometricData; // stored only for fuzzy matching comparison

        /**
         * Creates a new User with salted and hashed biometric data.
         */
        public User(String username, String biometricData) {
            this.username = username;
            this.salt = generateSalt();
            this.hashedBiometricData = hashWithSalt(biometricData, this.salt);
            this.originalBiometricData = biometricData;
        }

        public String getUsername() {
            return username;
        }

        public boolean authenticate(String inputData) {
            // First try exact match via hash comparison.
            String inputHash = hashWithSalt(inputData, this.salt);
            if (this.hashedBiometricData.equals(inputHash)) {
                return true;
            }

            // Fall back to fuzzy matching for minor biometric variations.
            int distance = levenshteinDistance(inputData, this.originalBiometricData);
            return distance <= MATCH_TOLERANCE;
        }


        private static byte[] generateSalt() {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            return salt;
        }


        private static String hashWithSalt(String input, byte[] salt) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(salt);
                byte[] hash = digest.digest(input.getBytes());
                StringBuilder hexString = new StringBuilder();

                // Convert hash bytes to hexadecimal representation.
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xFF & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }

                return hexString.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-256 algorithm not available.", e);
            }
        }
    }
}
