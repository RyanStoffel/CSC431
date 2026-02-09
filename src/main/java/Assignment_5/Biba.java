package Assignment_5;

public class Biba {
    void main() {
        User user = new User(1, IntegrityLevel.LOW);
        Object object = new Object(1, IntegrityLevel.HIGH, "Sample content");
    }
}

class User {
    private int id;
    public IntegrityLevel integrityLevel;

    User(int id, IntegrityLevel integrityLevel) {
        this.id = id;
        this.integrityLevel = integrityLevel;
    }
}

class Object {
    private int id;
    public IntegrityLevel integrityLevel;
    private String content;

    Object(int id, IntegrityLevel integrityLevel, String content) {
        this.id = id;
        this.integrityLevel = integrityLevel;
        this.content = content;
    }

//    public static void read(int id, User user) {
//        IntegrityLevel userLevel = user.integrityLevel;
//        if (user)
//    }
}

enum IntegrityLevel {
    LOW, MEDIUM, HIGH, VERY_HIGH
}