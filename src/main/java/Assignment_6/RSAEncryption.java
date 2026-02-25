package Assignment_6;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import javax.crypto.Cipher;
import java.util.Base64;
import java.util.Scanner;
import java.io.*;
import java.nio.file.*;

public class RSAEncryption {

    private static final int KEY_SIZE = 2048;
    private static final String ALGORITHM = "RSA";
    private static final String KEY_DIR = "keys";
    private static final String PUBLIC_KEY_FILE = KEY_DIR + "/public.key";
    private static final String PRIVATE_KEY_FILE = KEY_DIR + "/private.key";

    private PrivateKey privateKey;
    private PublicKey publicKey;

    // Generate a new 2048-bit RSA key pair
    public void generateKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
        generator.initialize(KEY_SIZE);
        KeyPair pair = generator.generateKeyPair();
        this.publicKey = pair.getPublic();
        this.privateKey = pair.getPrivate();
    }

    // Encrypt plaintext using the public key
    public String encrypt(String plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // Decrypt ciphertext using the private key
    public String decrypt(String ciphertext) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    // Sign a message using the private key
    public String sign(String message) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes(StandardCharsets.UTF_8));
        byte[] signed = signature.sign();
        return Base64.getEncoder().encodeToString(signed);
    }

    // Verify a signature using the public key
    public boolean verify(String message, String signatureStr) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(message.getBytes(StandardCharsets.UTF_8));
        return signature.verify(Base64.getDecoder().decode(signatureStr));
    }

    // Save keys to files
    public void saveKeys() throws IOException {
        Files.createDirectories(Paths.get(KEY_DIR));
        Files.write(Paths.get(PUBLIC_KEY_FILE), publicKey.getEncoded());
        Files.write(Paths.get(PRIVATE_KEY_FILE), privateKey.getEncoded());
        System.out.println("Keys saved to " + KEY_DIR + "/ directory.");
    }

    // Load keys from files
    public boolean loadKeys() {
        try {
            byte[] publicBytes = Files.readAllBytes(Paths.get(PUBLIC_KEY_FILE));
            byte[] privateBytes = Files.readAllBytes(Paths.get(PRIVATE_KEY_FILE));

            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicBytes));
            this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));

            System.out.println("Keys loaded from " + KEY_DIR + "/ directory.");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // RSA can only encrypt data up to (key size / 8) - 11 bytes with PKCS1 padding
    private static int getMaxPlaintextSize() {
        return (KEY_SIZE / 8) - 11;
    }

    void main() {
        RSAEncryption rsa = new RSAEncryption();

        try (Scanner scanner = new Scanner(System.in)) {
            // Try loading existing keys, generate new ones if not found
            if (!rsa.loadKeys()) {
                System.out.println("No saved keys found. Generating new 2048-bit RSA key pair...");
                rsa.generateKeys();
                System.out.println("Keys generated successfully.\n");
            }

            boolean running = true;
            while (running) {
                System.out.println("--- RSA Encryption Menu ---");
                System.out.println("1. Encrypt a message");
                System.out.println("2. Decrypt a message");
                System.out.println("3. Sign a message");
                System.out.println("4. Verify a signature");
                System.out.println("5. Generate new keys");
                System.out.println("6. Save keys to file");
                System.out.println("7. Exit");
                System.out.print("Choice: ");

                String choice = scanner.nextLine().trim();
                System.out.println();

                switch (choice) {
                    case "1":
                        System.out.print("Enter plaintext message: ");
                        String plaintext = scanner.nextLine();

                        if (plaintext.isEmpty()) {
                            System.out.println("Error: Message cannot be empty.\n");
                            break;
                        }
                        if (plaintext.getBytes(StandardCharsets.UTF_8).length > getMaxPlaintextSize()) {
                            System.out.println("Error: Message too large. Max size is "
                                    + getMaxPlaintextSize() + " bytes for 2048-bit RSA.\n");
                            break;
                        }

                        String encrypted = rsa.encrypt(plaintext);
                        System.out.println("Encrypted (Base64): " + encrypted);
                        System.out.println();
                        break;

                    case "2":
                        System.out.print("Enter Base64 ciphertext: ");
                        String ciphertext = scanner.nextLine().trim();

                        if (ciphertext.isEmpty()) {
                            System.out.println("Error: Ciphertext cannot be empty.\n");
                            break;
                        }

                        try {
                            String decrypted = rsa.decrypt(ciphertext);
                            System.out.println("Decrypted: " + decrypted);
                        } catch (Exception e) {
                            System.out.println("Error: Decryption failed. Invalid ciphertext or wrong key.");
                        }
                        System.out.println();
                        break;

                    case "3":
                        System.out.print("Enter message to sign: ");
                        String msgToSign = scanner.nextLine();

                        if (msgToSign.isEmpty()) {
                            System.out.println("Error: Message cannot be empty.\n");
                            break;
                        }

                        String sig = rsa.sign(msgToSign);
                        System.out.println("Signature (Base64): " + sig);
                        System.out.println();
                        break;

                    case "4":
                        System.out.print("Enter original message: ");
                        String origMsg = scanner.nextLine();
                        System.out.print("Enter signature (Base64): ");
                        String sigToVerify = scanner.nextLine().trim();

                        try {
                            boolean valid = rsa.verify(origMsg, sigToVerify);
                            System.out.println("Signature valid: " + valid);
                        } catch (Exception e) {
                            System.out.println("Error: Verification failed. Invalid signature format.");
                        }
                        System.out.println();
                        break;

                    case "5":
                        System.out.println("Generating new 2048-bit RSA key pair...");
                        rsa.generateKeys();
                        System.out.println("New keys generated.\n");
                        break;

                    case "6":
                        rsa.saveKeys();
                        System.out.println();
                        break;

                    case "7":
                        running = false;
                        System.out.println("Exiting.");
                        break;

                    default:
                        System.out.println("Invalid choice.\n");
                }
            }
        } catch (Exception e) {
            System.out.println("Fatal error: " + e.getMessage());
        }
    }
}