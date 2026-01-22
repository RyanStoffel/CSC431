package Assignment_2;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.security.*;
import static java.nio.file.StandardOpenOption.*;

public class Hashing {
    void main() {
        writeTestCasesFile();

        Scanner scanner = new Scanner(System.in);
        String userInput = askUserForMessageOrTextFile(scanner);
        String hashedInput;

        if (userInput.toLowerCase().endsWith(".txt")) {
            try {
                userInput = readFile(userInput);
                System.out.print("File Content: " + userInput);
                hashedInput = hashString(userInput);
                System.out.println("Hashed Input: " + hashedInput + "\n");

                char decision = askUserToSimulateModification(scanner);
                simulateModification(userInput, decision == 'y', false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Input: " + userInput);
            hashedInput = hashString(userInput);
            System.out.println("Hashed Input: " + hashedInput  + "\n");

            char decision = askUserToSimulateModification(scanner);
            simulateModification(userInput, decision == 'y', false);
        }
    }

    // Ask the user for a message or a text file path.
    private static String askUserForMessageOrTextFile(Scanner scanner) {
        System.out.print("Enter a message or a text file path: ");
        return scanner.nextLine();
    }

    // Ask the user if they would like to simulate a modification to the input.
    private static char askUserToSimulateModification(Scanner scanner) {
        System.out.print("Would you like to simulate a modification to the input? (y/n): ");
        return scanner.next().charAt(0);
    }

    // Simulate a modification to the input and compare the hashes.
    private static String simulateModification(String input, boolean modify, boolean forTestCases) {
        String output = "";
        String hashedInput = hashString(input); // Re-Hash the input before modifying it.
        String reHashedInput = hashString(input); // Re-Hash the original input.
        String modifiedInput = input += (" - modified."); // Modify the input.
        String modifiedHashedInput = hashString(modifiedInput); // Hash the modified input.

        if (forTestCases) {
            if (modify) {
                output += "Modified Input: " + modifiedInput + "\n";
                output += "Modified Hashed Input: " + modifiedHashedInput + "\n";
                output += checkDataIntegrity(hashedInput, modifiedHashedInput) + "\n";
            } else {
                output += "Re-Hashed Input: " + reHashedInput + "\n";
                output += checkDataIntegrity(hashedInput, reHashedInput) + "\n";
            }
            return output;

        } else {
            if (modify) {
                System.out.println("Modified Input: " + modifiedInput);
                System.out.println("Modified Hashed Input: " + modifiedHashedInput);
                System.out.println(checkDataIntegrity(hashedInput, modifiedHashedInput)); // Compare the hashes and determine if the data integrity has been compromised.
            } else {
                System.out.println("Re-Hashing your input...");
                System.out.println("Re-Hashed Input: " + reHashedInput);
                System.out.println(checkDataIntegrity(hashedInput, reHashedInput)); // Compare the hashes and determine if the data integrity has been compromised.
            }
        }
        return output;
    }

    // Compare the hashes of two inputs and determine if the data integrity has been compromised.
    private static String checkDataIntegrity(String hashedInput1, String hashedInput2) {
        if (hashedInput1.equals(hashedInput2)) {
            return "Data integrity check: PASSED.";
        } else {
            return "Data integrity check: FAILED.";
        }
    }

    // Read a text file and return its content as a String.
    private static String readFile(String filePath) throws IOException {
        Path file = Path.of(filePath); // Convert the file path to a Path object.
        // Try to open the file and read its content.
        try (InputStream in = Files.newInputStream(file);
             BufferedReader reader =
                     new BufferedReader(new InputStreamReader(in))) {
            String line;
            StringBuilder content = new StringBuilder();
            // Read the file line by line and append it to the content StringBuilder.
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }

    // Hash a String using SHA-256.
    private static String hashString(String input) {
        if (input.isEmpty()) {
            return "No input provided.";
        }

        if (input.length() > 1000) {
            return "Input too long. Please provide a shorter input.";
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(input.getBytes()); // Compute the hash of the input.
            StringBuilder hexString = new StringBuilder();

            // Convert the hash bytes to a hexadecimal String.
            for (byte b : hash) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to compute hash for input: " + input, e);
        }
    }

    // Write a Test_Cases.txt file with the results of the hashing operations.
    private static void writeTestCasesFile() {
        Path path = Paths.get("Test_Cases.txt");
        String testInput1 = "This is a test input.";
        String testInput2 = "This is another test input.";
        String testInput3 = "This is yet another test input.";

        String hashedInput1 = hashString(testInput1);
        String hashedInput2 = hashString(testInput2);
        String hashedInput3 = hashString(testInput3);

        String fileContent = "";
        fileContent += "Test Input 1: " + testInput1 + "\n";
        fileContent += "Hashed Input 1: " + hashedInput1 + "\n";
        fileContent += "Modified Test Input 1: " + simulateModification(testInput1, true, true) + " \n";
        fileContent += "Test Input 2: " + testInput2 + "\n";
        fileContent += "Hashed Input 2: " + hashedInput2 + "\n";
        fileContent += simulateModification(testInput2, false, true) + " \n";
        fileContent += "Test Input 3: " + testInput3 + "\n";
        fileContent += "Hashed Input 3: " + hashedInput3 + "\n";
        fileContent += "Modified Test Input 3: " + simulateModification(testInput3, true, true) + " \n";

        byte[] data = fileContent.getBytes();
        try (OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(path, CREATE, TRUNCATE_EXISTING))) {
            out.write(data, 0, data.length);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to file: " + "Test_Cases.txt", e);
        }
    }
}
