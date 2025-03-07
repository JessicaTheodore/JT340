package Project_1.P2P_New.src;

// Where Heartbeats Are Implemented
// In C2S_UDPClient.java & P2P_UDPClient.java
// Uses TimeUnit.SECONDS.sleep(random) to introduce random 0-30 sec delays before sending updates.
// Calls createAndSendPacket() each cycle to notify the server or peers that it’s alive.

// In C2S_UDPServer2.java & P2P_UDPServer2.java
// Each received update resets a 30-second countdown timer for that node.
// If no update is received within 30 sec, the node is marked offline and removed
// If a node sends another update later, it rejoins automatically.

import java.io.*;
import java.util.*;

public class P2P_UDPClient {
    private static String directoryPath;
    private static List<String> peerIps = new ArrayList<>();
    private static int port;

    public static void main(String[] args) {
        loadConfig();  // Load config before doing anything
        System.out.println("P2P Client Config Loaded: Directory - " + directoryPath + ", Port - " + port);

    }

    private static void loadConfig() {
        try {
            // Update the path to match your project structure
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
}
