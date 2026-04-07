package Assignment_10;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class MFA {

    private static final Map<String, UserAccount> accounts = new HashMap<>();
    private static final Scanner scanner = new Scanner(System.in);

    void main() {
        boolean running = true;

        while (running) {
            System.out.println("Multi-Factor Authentication System");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice = getIntInput();
            switch (choice) {
                case 1 -> register();
                case 2 -> login();
                case 3 -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }

        System.out.println("Exiting.");
        scanner.close();
    }

    // Prompts the user for username, password, and a second factor (OTP or security question)
    private static void register() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        if (accounts.containsKey(username)) {
            System.out.println("ERROR: Username already exists.");
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.println("Choose a second factor:");
        System.out.println("1. OTP sent to your phone number");
        System.out.println("2. Security Question");
        System.out.print("Choice: ");
        int factorChoice = getIntInput();

        UserAccount account = new UserAccount();
        account.username = username;

        byte[] salt = generateSalt();
        account.salt = salt;
        account.passwordHash = hashPassword(password, salt);

        if (factorChoice == 1) {
            account.factorType = FactorType.OTP;
            System.out.print("Enter your phone number: ");
            account.phoneNumber = scanner.nextLine().trim();
            System.out.println("Phone number registered for OTP delivery.");
        } else {
            account.factorType = FactorType.SECURITY_QUESTION;
            System.out.print("Enter your security question: ");
            account.securityQuestion = scanner.nextLine();
            System.out.print("Enter your answer: ");
            account.securityAnswer = scanner.nextLine().trim().toLowerCase();
        }

        accounts.put(username, account);
        System.out.println("Registration successful for user: " + username);
    }

    // Validates password hash and second factor, locks an account after 3 failed attempts
    private static void login() {
        System.out.print("\nEnter username: ");
        String username = scanner.nextLine().trim();

        if (!accounts.containsKey(username)) {
            System.out.println("ERROR: Username not found.");
            return;
        }

        UserAccount account = accounts.get(username);

        if (account.locked) {
            System.out.println("ERROR: Account is locked due to too many failed attempts.");
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        String inputHash = hashPassword(password, account.salt);
        if (!inputHash.equals(account.passwordHash)) {
            account.failedAttempts++;
            int remaining = 3 - account.failedAttempts;
            System.out.println("ERROR: Incorrect password. " + remaining + " attempt(s) remaining.");
            if (account.failedAttempts >= 3) {
                account.locked = true;
                System.out.println("Account has been locked.");
            }
            return;
        }

        // Password corrects, now verify the second factor
        boolean secondFactorPassed = false;

        if (account.factorType == FactorType.OTP) {
            Random random = new Random();
            int otpCode = 100000 + random.nextInt(900000);

            System.out.println("Sending OTP to " + account.phoneNumber + "...");
            System.out.println("[SIMULATED SMS] Your verification code is: " + otpCode);
            System.out.print("Enter OTP: ");
            int userOtp = getIntInput();

            if (userOtp == otpCode) {
                secondFactorPassed = true;
            } else {
                System.out.println("ERROR: Incorrect OTP.");
            }
        } else {
            System.out.println("Security Question: " + account.securityQuestion);
            System.out.print("Your answer: ");
            String answer = scanner.nextLine().trim().toLowerCase();

            if (answer.equals(account.securityAnswer)) {
                secondFactorPassed = true;
            } else {
                System.out.println("ERROR: Incorrect answer.");
            }
        }

        if (secondFactorPassed) {
            account.failedAttempts = 0;
            System.out.println("Login successful. Welcome, " + username + "!");
        } else {
            account.failedAttempts++;
            int remaining = 3 - account.failedAttempts;
            System.out.println(remaining + " attempt(s) remaining before lockout.");
            if (account.failedAttempts >= 3) {
                account.locked = true;
                System.out.println("Account has been locked.");
            }
        }
    }

    // SHA-256 hash with salt, returns Base64 encoded string
    private static String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    // Handles nextInt with leftover newline consumption
    private static int getIntInput() {
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a number: ");
            scanner.next();
        }
        int val = scanner.nextInt();
        scanner.nextLine();
        return val;
    }
}

enum FactorType {
    OTP, SECURITY_QUESTION
}

class UserAccount {
    String username;
    String passwordHash;
    byte[] salt;
    FactorType factorType;
    String phoneNumber;
    String securityQuestion;
    String securityAnswer;
    int failedAttempts = 0;
    boolean locked = false;
}