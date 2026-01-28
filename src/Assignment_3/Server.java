package Assignment_3;
import java.util.UUID;

public class Server {
    public static String serverId = "";
    public static int requestCount = 0;
    public static String serverStatus = "Available";

    public Server(int requestCount) {
        Server.serverId = UUID.randomUUID().toString();;
        Server.requestCount = requestCount;
    }

    public String toString() {
        return "Server ID: " + serverId + " - Number of Requests: " + requestCount;
    }
}
