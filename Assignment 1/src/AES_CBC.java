void main() throws Exception {
    String message = askUserForMessage();
    String secretKey = askUserForSecretKey();
    String initializationVector = askUserForInitializationVector();

    // Convert Secret Key from String to Bytes.
    byte[] secretKeyBytes = secretKey.getBytes();
    SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyBytes, "AES");

    // Convert Initialization Vector from String to Bytes.
    byte[] initializationVectorBytes = initializationVector.getBytes();
    IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVectorBytes);

    // Encrypt the users inputted message.
    Cipher encryptionCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
    // Encode the bytes using Base64
    byte[] encryptedBytes = encryptionCipher.doFinal(message.getBytes());
    Base64.Encoder encoder = Base64.getEncoder();
    encoder.encode(encryptedBytes);
    System.out.println("Encrypted Message: " + encoder.encodeToString(initializationVectorBytes));

    // Decrypt the users inputted message.
    Cipher decryptionCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    AlgorithmParameters algorithmParameters = encryptionCipher.getParameters();
    decryptionCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, algorithmParameters);
    String decryptedMessage = new String(decryptionCipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);
    System.out.println("Decrypted Message: " + decryptedMessage);
}

public static String askUserForMessage() {
    // Ask the user for a message they would like to encrypt.
    Scanner scanner = new Scanner(System.in);
    IO.print("Enter message you wish to encrypt: ");
    return scanner.nextLine();
}

public static String askUserForSecretKey() {
    Scanner scanner = new Scanner(System.in);
    IO.print("Enter a 128-bit (16 Characters) Secret Key: ");
    String secretKey = scanner.nextLine();
    if (secretKey.length() != 16) {
        System.out.println("Secret Key length should be 16 characters");
        System.out.print("Enter a 128-bit (16 Characters) Secret Key: ");
        secretKey = scanner.nextLine();
    }
    return secretKey;
}

public static String askUserForInitializationVector() {
    Scanner scanner = new Scanner(System.in);
    IO.print("Enter a 128-bit (16 Characters) Initialization Vector: ");
    String initializationVector = scanner.nextLine();
    if (initializationVector.length() != 16) {
        System.out.println("Initialization Vector length should be 16 characters");
        System.out.print("Enter a 128-bit (16 Characters) Initialization Vector: ");
        initializationVector = scanner.nextLine();
    }
    return initializationVector;
}