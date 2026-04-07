package Assignment_8;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Passwords {

    private static final Map<String, String[]> accounts = new HashMap<>();
    private static final Map<String, String> securityQuestions = new HashMap<>();
    private static final Scanner scanner = new Scanner(System.in);

    void main() {
        while (true) {
            System.out.println("\n--- Password Management System ---");
            System.out.println("1. Create Account");
            System.out.println("2. Login");
            System.out.println("3. Reset Password");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": createAccount(); break;
                case "2": login(); break;
                case "3": resetPassword(); break;
                case "4":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Error: Invalid option. Please enter 1-4.");
            }
        }
    }

    private static void createAccount() {
        System.out.print("Enter a username: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Error: Username cannot be empty.");
            return;
        }

        if (accounts.containsKey(username)) {
            System.out.println("Error: Account already exists.");
            return;
        }

        System.out.print("Enter a password: ");
        String password = scanner.nextLine();

        if (password.isEmpty()) {
            System.out.println("Error: Password cannot be empty.");
            return;
        }

        System.out.print("Enter a security question (for password reset): ");
        String question = scanner.nextLine().trim();

        System.out.print("Enter the answer to your security question: ");
        String answer = scanner.nextLine().trim();

        if (question.isEmpty() || answer.isEmpty()) {
            System.out.println("Error: Security question and answer cannot be empty.");
            return;
        }

        byte[] salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        String saltString = Base64.getEncoder().encodeToString(salt);

        accounts.put(username, new String[]{hashedPassword, saltString});
        securityQuestions.put(username, question + "|" + answer.toLowerCase());

        System.out.println("Account created successfully for '" + username + "'.");
    }

    private static void login() {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine().trim();

        if (!accounts.containsKey(username)) {
            System.out.println("Error: Account does not exist.");
            return;
        }

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        String[] stored = accounts.get(username);
        String storedHash = stored[0];
        byte[] salt = Base64.getDecoder().decode(stored[1]);

        String attemptHash = hashPassword(password, salt);

        if (storedHash.equals(attemptHash)) {
            System.out.println("Login successful! Welcome, " + username + ".");
        } else {
            System.out.println("Error: Incorrect password.");
        }
    }

    private static void resetPassword() {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine().trim();

        if (!accounts.containsKey(username)) {
            System.out.println("Error: Account does not exist.");
            return;
        }

        String[] qaPair = securityQuestions.get(username).split("\\|", 2);
        System.out.println("Security Question: " + qaPair[0]);
        System.out.print("Your answer: ");
        String answer = scanner.nextLine().trim();

        if (!answer.toLowerCase().equals(qaPair[1])) {
            System.out.println("Error: Incorrect answer. Password reset denied.");
            return;
        }

        System.out.print("Enter your new password: ");
        String newPassword = scanner.nextLine();

        if (newPassword.isEmpty()) {
            System.out.println("Error: Password cannot be empty.");
            return;
        }

        byte[] salt = generateSalt();
        String hashedPassword = hashPassword(newPassword, salt);
        String saltString = Base64.getEncoder().encodeToString(salt);

        accounts.put(username, new String[]{hashedPassword, saltString});

        System.out.println("Password reset successful for '" + username + "'.");
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private static String hashPassword(String password, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed: " + e.getMessage());
        }
    }
}