package Assignment_3;

import java.util.Scanner;

public class Availability {

    void main() {
        Scanner scanner = new Scanner(System.in);
        int[] numberOfServersAndRequests = promptUserForNumberOfServers(scanner);
        System.out.println();
        System.out.println("Initializing " + numberOfServersAndRequests[0] + " servers...");
        System.out.println();

        Server[] servers = new Server[numberOfServersAndRequests[0]];
        for (int i = 0; i < servers.length; i++) {
            servers[i] = new Server(i + 1);
        }

        System.out.println("Starting simulation with " + numberOfServersAndRequests[1] + " requests");

        roundRobinScheduling(servers, numberOfServersAndRequests[1]);

        System.out.println("Final Statistics");
        for (Server server : servers) {
            System.out.println(server);
        }

        scanner.close();
    }

    private static int[] promptUserForNumberOfServers(Scanner scanner) {
        int[] numberOfServersAndRequests = new int[2];

        System.out.print("Enter the number of servers: ");
        int numberOfServers = scanner.nextInt();
        System.out.print("Enter the total number of requests: ");
        int numberOfRequests = scanner.nextInt();

        numberOfServersAndRequests[0] = numberOfServers;
        numberOfServersAndRequests[1] = numberOfRequests;
        return numberOfServersAndRequests;
    }

    // Distributes requests across servers using round-robin scheduling.
    private static void roundRobinScheduling(
            Server[] servers,
            int numberOfRequests
    ) {
        int currentIndex = 0;

        for (int i = 0; i < numberOfRequests; i++) {

            simulateServerFailure(servers);

            int startIndex = currentIndex;
            boolean found = false;

            // Find the next available server.
            while (!found) {
                if (servers[currentIndex].isAvailable()) {
                    servers[currentIndex].handleRequest(i + 1);
                    currentIndex = (currentIndex + 1) % servers.length;
                    found = true;
                } else {
                    currentIndex = (currentIndex + 1) % servers.length;

                    // All servers checked and none available.
                    if (currentIndex == startIndex) {
                        System.out.println("ERROR: No servers available for request " + (i + 1));
                        break;
                    }
                }
            }

            simulateServerRecovery(servers);
        }
    }

    // 10% chance per request that an available server fails.
    private static void simulateServerFailure(Server[] servers) {
        for (Server server : servers) {
            if (server.isAvailable() && Math.random() < 0.1) {
                server.fail();
            }
        }
    }

    // 5% chance per request that a failed server recovers.
    private static void simulateServerRecovery(Server[] servers) {
        for (Server server : servers) {
            if (!server.isAvailable() && Math.random() < 0.05) {
                server.recover();
            }
        }
    }
}