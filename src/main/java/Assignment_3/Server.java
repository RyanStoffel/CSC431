package Assignment_3;

public class Server {

    public int serverId;
    public int requestCount = 0;
    public String serverStatus = "Available";

    public Server(int serverId) {
        this.serverId = serverId;
    }

    public String toString() {
        return "Server " + serverId + " - Status: " + serverStatus +
                " - Requests: " + requestCount;
    }

    // Processes a request and increments the counter.
    public void handleRequest(int requestNumber) {
        this.requestCount++;
        System.out.println("Request " + requestNumber + " handled by Server " + serverId +
                " (Total: " + requestCount + ")");
    }

    public boolean isAvailable() {
        return this.serverStatus.equals("Available");
    }

    public void fail() {
        this.serverStatus = "Failed";
        System.out.println("[!] Server " + serverId + " FAILED");
    }

    public void recover() {
        this.serverStatus = "Available";
        System.out.println("[+] Server " + serverId + " RECOVERED");
    }
}