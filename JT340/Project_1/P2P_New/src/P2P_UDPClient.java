package Project_1.P2P_New.src;


import java.io.*;
import java.net.*;
import java.util.*;

public class P2P_UDPClient {
    private static String directoryPath;
    private static List<String> peerIps = new ArrayList<>();
    private static int port;

    public static void main(String[] args) {
        loadConfig();  // Load config before doing anything
        System.out.println("P2P Client Config Loaded: Directory - " + directoryPath + ", Port - " + port);

        // Request file listings from peers
        for (String peer : peerIps) {
            System.out.println("Requesting file listing from: " + peer);
            requestFileListing(peer, port);
        }
    }

    private static void loadConfig() {
        try {
            File configFile = new File("P2P_New/Config/P2Pconfig.txt");

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
                peerIps = Arrays.asList(peers.split(","));
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


    //Part about requesting file listings from other nodes
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
