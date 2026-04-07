import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

void main() {
    Scanner scanner = new Scanner(System.in);
    while (true) {
        clear();
        System.out.println("RBAC System");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("> ");
        switch (scanner.nextLine().trim()) {
            case "1" -> registerFlow(scanner);
            case "2" -> loginFlow(scanner);
            case "3" -> { clear(); System.out.println("Goodbye."); return; }
            default  -> System.out.println("Invalid choice.");
        }
    }
}

private static void clear() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
}

private static void registerFlow(Scanner scanner) {
    clear();
    System.out.println("Register");
    System.out.print("Username : "); String username = scanner.nextLine().trim();
    System.out.print("Email    : "); String email    = scanner.nextLine().trim();
    System.out.print("Password : "); String password = scanner.nextLine().trim();

    List<String> validRoles;

    if (userStore.isEmpty()) {
        System.out.println("First user — assign your own role.");
        System.out.println("Roles available: " + roleStore.keySet());
        System.out.print("Roles (comma-separated): ");
        validRoles = parseRoles(scanner.nextLine().trim().toUpperCase());
        if (validRoles.isEmpty()) {
            System.out.println("No valid roles. Registration cancelled.");
            return;
        }
    } else {
        validRoles = List.of("VIEWER");
        System.out.println("Role assigned: VIEWER (an admin can change this)");
    }

    userStore.put(username, new User(username, email, password, validRoles));
    System.out.println("Registered '" + username + "' with roles: " + validRoles);
}

private static void loginFlow(Scanner scanner) {
    clear();
    System.out.println("Login");
    System.out.print("Username : "); String username = scanner.nextLine().trim();
    System.out.print("Password : "); String password = scanner.nextLine().trim();

    User user = authenticate(username, password);
    if (user == null) return;

    clear();
    System.out.println("Logged in: " + user.username + " " + user.roles);

    if (user.isAdmin()) adminMenu(scanner, user);
    else userMenu(scanner, user);

    clear();
    System.out.println("Logged out: " + user.username);
}

private static void userMenu(Scanner scanner, User user) {
    while (true) {
        System.out.println("\nUser Menu");
        System.out.println("1. Read document");
        System.out.println("2. Edit document");
        System.out.println("3. Delete document");
        System.out.println("4. Logout");
        System.out.print("> ");
        switch (scanner.nextLine().trim()) {
            case "1" -> { clear(); checkPermission(user, Permission.READ,   "Document content: [lorem ipsum]"); }
            case "2" -> { clear(); checkPermission(user, Permission.WRITE,  "Document updated."); }
            case "3" -> { clear(); checkPermission(user, Permission.DELETE, "Document deleted."); }
            case "4" -> { return; }
            default  -> System.out.println("Invalid choice.");
        }
    }
}

private static void adminMenu(Scanner scanner, User user) {
    while (true) {
        System.out.println("\nAdmin Menu");
        System.out.println("1. Create role");
        System.out.println("2. Delete role");
        System.out.println("3. List roles");
        System.out.println("4. Assign role to user");
        System.out.println("5. Remove role from user");
        System.out.println("6. User menu");
        System.out.println("7. Logout");
        System.out.print("> ");
        switch (scanner.nextLine().trim()) {
            case "1" -> { clear(); createRoleFlow(scanner); }
            case "2" -> { clear(); deleteRoleFlow(scanner); }
            case "3" -> { clear(); listRoles(); }
            case "4" -> { clear(); assignRoleFlow(scanner); }
            case "5" -> { clear(); removeRoleFlow(scanner); }
            case "6" -> { clear(); userMenu(scanner, user); }
            case "7" -> { return; }
            default  -> System.out.println("Invalid choice.");
        }
    }
}

private static void createRoleFlow(Scanner scanner) {
    System.out.println("Create Role");
    System.out.print("Role name: ");
    String name = scanner.nextLine().trim().toUpperCase();
    if (roleStore.containsKey(name)) { System.out.println("Role already exists."); return; }

    System.out.println("Permissions available: " + Arrays.toString(Permission.values()));
    System.out.print("Permissions (comma-separated): ");
    Set<Permission> perms = parsePermissions(scanner.nextLine().trim().toUpperCase());

    roleStore.put(name, perms);
    System.out.println("Created role '" + name + "' with: " + perms);
}

