package Project_1.C2S_New.src;

// Where Heartbeats Are Implemented
// In C2S_UDPClient.java & P2P_UDPClient.java
// Uses TimeUnit.SECONDS.sleep(random) to introduce random 0-30 sec delays before sending updates.
// Calls createAndSendPacket() each cycle to notify the server or peers that it’s alive.

// In C2S_UDPServer2.java & P2P_UDPServer2.java
// Each received update resets a 30-second countdown timer for that node.
// If no update is received within 30 sec, the node is marked offline and removed
// If a node sends another update later, it rejoins automatically.

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Properties;

public class C2S_UDPClient extends C2S_Protocol implements Serializable {

    private static final long serialVersionUID = 1L;
    private static SecureRandom secureRandom = new SecureRandom();
    
    private DatagramSocket socket;
    private InetAddress serverIp;
    private int serverPort;
    private String directoryPath;

    public C2S_UDPClient() {
        loadConfig();
    }

    // Load config file (Client.txt)
    private void loadConfig() {
        try (FileInputStream fis = new FileInputStream("Client.txt")) {
            Properties prop = new Properties();
            prop.load(fis);

            // Read and parse values
            this.serverIp = InetAddress.getByName(prop.getProperty("server_ips").trim());
            this.serverPort = Integer.parseInt(prop.getProperty("port").trim());
            this.directoryPath = prop.getProperty("directory_path").trim();

            System.out.println("Loaded Config - Server IP: " + serverIp + ", Port: " + serverPort);
        } catch (IOException e) {
            System.err.println("Error reading Client.txt: " + e.getMessage());
        }
    }

    public void createAndSendPacket() {
        try {
            socket = new DatagramSocket();

            // Get file list
            String fileList = getFileListing(directoryPath);

            // Construct protocol packet
            this.setTimeStamp(System.currentTimeMillis());
            this.setDestIp(serverIp);
            this.setDestPort(serverPort);
            this.setFileList(fileList);

            // Convert protocol packet to string format
            String packetData = this.toPacketString();
            byte[] data = packetData.getBytes();

            // Send packet to server
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, serverIp, serverPort);
            socket.send(sendPacket);
            System.out.println("Packet sent to server.");

            // Receive response from server
            byte[] incomingData = new byte[1024];
            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            socket.receive(incomingPacket);

            String response = new String(incomingPacket.getData(), 0, incomingPacket.getLength());
            System.out.println("Response from server: " + response);

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Retrieve list of files in the specified directory
    private String getFileListing(String path) {
        try {
            List<String> fileList = Files.list(Paths.get(path))
                    .map(p -> p.getFileName().toString())
                    .toList();
            return String.join(", ", fileList);
        } catch (IOException e) {
            return "Error retrieving files";
        }
    }

    public static void main(String[] args) throws InterruptedException {
        C2S_UDPClient client = new C2S_UDPClient();

        // Send periodic updates (random 0-30 sec intervals)
        int maxAttempts = 20;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int random = secureRandom.nextInt(30) + 1;
            System.out.println("Waiting for " + random + " seconds before next update...");
            TimeUnit.SECONDS.sleep(random);
            client.createAndSendPacket();
        }

        System.out.println("Max attempts reached. Exiting.");
    }
}
