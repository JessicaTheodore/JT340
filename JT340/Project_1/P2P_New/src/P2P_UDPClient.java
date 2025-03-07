package Project_1.P2P_New.src;

import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class P2P_UDPClient {
    private static String directoryPath;
    private static List<String> peerIps = new ArrayList<>();
    private static int port;

    public static void main(String[] args) {
        loadConfig(); // Load configuration from P2Pconfig.txt
        System.out.println("P2P Client Config Loaded: Directory - " + directoryPath + ", Port - " + port);

        // Start sending heartbeat messages in a separate thread
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(() -> sendHeartbeat(), 0, 30, TimeUnit.SECONDS);
        
        // Request file listings from peers every 15 seconds
        scheduler.scheduleAtFixedRate(() -> {
            for (String peer : peerIps) {
                System.out.println("Requesting file listing from: " + peer);
                requestFileListing(peer, port);
            }
        }, 0, 15, TimeUnit.SECONDS);
    }

    private static void loadConfig() {
        try {
            File configFile = Paths.get("P2P_New", "Config", "P2Pconfig.txt").toFile();

            if (!configFile.exists()) {
                System.out.println("Error: P2Pconfig.txt not found at " + configFile.getAbsolutePath());
                return;
            }

            System.out.println("Reading config from: " + configFile.getAbsolutePath());

            Properties properties = new Properties();
            FileInputStream fis = new FileInputStream(configFile);
            properties.load(fis);
            fis.close();

            // Read values from the config file
            String peers = properties.getProperty("peer_ips");
            if (peers != null) {
                peerIps = Arrays.asList(peers.split("\\s*,\\s*")); // Trim spaces
            }

            port = Integer.parseInt(properties.getProperty("port", "1000"));
            directoryPath = properties.getProperty("directory_path");

            System.out.println("Loaded Peers: " + peerIps);
            System.out.println("Port: " + port);
            System.out.println("Directory Path: " + directoryPath);

        } catch (Exception e) {
            System.out.println("Error loading config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 🔹 Send Heartbeat Messages to Peers
    private static void sendHeartbeat() {
        try (DatagramSocket socket = new DatagramSocket()) {
            String heartbeatMessage = "HEARTBEAT from " + InetAddress.getLocalHost().getHostAddress();
            byte[] buffer = heartbeatMessage.getBytes();

            for (String peer : peerIps) {
                InetAddress peerAddress = InetAddress.getByName(peer);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, peerAddress, port);
                socket.send(packet);
            }

            System.out.println("Heartbeat sent to peers: " + peerIps);
        } catch (Exception e) {
            System.out.println("Error sending heartbeat: " + e.getMessage());
        }
    }

    // 🔹 Request File Listings from Peers
    private static void requestFileListing(String peerIP, int peerPort) {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] requestData = "REQUEST_FILE_LISTING".getBytes();

            InetAddress peerAddress = InetAddress.getByName(peerIP);
            DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, peerAddress, peerPort);

            socket.send(requestPacket);
            System.out.println("Sent file listing request to " + peerIP + ":" + peerPort);

            // Receive the response
            byte[] buffer = new byte[4096];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);

            socket.setSoTimeout(5000); // Set timeout in case peer doesn't respond
            socket.receive(responsePacket);

            String fileList = new String(responsePacket.getData(), 0, responsePacket.getLength());
            System.out.println("File listing from " + peerIP + ":\n" + fileList);

            socket.close();
        } catch (Exception e) {
            System.out.println("Error requesting file listing from " + peerIP + ": " + e.getMessage());
        }
    }
}
