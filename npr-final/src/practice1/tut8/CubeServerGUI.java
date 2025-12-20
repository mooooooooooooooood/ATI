package practice1.tut8;

import javax.swing.*;
import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class CubeServerGUI extends JFrame {

    private JTextArea logArea;
    private DatagramSocket serverSocket;
    private boolean running = false;
    private final int PORT = 9876;

    public CubeServerGUI() {
        setTitle("Cube UDP Server (Port " + PORT + ")");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // UI Setup
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);

        add(scrollPane, BorderLayout.CENTER);

        // Auto-start server logic
        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new DatagramSocket(PORT);
                running = true;
                log("Server is running on port " + PORT + "...");

                while (running) {
                    byte[] receiveData = new byte[1024];
                    byte[] sendData;

                    // 1. Receive Packet
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);

                    String strInput = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
                    InetAddress clientIP = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();

                    log("Request from " + clientIP + ": " + strInput);

                    try {
                        // 2. Calculate Cube
                        double number = Double.parseDouble(strInput);
                        double cubedValue = Math.pow(number, 3);
                        String strOutput = String.valueOf(cubedValue);

                        // 3. Send Response
                        sendData = strOutput.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientIP, clientPort);
                        serverSocket.send(sendPacket);

                        log("   -> Sent result: " + cubedValue);

                    } catch (NumberFormatException e) {
                        log("   -> Error: Invalid number received.");
                        // Optional: Send error message back to client
                        String errorMsg = "Error: Invalid Number";
                        sendData = errorMsg.getBytes();
                        DatagramPacket errorPacket = new DatagramPacket(sendData, sendData.length, clientIP, clientPort);
                        serverSocket.send(errorPacket);
                    }
                }
            } catch (Exception e) {
                log("Server Error: " + e.getMessage());
            }
        }).start();
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CubeServerGUI().setVisible(true));
    }
}