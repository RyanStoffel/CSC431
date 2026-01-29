package Assignment_3;

import java.util.Scanner;

public class Availability {

    void main() {
        Scanner scanner = new Scanner(System.in);
        int[] numberOfServersAndRequests = promptUserForNumberOfServers(
            scanner
        );
        Server[] servers = new Server[numberOfServersAndRequests[0]];
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

    private static void roundRobinScheduling(
        Server[] servers,
        int numberOfRequests
    ) {}
}
