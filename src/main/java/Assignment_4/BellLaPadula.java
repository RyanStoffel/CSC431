package Assignment_4;

import java.util.*;

public class BellLaPadula {

    // Security levels ordered from lowest to highest
    enum SecurityLevel {
        UNCLASSIFIED(0),
        CONFIDENTIAL(1),
        SECRET(2),
        TOP_SECRET(3);

        private final int rank;

        SecurityLevel(int rank) {
            this.rank = rank;
        }

        public int getRank() {
            return rank;
        }

        public static SecurityLevel fromString(String s) {
            return switch (s.toUpperCase().replace(" ", "_")) {
                case "UNCLASSIFIED" -> UNCLASSIFIED;
                case "CONFIDENTIAL" -> CONFIDENTIAL;
                case "SECRET" -> SECRET;
                case "TOP_SECRET" -> TOP_SECRET;
                default -> null;
            };
        }
    }

    static class User {
        String id;
        String name;
        SecurityLevel level;

        User(String id, String name, SecurityLevel level) {
            this.id = id;
            this.name = name;
            this.level = level;
        }
    }

    static class SecureObject {
        String id;
        String name;
        SecurityLevel level;
        String content;

        SecureObject(String id, String name, SecurityLevel level, String content) {
            this.id = id;
            this.name = name;
            this.level = level;
            this.content = content;
        }
    }

    // Access log for tracking all requests (extra credit)
    static class AccessLog {
        String timestamp;
        String userId;
        String objectId;
        String operation;
        boolean permitted;
        String reason;

        AccessLog(String userId, String objectId, String operation, boolean permitted, String reason) {
            this.timestamp = new Date().toString();
            this.userId = userId;
            this.objectId = objectId;
            this.operation = operation;
            this.permitted = permitted;
            this.reason = reason;
        }

        @Override
        public String toString() {
            return String.format("[%s] User: %s | Object: %s | Op: %s | %s | %s",
                    timestamp, userId, objectId, operation,
                    permitted ? "ALLOWED" : "DENIED", reason);
        }
    }

    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, SecureObject> objects = new LinkedHashMap<>();
    private final List<AccessLog> accessLogs = new ArrayList<>();

    public void addUser(String id, String name, SecurityLevel level) {
        users.put(id, new User(id, name, level));
    }

    public void addObject(String id, String name, SecurityLevel level, String content) {
        objects.put(id, new SecureObject(id, name, level, content));
    }

    // Dynamic role change (extra credit): update a user's security level
    public boolean changeUserLevel(String userId, SecurityLevel newLevel) {
        User user = users.get(userId);
        if (user == null) return false;
        SecurityLevel oldLevel = user.level;
        user.level = newLevel;
        accessLogs.add(new AccessLog(userId, "N/A", "LEVEL_CHANGE",
                true, "Changed from " + oldLevel + " to " + newLevel));
        return true;
    }

    // Dynamic role change (extra credit): update an object's security level
    public boolean changeObjectLevel(String objectId, SecurityLevel newLevel) {
        SecureObject obj = objects.get(objectId);
        if (obj == null) return false;
        SecurityLevel oldLevel = obj.level;
        obj.level = newLevel;
        accessLogs.add(new AccessLog("SYSTEM", objectId, "LEVEL_CHANGE",
                true, "Object changed from " + oldLevel + " to " + newLevel));
        return true;
    }

    // Simple Security Property: no read up
    // A user can read an object only if user's level >= object's level
    public String attemptRead(String userId, String objectId) {
        User user = users.get(userId);
        SecureObject obj = objects.get(objectId);

        if (user == null) {
            String msg = "Error: User '" + userId + "' does not exist.";
            accessLogs.add(new AccessLog(userId, objectId, "READ", false, msg));
            return msg;
        }
        if (obj == null) {
            String msg = "Error: Object '" + objectId + "' does not exist.";
            accessLogs.add(new AccessLog(userId, objectId, "READ", false, msg));
            return msg;
        }

        String msg;
        if (user.level.getRank() >= obj.level.getRank()) {
            msg = String.format("ACCESS GRANTED: User '%s' (%s) can read object '%s' (%s).\n  Content: \"%s\"",
                    user.name, user.level, obj.name, obj.level, obj.content);
            accessLogs.add(new AccessLog(userId, objectId, "READ", true,
                    "User level " + user.level + " >= Object level " + obj.level));
        } else {
            msg = String.format("ACCESS DENIED: User '%s' (%s) cannot read object '%s' (%s).\n  Reason: Simple Security Property violated - user's clearance is below the object's classification.",
                    user.name, user.level, obj.name, obj.level);
            accessLogs.add(new AccessLog(userId, objectId, "READ", false,
                    "Simple Security Property: " + user.level + " < " + obj.level));
        }
        return msg;
    }

    // Star Property: no writing down
    // A user can write to an object only if object's level >= user's level
    public String attemptWrite(String userId, String objectId, String newContent) {
        User user = users.get(userId);
        SecureObject obj = objects.get(objectId);

        if (user == null) {
            String msg = "Error: User '" + userId + "' does not exist.";
            accessLogs.add(new AccessLog(userId, objectId, "WRITE", false, msg));
            return msg;
        }
        if (obj == null) {
            String msg = "Error: Object '" + objectId + "' does not exist.";
            accessLogs.add(new AccessLog(userId, objectId, "WRITE", false, msg));
            return msg;
        }

        if (obj.level.getRank() >= user.level.getRank()) {
            obj.content = newContent;
            String msg = String.format("ACCESS GRANTED: User '%s' (%s) can write to object '%s' (%s).\n  Object content updated.",
                    user.name, user.level, obj.name, obj.level);
            accessLogs.add(new AccessLog(userId, objectId, "WRITE", true,
                    "Object level " + obj.level + " >= User level " + user.level));
            return msg;
        } else {
            String msg = String.format("ACCESS DENIED: User '%s' (%s) cannot write to object '%s' (%s).\n  Reason: Star (*) Property violated - cannot write to an object with lower classification than user's clearance.",
                    user.name, user.level, obj.name, obj.level);
            accessLogs.add(new AccessLog(userId, objectId, "WRITE", false,
                    "Star Property: Object level " + obj.level + " < User level " + user.level));
            return msg;
        }
    }