private static void deleteRoleFlow(Scanner scanner) {
    System.out.println("Delete Role");
    System.out.print("Role to delete: ");
    String name = scanner.nextLine().trim().toUpperCase();
    if (name.equals("ADMIN"))         { System.out.println("Cannot delete ADMIN role."); return; }
    if (!roleStore.containsKey(name)) { System.out.println("Role not found."); return; }
    roleStore.remove(name);
    System.out.println("Deleted role '" + name + "'.");
}

private static void listRoles() {
    System.out.println("Roles");
    roleStore.forEach((role, perms) -> System.out.println(role + ": " + perms));
}

private static void assignRoleFlow(Scanner scanner) {
    System.out.println("Assign Role");
    User target = resolveUser(scanner);
    if (target == null) return;

    System.out.println("Current roles: " + target.roles);
    System.out.println("Roles available: " + roleStore.keySet());
    System.out.print("Roles to assign (comma-separated): ");
    List<String> toAdd = parseRoles(scanner.nextLine().trim().toUpperCase());

    List<String> skipped = new ArrayList<>();
    for (String role : toAdd) {
        if (target.roles.contains(role)) { skipped.add(role); continue; }
        target.roles.add(role);
    }

    System.out.println("Assigned: " + toAdd);
    if (!skipped.isEmpty()) System.out.println("Already had: " + skipped);
}

private static void removeRoleFlow(Scanner scanner) {
    System.out.println("Remove Role");
    User target = resolveUser(scanner);
    if (target == null) return;

    System.out.println("Current roles: " + target.roles);
    System.out.print("Roles to remove (comma-separated): ");
    String[] parts = scanner.nextLine().trim().toUpperCase().split(",");

    List<String> toRemove = new ArrayList<>();
    for (String p : parts) toRemove.add(p.trim());

    if (target.roles.size() - toRemove.size() < 1) {
        System.out.println("User must retain at least one role.");
        return;
    }

    List<String> notFound = new ArrayList<>();
    for (String role : toRemove) {
        if (!target.roles.remove(role)) notFound.add(role);
    }

    System.out.println("Removed: " + toRemove);
    if (!notFound.isEmpty()) System.out.println("Not on user: " + notFound);
}

private static void checkPermission(User user, Permission required, String successMessage) {
    if (user.hasPermission(required)) System.out.println(successMessage);
    else System.out.println("Access denied. Missing permission: " + required);
}

private static User resolveUser(Scanner scanner) {
    System.out.print("Username: ");
    String username = scanner.nextLine().trim();
    User user = userStore.get(username);
    if (user == null) System.out.println("User not found.");
    return user;
}

private static List<String> parseRoles(String input) {
    List<String> valid = new ArrayList<>();
    for (String r : input.split(",")) {
        String role = r.trim();
        if (roleStore.containsKey(role)) valid.add(role);
        else System.out.println("Unknown role '" + role + "' — skipped.");
    }
    return valid;
}

private static Set<Permission> parsePermissions(String input) {
    Set<Permission> perms = new HashSet<>();
    for (String p : input.split(",")) {
        try {
            perms.add(Permission.valueOf(p.trim()));
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown permission '" + p.trim() + "' — skipped.");
        }
    }
    return perms;
}

private static User authenticate(String username, String password) {
    User user = userStore.get(username);
    if (user == null || !user.password.equals(User.hashString(password))) {
        System.out.println("Invalid username or password.");
        return null;
    }
    return user;
}

private static Set<Permission> permissions(String role) {
    return roleStore.getOrDefault(role.toUpperCase(), new HashSet<>());
}

private static final Map<String, Set<Permission>> roleStore = new HashMap<>(Map.of(
        "ADMIN",  new HashSet<>(List.of(Permission.READ, Permission.WRITE, Permission.DELETE)),
        "EDITOR", new HashSet<>(List.of(Permission.READ, Permission.WRITE)),
        "VIEWER", new HashSet<>(List.of(Permission.READ))
));

private static final Map<String, User> userStore = new HashMap<>();

enum Permission { READ, WRITE, DELETE }

static class User {
    String username;
    String email;
    String password;
    List<String> roles;

    User(String username, String email, String password, List<String> roles) {
        this.username = username;
        this.email    = email;
        this.password = hashString(password);
        this.roles    = new ArrayList<>(roles);
    }

    Set<Permission> getPermissions() {
        Set<Permission> merged = new HashSet<>();
        for (String role : roles) merged.addAll(permissions(role));
        return merged;
    }

    boolean hasPermission(Permission permission) { return getPermissions().contains(permission); }
    boolean isAdmin() { return roles.contains("ADMIN"); }

    static String hashString(String input) {
        if (input == null || input.isEmpty()) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xFF & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}