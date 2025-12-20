package practice1.tut9_2;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPMulticastPublisherGUI extends JFrame {

    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private boolean isRunning = false;
    private int counter = 1;

    // Configuration
    private final String MULTICAST_IP = "230.0.0.0";
    private final int PORT = 4321;

    public UDPMulticastPublisherGUI() {
        setTitle("Multicast Publisher");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // UI Components
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start Streaming");
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        startButton.addActionListener(e -> startBroadcasting());
        stopButton.addActionListener(e -> stopBroadcasting());
    }

    private void startBroadcasting() {
        isRunning = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        counter = 1; // Reset counter on new start

        // Run the "while(true)" loop in a separate thread
        new Thread(() -> {
            log("Publisher started. Sending to " + MULTICAST_IP + ":" + PORT);

            try (DatagramSocket socket = new DatagramSocket()) {
                InetAddress group = InetAddress.getByName(MULTICAST_IP);

                while (isRunning) {
                    String msg = "Hi there! This is multicast message number " + counter + " from publisher!";
                    byte[] buffer = msg.getBytes();

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                    socket.send(packet);

                    log("Sent: " + msg);
                    counter++;

                    // Sleep for 1 second (1000ms)
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                log("Broadcasting interrupted.");
            } catch (IOException e) {
                log("Network Error: " + e.getMessage());
            }
        }).start();
    }

    private void stopBroadcasting() {
        isRunning = false; // This breaks the while loop
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        log("Publisher stopped.");
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            // Auto-scroll to bottom
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UDPMulticastPublisherGUI().setVisible(true));
    }
}