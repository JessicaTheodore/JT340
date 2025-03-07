package Project_1.P2P_New.src;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class P2P_UDPServer {

    private static final int TIMEOUT_SECONDS = 30;
    private DatagramSocket socket;
    private int serverPort = 1000;
    
    private final HashMap<InetAddress, Integer> peerPorts = new HashMap<>();
    private final HashMap<InetAddress, String> peerFiles = new HashMap<>();
    private final ConcurrentHashMap<InetAddress, ScheduledFuture<?>> peerTimers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public P2P_UDPServer() {
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
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                socket.receive(incomingPacket);
                
                InetAddress peerAddress = incomingPacket.getAddress();
                int peerPort = incomingPacket.getPort();
                String message = new String(incomingPacket.getData(), 0, incomingPacket.getLength());

                peerPorts.put(peerAddress, peerPort);
                peerFiles.put(peerAddress, parseFileList(message));
                System.out.println("Update from peer: " + peerAddress);

                resetTimeout(peerAddress);
                broadcastPeerStatus();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String parseFileList(String packetData) {
        int filesIndex = packetData.indexOf("Files:");
        return (filesIndex != -1) ? packetData.substring(filesIndex + 6) : "No files listed";
    }

    private void resetTimeout(InetAddress peerAddress) {
        if (peerTimers.containsKey(peerAddress)) {
            peerTimers.get(peerAddress).cancel(false);
        }

        Runnable timeoutTask = () -> {
            System.out.println("Peer " + peerAddress + " timed out!");
            peerPorts.remove(peerAddress);
            peerFiles.remove(peerAddress);
            peerTimers.remove(peerAddress);
            broadcastPeerStatus();
        };

        ScheduledFuture<?> future = scheduler.schedule(timeoutTask, TIMEOUT_SECONDS, TimeUnit.SECONDS);
        peerTimers.put(peerAddress, future);
    }

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
        P2P_UDPServer server = new P2P_UDPServer();
        server.createAndListenSocket();
    }
}
