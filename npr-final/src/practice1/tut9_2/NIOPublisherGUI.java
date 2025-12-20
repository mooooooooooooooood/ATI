package practice1.tut9_2;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class NIOPublisherGUI extends JFrame {

    private JTextField interfaceField;
    private JTextArea logArea;
    private JButton startButton, stopButton;
    private boolean isRunning = false;

    // Default config
    private final String MULTICAST_IP = "230.0.0.0";
    private final int PORT = 4567;

    public NIOPublisherGUI() {
        setTitle("NIO Publisher (DatagramChannel)");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Configuration Panel
        JPanel configPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        configPanel.setBorder(BorderFactory.createTitledBorder("Configuration"));

        interfaceField = new JTextField("wireless_32768"); // Default from your code
        configPanel.add(new JLabel("Interface Name (e.g., wlan0, eth0):"));
        configPanel.add(interfaceField);

        // Buttons
        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        // Layout
        add(configPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Events
        startButton.addActionListener(e -> startSending());
        stopButton.addActionListener(e -> stopSending());
    }

    private void startSending() {
        String ifaceName = interfaceField.getText().trim();
        if (ifaceName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Interface Name (check NetInfoGUI)!");
            return;
        }

        isRunning = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        interfaceField.setEditable(false);

        new Thread(() -> {
            log("Starting publisher on interface: " + ifaceName);
            int n = 1;

            try (DatagramChannel datagramChannel = DatagramChannel.open()) {
                // Bind and Set Interface
                datagramChannel.bind(null);
                NetworkInterface networkInterface = NetworkInterface.getByName(ifaceName);

                if (networkInterface == null) {
                    throw new IOException("Interface '" + ifaceName + "' not found!");
                }

                datagramChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
                InetSocketAddress inetSocketAddress = new InetSocketAddress(MULTICAST_IP, PORT);

                while (isRunning) {
                    String message = "Hi there! This is NIO message number " + n + " from publisher!";
                    ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes());

                    datagramChannel.send(byteBuffer, inetSocketAddress);

                    log("Sent: " + message);
                    n++;
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                log("Error: " + e.getMessage());
                stopSending(); // Reset UI on error
            }
        }).start();
    }

    private void stopSending() {
        isRunning = false;
        SwingUtilities.invokeLater(() -> {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            interfaceField.setEditable(true);
            log("Publisher stopped.");
        });
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NIOPublisherGUI().setVisible(true));
    }
}