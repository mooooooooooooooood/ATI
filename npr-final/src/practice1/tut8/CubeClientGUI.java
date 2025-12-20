package practice1.tut8;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.*;

public class CubeClientGUI extends JFrame {

    private JTextField numberField;
    private JTextArea resultArea;
    private JButton calculateButton;
    private final String SERVER_HOST = "localhost";
    private final int SERVER_PORT = 9876;

    public CubeClientGUI() {
        setTitle("Cube Calculator Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ==== UI Components ====
        JPanel inputPanel = new JPanel(new FlowLayout());
        numberField = new JTextField(10);
        calculateButton = new JButton("Calculate Cube");

        inputPanel.add(new JLabel("Enter Number (R): "));
        inputPanel.add(numberField);
        inputPanel.add(calculateButton);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultArea);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // ==== Action Listener ====
        calculateButton.addActionListener(this::sendRequest);
        numberField.addActionListener(this::sendRequest); // Allow pressing Enter
    }

    private void sendRequest(ActionEvent e) {
        String inputStr = numberField.getText().trim();

        if (inputStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a number!");
            return;
        }

        // Disable button during processing
        calculateButton.setEnabled(false);
        log("Sending: " + inputStr);

        new Thread(() -> {
            DatagramSocket clientSocket = null;
            try {
                clientSocket = new DatagramSocket();
                clientSocket.setSoTimeout(3000); // 3-second timeout
                InetAddress IPAddress = InetAddress.getByName(SERVER_HOST);

                byte[] sendData = inputStr.getBytes();
                byte[] receiveData = new byte[1024];

                // 1. Send Data
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, SERVER_PORT);
                clientSocket.send(sendPacket);

                // 2. Receive Result
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);

                String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

                log("Result (R^3): " + response);
                log("--------------------------");

            } catch (SocketTimeoutException ex) {
                log("Error: Server did not respond (Timeout).");
            } catch (Exception ex) {
                log("Error: " + ex.getMessage());
            } finally {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
                // Re-enable button on GUI thread
                SwingUtilities.invokeLater(() -> {
                    calculateButton.setEnabled(true);
                    numberField.selectAll(); // Select text for easy overwriting
                    numberField.requestFocus();
                });
            }
        }).start();
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> resultArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CubeClientGUI().setVisible(true));
    }
}