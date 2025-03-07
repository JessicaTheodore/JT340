// Updated Files & Fixes
//Client-Server Mode (C2S)
// C2S_Protocol.java – Standardized <CONTROL INFO> <DATA> packet format.
// C2S_UDPClient.java – Reads Client.txt, sends periodic updates, retrieves file lists.
// C2S_UDPServer2.java – Reads Server.txt, tracks clients, detects failures, and broadcasts updates.

//Peer-to-Peer Mode (P2P)
// P2P_Protocol.java – Standardized <CONTROL INFO> <DATA> packet format.
// P2P_UDPClient.java – Reads P2Pconfig.txt, sends updates every 0-30 sec, retrieves file lists.
// P2P_UDPServer2.java – Tracks active peers, detects failures, broadcasts updates.
// P2P_UDPClient2.java – Allows manual messaging while following update rules.
// P2P_UDPServer.java – Handles peer tracking, detects timeouts, updates network.

package Project_1.P2P_New.src;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class P2P_UDPServer2 {

    private static final int TIMEOUT_SECONDS = 30;
    private DatagramSocket socket;
    private int serverPort = 1000;
    
    private final HashMap<InetAddress, Integer> peerPorts = new HashMap<>();
    private final HashMap<InetAddress, String> peerFiles = new HashMap<>();
    private final ConcurrentHashMap<InetAddress, ScheduledFuture<?>> peerTimers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public P2P_UDPServer2() {
        try {
            socket = new DatagramSocket(serverPort);
            System.out.println("P2P Server started on port: " + serverPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void createAndListenSocket() {
        byte[] incomingData = new byte[1024];

        while (true) {
            try {
                // Receive data packet
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                socket.receive(incomingPacket);
                
                // Extract peer details
                InetAddress peerAddress = incomingPacket.getAddress();
                int peerPort = incomingPacket.getPort();
                String message = new String(incomingPacket.getData(), 0, incomingPacket.getLength());

                // Update peer record
                peerPorts.put(peerAddress, peerPort);
                peerFiles.put(peerAddress, parseFileList(message));
                System.out.println("Received update from peer: " + peerAddress);

                // Reset failure detection timer
                resetTimeout(peerAddress);

                // Broadcast the updated peer list
                broadcastPeerStatus();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Extract file list from protocol message
    private String parseFileList(String packetData) {
        int filesIndex = packetData.indexOf("Files:");
        return (filesIndex != -1) ? packetData.substring(filesIndex + 6) : "No files listed";
    }

    // Reset timeout for peer failure detection
    private void resetTimeout(InetAddress peerAddress) {
        if (peerTimers.containsKey(peerAddress)) {
            peerTimers.get(peerAddress).cancel(false);
        }

        Runnable timeoutTask = () -> {
            System.out.println("Peer " + peerAddress + " has timed out!");
            peerPorts.remove(peerAddress);
            peerFiles.remove(peerAddress);
            peerTimers.remove(peerAddress);
            broadcastPeerStatus();
        };

        ScheduledFuture<?> future = scheduler.schedule(timeoutTask, TIMEOUT_SECONDS, TimeUnit.SECONDS);
        peerTimers.put(peerAddress, future);
    }

    // Broadcast the updated peer status to all nodes
    private void broadcastPeerStatus() {
        StringBuilder statusMessage = new StringBuilder("Current Live Peers:\n");
        for (InetAddress peer : peerPorts.keySet()) {
            statusMessage.append(peer).append(" | Files: ").append(peerFiles.get(peer)).append("\n");
        }

        byte[] data = statusMessage.toString().getBytes();
        for (InetAddress peer : peerPorts.keySet()) {
            try {
                DatagramPacket packet = new DatagramPacket(data, data.length, peer, peerPorts.get(peer));
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        P2P_UDPServer2 server = new P2P_UDPServer2();
        server.createAndListenSocket();
    }
}
