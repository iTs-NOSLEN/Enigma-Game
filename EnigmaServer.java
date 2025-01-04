package datacommunicationproyect.enigma;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EnigmaServer {
    private static final int PORT = 12345;
    private HashMap<String, String> data = new HashMap<>();
    private Random random = new Random();

    public EnigmaServer() {
        loadDataFromCSV("C:\\Users\\pcmob\\Downloads\\ProyectoFinalSol--main\\ProyectoFinalSol--main\\Enigma\\src\\main\\java\\datacommunicationproyect\\enigma\\words.csv"); // Adjust the path to your CSV file
    }

    private void loadDataFromCSV(String fileName) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(fileName));
            for (String line : lines) {
                String[] fields = line.split(",", 2);
                if (fields.length == 2) {
                    data.put(fields[0].trim(), fields[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map.Entry<String, String> getRandomEntry() {
        if (!data.isEmpty()) {
            List<Map.Entry<String, String>> entries = new ArrayList<>(data.entrySet());
            return entries.get(random.nextInt(entries.size()));
        }
        return null;
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server is running... Waiting for players.");
        
        try {
            while (true) {
                Socket player1Socket = serverSocket.accept();
                System.out.println("Player 1 connected.");
                Socket player2Socket = serverSocket.accept();
                System.out.println("Player 2 connected.");

                new GameSession(player1Socket, player2Socket, this).start();
            }
        } finally {
            serverSocket.close();
        }
    }

    public Map.Entry<String, String> getPuzzle() {
        return getRandomEntry();
    }

    public static void main(String[] args) throws IOException {
        new EnigmaServer().start();
    }
}

class GameSession extends Thread {
    private Socket player1Socket;
    private Socket player2Socket;
    private EnigmaServer server;

    public GameSession(Socket player1Socket, Socket player2Socket, EnigmaServer server) {
        this.player1Socket = player1Socket;
        this.player2Socket = player2Socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (
            BufferedReader in1 = new BufferedReader(new InputStreamReader(player1Socket.getInputStream()));
            PrintWriter out1 = new PrintWriter(player1Socket.getOutputStream(), true);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(player2Socket.getInputStream()));
            PrintWriter out2 = new PrintWriter(player2Socket.getOutputStream(), true)
        ) {
            out1.println("Welcome Player 1!");
            out2.println("Welcome Player 2!");

            int rounds = 3; // Number of rounds
            int score1 = 0, score2 = 0;

            for (int i = 1; i <= rounds; i++) {
                Map.Entry<String, String> puzzle = server.getPuzzle();
                if (puzzle == null) break;

                String definition = puzzle.getValue();
                String answer = puzzle.getKey();

                out1.println("Round " + i + ": Definition: " + definition);
                out1.println("Enter your answer:");
                out2.println("Round " + i + ": Definition: " + definition);
                out2.println("Enter your answer:");

                String response1 = in1.readLine();
                String response2 = in2.readLine();

                // Check Player 1's answer
                if (response1 != null && response1.equalsIgnoreCase(answer)) {
                    out1.println("Correct! +10 points");
                    score1 += 10;
                } else {
                    out1.println("Incorrect! The correct answer was: " + answer);
                }

                // Check Player 2's answer
                if (response2 != null && response2.equalsIgnoreCase(answer)) {
                    out2.println("Correct! +10 points");
                    score2 += 10;
                } else {
                    out2.println("Incorrect! The correct answer was: " + answer);
                }
            }

            // Game over messages
            out1.println("Game Over! Your score: " + score1 + ". Opponent's score: " + score2);
            out2.println("Game Over! Your score: " + score2 + ". Opponent's score: " + score1);

            if (score1 > score2) {
                out1.println("You WIN!");
                out2.println("You LOSE!");
            } else if (score1 < score2) {
                out2.println("You WIN!");
                out1.println("You LOSE!");
            } else {
                out1.println("It's a TIE!");
                out2.println("It's a TIE!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
