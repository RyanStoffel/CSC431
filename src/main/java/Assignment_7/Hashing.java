package Assignment_7;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

public class Hashing {
    void main() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("How many messages do you want to hash? ");
        int count;
        try {
            count = Integer.parseInt(scanner.nextLine().trim());
            if (count <= 0) {
                System.out.println("Error: Number of messages must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid number entered.");
            return;
        }

        List<String> inputs = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            System.out.print("Enter message " + i + ": ");
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                System.out.println("Warning: Message " + i + " is empty. Hashing empty string.");
            }
            inputs.add(input);
        }

        Map<String, List<String>> md5Map = new HashMap<>();
        Map<String, List<String>> sha1Map = new HashMap<>();
        Map<String, List<String>> sha256Map = new HashMap<>();

        for (String input : inputs) {
            String md5 = computeHash(input, "MD5");
            String sha1 = computeHash(input, "SHA-1");
            String sha256 = computeHash(input, "SHA-256");

            System.out.println("\nInput: \"" + input + "\"");
            System.out.println("MD5:     " + md5);
            System.out.println("SHA-1:   " + sha1);
            System.out.println("SHA-256: " + sha256);

            md5Map.computeIfAbsent(md5, k -> new ArrayList<>()).add(input);
            sha1Map.computeIfAbsent(sha1, k -> new ArrayList<>()).add(input);
            sha256Map.computeIfAbsent(sha256, k -> new ArrayList<>()).add(input);
        }

        System.out.print("\nSimulate a collision? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("yes") || response.equals("y")) {
            simulateCollision(md5Map, sha1Map, sha256Map);
        }

        System.out.println();

        reportCollisions("MD5", md5Map);
        reportCollisions("SHA-1", sha1Map);
        reportCollisions("SHA-256", sha256Map);

        System.out.println();

        printAlgorithmComparison();
    }

    private static String computeHash(String input, String algorithm) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            byte[] hash = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
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

    private static void reportCollisions(String algorithm, Map<String, List<String>> hashMap) {
        boolean found = false;
        for (Map.Entry<String, List<String>> entry : hashMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                System.out.println(algorithm + " COLLISION DETECTED:");
                System.out.println("  Hash: " + entry.getKey());
                System.out.println("  Inputs: " + entry.getValue());
                found = true;
            }
        }
        if (!found) {
            System.out.println(algorithm + ": No collisions detected.");
        }
    }

    private static void simulateCollision(Map<String, List<String>> md5Map,
                                          Map<String, List<String>> sha1Map,
                                          Map<String, List<String>> sha256Map) {
        String md5Key = md5Map.keySet().iterator().next();
        String sha1Key = sha1Map.keySet().iterator().next();
        String sha256Key = sha256Map.keySet().iterator().next();

        md5Map.get(md5Key).add("SIMULATED_COLLISION");
        sha1Map.get(sha1Key).add("SIMULATED_COLLISION");
        sha256Map.get(sha256Key).add("SIMULATED_COLLISION");

        System.out.println("\nCollision simulated.");
    }

    private static void printAlgorithmComparison() {
        System.out.println("Algorithm Comparison:");
        System.out.println("MD5:     Fast, 128-bit output. Cryptographically broken; vulnerable to collision attacks. Not suitable for security-sensitive use.");
        System.out.println("SHA-1:   160-bit output. Deprecated for most security uses due to demonstrated collision vulnerabilities.");
        System.out.println("SHA-256: 256-bit output. Part of the SHA-2 family. Currently considered secure and suitable for cryptographic use.");
        System.out.println();
    }
}
