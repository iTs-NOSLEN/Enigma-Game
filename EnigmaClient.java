package datacommunicationproyect.enigma;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class EnigmaClient {
    private JFrame frame;
    private JTextArea displayArea;
    private JTextField inputField;
    private PrintWriter out;
    private JLabel roundLabel; // Etiqueta para mostrar el progreso de las rondas

    public EnigmaClient(String serverAddress, int port) {
        try {
            Socket socket = new Socket(serverAddress, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Configurar Look & Feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Configurar GUI
            frame = new JFrame("Enigma Game Client");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout(10, 10));
            frame.setSize(600, 400);

            // Panel de imagen y ronda
            JPanel topPanel = new JPanel(new BorderLayout());
            JLabel imageLabel = new JLabel(new ImageIcon("C:\\Users\\pcmob\\Downloads\\ProyectoFinalSol--main\\ProyectoFinalSol--main\\Enigma\\src\\main\\java\\datacommunicationproyect\\enigma\\enigma_icon.png"));
            topPanel.add(imageLabel, BorderLayout.WEST);

            // Etiqueta para mostrar las rondas
            roundLabel = new JLabel("Round 1/3", JLabel.CENTER);
            roundLabel.setFont(new Font("Arial", Font.BOLD, 18));
            roundLabel.setForeground(Color.BLACK);
            topPanel.add(roundLabel, BorderLayout.CENTER);

            frame.add(topPanel, BorderLayout.NORTH);

            // Personalizar área de mensajes
            displayArea = new JTextArea(20, 40);
            displayArea.setEditable(false);
            displayArea.setFont(new Font("Aharoni", Font.BOLD, 16));
            displayArea.setBackground(Color.LIGHT_GRAY);
            displayArea.setForeground(Color.BLACK);
            displayArea.setLineWrap(true);
            displayArea.setWrapStyleWord(true);
            displayArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Personalizar campo de entrada
            inputField = new JTextField(30);
            inputField.setFont(new Font("Roboto", Font.PLAIN, 14));
            inputField.setBackground(Color.LIGHT_GRAY);
            inputField.setForeground(Color.BLACK);
            inputField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

            // Botón de enviar
            JButton sendButton = new JButton("SUBMIT");
            sendButton.setBorderPainted(false);
            sendButton.setFont(new Font("Roboto", Font.BOLD, 16));
            sendButton.setBackground(Color.RED);
            sendButton.setForeground(Color.BLACK);

            // Cambiar color del botón al pasar el cursor
            sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    sendButton.setBackground(Color.GREEN);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    sendButton.setBackground(Color.RED);
                }
            });

            // Panel inferior para entrada y botón
            JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
            inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            inputPanel.add(inputField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);

            // Panel central para la pantalla
            JPanel displayPanel = new JPanel(new BorderLayout());
            displayPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            displayPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);

            // Añadir componentes al frame
            frame.add(displayPanel, BorderLayout.CENTER);
            frame.add(inputPanel, BorderLayout.SOUTH);

            // Acciones
            inputField.addActionListener(e -> sendMessage());
            sendButton.addActionListener(e -> sendMessage());

            // Mostrar la ventana
            frame.setVisible(true);

            // Hilo para escuchar mensajes del servidor
            new Thread(() -> {
                try {
                    String message;
                    int round = 1;
                    while ((message = in.readLine()) != null) {
                        // Mostrar los mensajes del servidor
                        displayMessage(message);

                        // Actualizar el progreso de las rondas
                        if (message.contains("Round") && round <= 3) {
                            roundLabel.setText("Round " + round + "/3");
                            round++;
                        }
                    }
                } catch (IOException e) {
                    displayMessage("Connection lost: " + e.getMessage());
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error connecting to server: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    // Enviar mensaje al servidor
    private void sendMessage() {
        String userInput = inputField.getText().trim();
        if (!userInput.isEmpty()) {
            out.println(userInput);
            inputField.setText("");
        }
    }

    // Mostrar mensaje recibido
    private void displayMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String[] lines = message.split("\n");
            for (String line : lines) {
                displayArea.append(line + "\n");
            }
            displayArea.setCaretPosition(displayArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EnigmaClient("localhost", 12345));
    }
}
