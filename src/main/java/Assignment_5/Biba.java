package Assignment_5;

import java.util.*;

public class Biba {

    // Integrity levels: 1=Low, 2=Medium, 3=High, 4=Very High
    static String[] levelNames = {"", "Low", "Medium", "High", "Very High"};

    static Map<String, String> userNames = new LinkedHashMap<>();
    static Map<String, Integer> userLevels = new LinkedHashMap<>();
    static Map<String, String> objectNames = new LinkedHashMap<>();
    static Map<String, Integer> objectLevels = new LinkedHashMap<>();
    static List<String> log = new ArrayList<>();

    void main() {
        // Set up users
        userNames.put("U1", "Alice");   userLevels.put("U1", 4); // Very High
        userNames.put("U2", "Bob");     userLevels.put("U2", 3); // High
        userNames.put("U3", "Charlie"); userLevels.put("U3", 2); // Medium
        userNames.put("U4", "Diana");   userLevels.put("U4", 1); // Low

        // Setup objects
        objectNames.put("O1", "TopSecretDoc");       objectLevels.put("O1", 4);
        objectNames.put("O2", "ConfidentialReport");  objectLevels.put("O2", 3);
        objectNames.put("O3", "InternalMemo");        objectLevels.put("O3", 2);
        objectNames.put("O4", "PublicNotice");         objectLevels.put("O4", 1);

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Biba Integrity Model Simulator ===\n");
        runTestCases();

        // Interactive loop
        label:
        while (true) {
            System.out.println("Commands: access, users, objects, chglevel, log, test, quit");
            System.out.print("> ");
            String cmd = scanner.nextLine().trim().toLowerCase();

            switch (cmd) {
                case "quit":
                    break label;
                case "users":
                    printUsers();
                    break;
                case "objects":
                    printObjects();
                    break;
                case "log":
                    printLog();
                    break;
                case "test":
                    runTestCases();
                    break;
                case "chglevel":
                    // Extra credit: dynamic integrity level changes
                    System.out.print("User or Object ID: ");
                    String id = scanner.nextLine().trim().toUpperCase();
                    System.out.print("New level (1=Low, 2=Medium, 3=High, 4=Very High): ");
                    String lvl = scanner.nextLine().trim();
                    changeLevel(id, lvl);
                    break;
                case "access":
                    printUsers();
                    System.out.print("User ID: ");
                    String uid = scanner.nextLine().trim().toUpperCase();
                    printObjects();
                    System.out.print("Object ID: ");
                    String oid = scanner.nextLine().trim().toUpperCase();
                    System.out.print("Operation (read/write): ");
                    String op = scanner.nextLine().trim().toLowerCase();
                    System.out.println(processAccess(uid, oid, op));
                    break;
                default:
                    System.out.println("Unknown command.");
                    break;
            }
            System.out.println();
        }
        scanner.close();
    }

    // Process a read or write request using Biba rules
    static String processAccess(String userId, String objectId, String operation) {
        if (!userNames.containsKey(userId)) {
            String msg = "ERROR: User '" + userId + "' not found.";
            log.add(msg);
            return msg;
        }
        if (!objectNames.containsKey(objectId)) {
            String msg = "ERROR: Object '" + objectId + "' not found.";
            log.add(msg);
            return msg;
        }
        if (!operation.equals("read") && !operation.equals("write")) {
            String msg = "ERROR: Invalid operation '" + operation + "'. Use read or write.";
            log.add(msg);
            return msg;
        }

        int uLevel = userLevels.get(userId);
        int oLevel = objectLevels.get(objectId);
        String uName = userNames.get(userId);
        String oName = objectNames.get(objectId);
        String result;

        if (operation.equals("read")) {
            // Simple Integrity Property: no read down
            // User can only read objects at the same or higher integrity
            if (oLevel >= uLevel) {
                result = "ALLOWED: " + uName + " (" + levelNames[uLevel] + ") can read " + oName + " (" + levelNames[oLevel] + ").";
            } else {
                result = "DENIED: " + uName + " (" + levelNames[uLevel] + ") cannot read " + oName + " (" + levelNames[oLevel] + "). Violates Simple Integrity Property (No Read Down).";
            }
        } else {
            // *-Integrity Property: no writing up
            // User can only write to objects at the same or lower integrity
            if (oLevel <= uLevel) {
                result = "ALLOWED: " + uName + " (" + levelNames[uLevel] + ") can write " + oName + " (" + levelNames[oLevel] + ").";
            } else {
                result = "DENIED: " + uName + " (" + levelNames[uLevel] + ") cannot write " + oName + " (" + levelNames[oLevel] + "). Violates *-Integrity Property (No Write Up).";
            }
        }

        log.add(userId + " " + operation + " " + objectId + " -> " + result);
        return result;
    }

    // Extra credit: change the integrity level of a user or object
    static void changeLevel(String id, String levelStr) {
        int level;
        try {
            level = Integer.parseInt(levelStr);
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Enter a number 1-4.");
            return;
        }
        if (level < 1 || level > 4) {
            System.out.println("ERROR: Level must be 1-4.");
            return;
        }
        if (userLevels.containsKey(id)) {
            userLevels.put(id, level);
            System.out.println(userNames.get(id) + " integrity changed to " + levelNames[level] + ".");
            log.add(id + " integrity changed to " + levelNames[level]);
        } else if (objectLevels.containsKey(id)) {
            objectLevels.put(id, level);
            System.out.println(objectNames.get(id) + " integrity changed to " + levelNames[level] + ".");
            log.add(id + " integrity changed to " + levelNames[level]);
        } else {
            System.out.println("ERROR: ID '" + id + "' not found.");
        }
    }

    static void printUsers() {
        System.out.println("Users:");
        for (String id : userNames.keySet())
            System.out.println("  " + id + " - " + userNames.get(id) + " (" + levelNames[userLevels.get(id)] + ")");
    }

    static void printObjects() {
        System.out.println("Objects:");
        for (String id : objectNames.keySet())
            System.out.println("  " + id + " - " + objectNames.get(id) + " (" + levelNames[objectLevels.get(id)] + ")");
    }

    static void printLog() {
        System.out.println("Access Log:");
        if (log.isEmpty()) { System.out.println("  (empty)"); return; }
        for (String entry : log) System.out.println("  " + entry);
    }

    static void runTestCases() {
        System.out.println("--- Test Cases ---\n");

        System.out.println("Test 1: Alice (Very High) reads PublicNotice (Low) - should be DENIED (No Read Down)");
        System.out.println("  " + processAccess("U1", "O4", "read"));

        System.out.println("Test 2: Bob (High) reads ConfidentialReport (High) - should be ALLOWED");
        System.out.println("  " + processAccess("U2", "O2", "read"));

        System.out.println("Test 3: Diana (Low) writes TopSecretDoc (Very High) - should be DENIED (No Write Up)");
        System.out.println("  " + processAccess("U4", "O1", "write"));

        System.out.println("Test 4: Charlie (Medium) writes InternalMemo (Medium) - should be ALLOWED");
        System.out.println("  " + processAccess("U3", "O3", "write"));

        System.out.println("Test 5: Diana (Low) reads TopSecretDoc (Very High) - should be ALLOWED");
        System.out.println("  " + processAccess("U4", "O1", "read"));

        System.out.println("Test 6: Invalid user U99 - should show error");
        System.out.println("  " + processAccess("U99", "O1", "read"));

        System.out.println();
    }
}