    public void printAccessLog() {
        System.out.println("\n========== ACCESS LOG ==========");
        if (accessLogs.isEmpty()) {
            System.out.println("No access attempts recorded.");
        } else {
            for (AccessLog log : accessLogs) {
                System.out.println(log);
            }
        }
        System.out.println("================================\n");
    }

    public void printSystemState() {
        System.out.println("\n--- Users ---");
        for (User u : users.values()) {
            System.out.printf("  ID: %-6s | Name: %-15s | Level: %s%n", u.id, u.name, u.level);
        }
        System.out.println("\n--- Objects ---");
        for (SecureObject o : objects.values()) {
            System.out.printf("  ID: %-6s | Name: %-20s | Level: %-15s | Content: \"%s\"%n",
                    o.id, o.name, o.level, o.content);
        }
        System.out.println();
    }

    void main() {
        BellLaPadula blp = new BellLaPadula();

        // Initialize users
        blp.addUser("U1", "Alice", SecurityLevel.TOP_SECRET);
        blp.addUser("U2", "Bob", SecurityLevel.SECRET);
        blp.addUser("U3", "Charlie", SecurityLevel.CONFIDENTIAL);
        blp.addUser("U4", "Diana", SecurityLevel.UNCLASSIFIED);

        // Initialize objects
        blp.addObject("O1", "Public Memo", SecurityLevel.UNCLASSIFIED, "Company picnic on Friday.");
        blp.addObject("O2", "Internal Report", SecurityLevel.CONFIDENTIAL, "Q3 revenue projections.");
        blp.addObject("O3", "Mission Plan", SecurityLevel.SECRET, "Operation details for Alpha.");
        blp.addObject("O4", "Nuclear Codes", SecurityLevel.TOP_SECRET, "Launch sequence Alpha-7.");

        blp.printSystemState();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Bell-LaPadula Access Control System");
        System.out.println("Commands: 'read', 'write', 'status', 'log', 'chuser', 'chobj', 'quit'");
        System.out.println("  read   - Attempt to read an object");
        System.out.println("  write  - Attempt to write to an object");
        System.out.println("  status - Display current users and objects");
        System.out.println("  log    - Display access log");
        System.out.println("  chuser - Change a user's security level");
        System.out.println("  chobj  - Change an object's security level");
        System.out.println("  quit   - Exit the program\n");

        while (true) {
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "read": {
                    System.out.print("Enter User ID: ");
                    String userId = scanner.nextLine().trim();
                    System.out.print("Enter Object ID: ");
                    String objectId = scanner.nextLine().trim();
                    System.out.println(blp.attemptRead(userId, objectId));
                    break;
                }
                case "write": {
                    System.out.print("Enter User ID: ");
                    String userId = scanner.nextLine().trim();
                    System.out.print("Enter Object ID: ");
                    String objectId = scanner.nextLine().trim();
                    System.out.print("Enter new content: ");
                    String content = scanner.nextLine().trim();
                    System.out.println(blp.attemptWrite(userId, objectId, content));
                    break;
                }
                case "status":
                    blp.printSystemState();
                    break;
                case "log":
                    blp.printAccessLog();
                    break;
                case "chuser": {
                    System.out.print("Enter User ID: ");
                    String userId = scanner.nextLine().trim();
                    System.out.print("Enter new level (UNCLASSIFIED, CONFIDENTIAL, SECRET, TOP_SECRET): ");
                    String levelStr = scanner.nextLine().trim();
                    SecurityLevel newLevel = SecurityLevel.fromString(levelStr);
                    if (newLevel == null) {
                        System.out.println("Error: Invalid security level.");
                    } else if (!blp.changeUserLevel(userId, newLevel)) {
                        System.out.println("Error: User '" + userId + "' not found.");
                    } else {
                        System.out.println("User '" + userId + "' level changed to " + newLevel + ".");
                    }
                    break;
                }
                case "chobj": {
                    System.out.print("Enter Object ID: ");
                    String objectId = scanner.nextLine().trim();
                    System.out.print("Enter new level (UNCLASSIFIED, CONFIDENTIAL, SECRET, TOP_SECRET): ");
                    String levelStr = scanner.nextLine().trim();
                    SecurityLevel newLevel = SecurityLevel.fromString(levelStr);
                    if (newLevel == null) {
                        System.out.println("Error: Invalid security level.");
                    } else if (!blp.changeObjectLevel(objectId, newLevel)) {
                        System.out.println("Error: Object '" + objectId + "' not found.");
                    } else {
                        System.out.println("Object '" + objectId + "' level changed to " + newLevel + ".");
                    }
                    break;
                }
                case "quit":
                    System.out.println("Exiting. Final access log:");
                    blp.printAccessLog();
                    scanner.close();
                    return;
                default:
                    System.out.println("Error: Unknown command '" + command + "'. Use read, write, status, log, chuser, chobj, or quit.");
            }
            System.out.println();
        }
    }
}