package practice1.tut9_2;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class UDPMulticastClientGUI extends JFrame {

    private JTextArea logArea;
    private JButton connectButton;
    private JButton disconnectButton;

    // Volatile ensures visibility across threads
    private volatile boolean isRunning = false;
    private MulticastSocket socket;
    private InetAddress group;

    // Configuration
    private final String MULTICAST_IP = "230.0.0.0";
    private final int PORT = 4321;

    public UDPMulticastClientGUI() {
        setTitle("Multicast Client Receiver");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Offset location so it doesn't overlap publisher
        setLocation(100, 100);

        // UI Components
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        JPanel buttonPanel = new JPanel();
        connectButton = new JButton("Join Group");
        disconnectButton = new JButton("Leave Group");
        disconnectButton.setEnabled(false);

        buttonPanel.add(connectButton);
        buttonPanel.add(disconnectButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        connectButton.addActionListener(e -> startListening());
        disconnectButton.addActionListener(e -> stopListening());
    }

    private void startListening() {
        if (isRunning) return;

        isRunning = true;
        connectButton.setEnabled(false);
        disconnectButton.setEnabled(true);
        log("Joining Multicast Group " + MULTICAST_IP + "...");

        // Start background thread for receive()
        new Thread(() -> {
            try {
                socket = new MulticastSocket(PORT);
                group = InetAddress.getByName(MULTICAST_IP);
                socket.joinGroup(group);

                log("Joined. Waiting for messages...");

                byte[] buffer = new byte[1024];
                int n = 1;

                while (isRunning) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    // This blocks until a message comes or socket is closed
                    socket.receive(packet);

                    String msg = new String(packet.getData(), 0, packet.getLength());
                    log("[Msg " + n + "]: " + msg);
                    n++;

                    // Reset buffer length for next packet
                    packet.setLength(buffer.length);
                }

            } catch (IOException ex) {
                if (isRunning) {
                    log("Error: " + ex.getMessage());
                } else {
                    // Exception expected when we close socket manually
                    log("Connection closed.");
                }
            }
        }).start();
    }

    private void stopListening() {
        isRunning = false;
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);

        // We must close the socket to unblock the socket.receive() call
        if (socket != null && !socket.isClosed()) {
            try {
                if (group != null) {
                    socket.leaveGroup(group);
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UDPMulticastClientGUI().setVisible(true));
    }
}