package Assignment_1;

import java.util.Scanner;
import java.util.Arrays;
import java.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.AlgorithmParameters;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;


public class AES_CBC {

    void main() throws Exception {
        Scanner scanner = new Scanner(System.in);
        String input;

        // Simple do-while loop to allow the user to encrypt multiple messages.
        do {
            System.out.println(
                "Welcome to Ryan Stoffel's AES CBC Encryptor and Decryptor!"
            );

            String message = askUserForMessage(scanner);
            String secretKey = askUserForSecretKey(scanner);
            String iv = askUserForInitializationVector(scanner);

            byte[] encryptedBytes = encrypt(message, secretKey, iv);
            System.out.println("Encrypted Message: " + encode(encryptedBytes));

            String decryptedMessage = decrypt(encryptedBytes, secretKey);
            System.out.println("Decrypted Message: " + decryptedMessage);

            System.out.print(
                "\nType 'y' to encrypt another message or 'exit' to quit: "
            );
            input = scanner.next();
            scanner.nextLine();
        } while (input.equalsIgnoreCase("y"));

        scanner.close();
    }

    // encryptionCipher in Class Scope so it can be used by both the encrypt and decrypt methods.
    private Cipher encryptionCipher;

    // Encrypt a plain text message.
    private byte[] encrypt(String message, String secretKey, String iv)
        throws Exception {
        SecretKeySpec secretKeySpec;
        if (secretKey.length() != 16) {
            // Verify secretKey is exactly 128-bits (16 Characters) long.
            throw new Exception(
                "Secret Key must be exactly 128-bits (16 Characters)"
            );
        } else {
            // Generate secretKeySpec for AES Algorithm using the user's inputted secretKey.
            secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
        }

        IvParameterSpec ivParameterSpec;
        if (iv.length() != 16) {
            // Verify initializationVector is exactly 128-bit (16 Characters) long.
            throw new Exception(
                "Initialization Vector must be exactly 128-bits (16 Characters)"
            );
        } else {
            // Generate initializationVectorSpec using the user's inputter initializationVector.
            ivParameterSpec = new IvParameterSpec(iv.getBytes());
        }

        // Define the mode and the padding we want to use for the AES Algorithm.
        encryptionCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        encryptionCipher.init(
            Cipher.ENCRYPT_MODE,
            secretKeySpec,
            ivParameterSpec
        );
        // Encrypt the plain text message.
        return encryptionCipher.doFinal(message.getBytes());
    }

    // Decrypt an encrypted message.
    private String decrypt(byte[] encryptedBytes, String secretKey)
        throws Exception {
        SecretKeySpec secretKeySpec;
        if (secretKey.length() != 16) {
            // Verify secretKey is exactly 128-bits (16 Characters) long.
            throw new Exception(
                "Secret Key must be exactly 128-bits (16 Characters)"
            );
        } else {
            // Generate secretKeySpec for AES Algorithm using the user's inputted secretKey.
            secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
        }

        // Define the mode and the padding we want to use for the AES Algorithm.
        Cipher decryptionCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        // Generate algorithmParameters from the encryptionCipher, that's why the cipher needs to be in the class scope.
        AlgorithmParameters algorithmParameters =
            encryptionCipher.getParameters();
        decryptionCipher.init(
            Cipher.DECRYPT_MODE,
            secretKeySpec,
            algorithmParameters
        );
        // Decrypt the encrypted message.
        return new String(
            decryptionCipher.doFinal(encryptedBytes),
            StandardCharsets.UTF_8
        );
    }

    // Encode an encrypted message.
    private static String encode(byte[] input) {
        Base64.Encoder encoder = Base64.getEncoder();
        // Encode the encrypted message into a readable Base64 format.
        return encoder.encodeToString(input);
    }

    private static String askUserForMessage(Scanner scanner) {
        System.out.print("Enter message you wish to encrypt: ");
        return scanner.nextLine();
    }

    private static String askUserForSecretKey(Scanner scanner) {
        char[] input;
        do {
            System.out.print("Enter a 128-bit (16 Characters) Secret Key: ");
            input = scanner.nextLine().toCharArray();
            if (input.length != 16) {
                // Verify secretKey is exactly 128-bits (16 Characters) long.
                System.out.println(
                    "Secret Key must be exactly 128-bits (16 Characters)"
                );
                Arrays.fill(input, '\0'); // Reset the input and ask again.
            }
        } while (input.length != 16);

        // Hash the input to safely & securely store the user's inputted secretKey.
        String result = hashString(new String(input)).substring(0, 16);
        // Wipe the temporary input to make sure we are not storing the user's inputted secretKey.
        Arrays.fill(input, '\0');
        return result;
    }

    private static String askUserForInitializationVector(Scanner scanner) {
        char[] input;
        do {
            System.out.print(
                "Enter a 128-bit (16 Characters) Initialization Vector: "
            );
            input = scanner.nextLine().toCharArray();
            if (input.length != 16) {
                // Verify initializationVector is exactly 128-bit (16 Characters) long.
                System.out.println(
                    "Initialization Vector must be exactly 128-bits (16 Characters)"
                );
                Arrays.fill(input, '\0');
            }
        } while (input.length != 16);

        // Hash the input to safely & securely store the user's inputted initializationVector.
        String result = hashString(new String(input)).substring(0, 16);
        // Wipe the temporary input to make sure we are not storing the user's inputted secretKey.
        Arrays.fill(input, '\0');
        return result;
    }

    // Hash the user's input using SHA-256.
    private static String hashString(String input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
