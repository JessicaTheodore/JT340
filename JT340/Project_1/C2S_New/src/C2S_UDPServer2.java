package Project_1.C2S_New.src;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class C2S_UDPServer2 {

    private static final int TIMEOUT_SECONDS = 30;
    private DatagramSocket socket;
    private int serverPort;
    
    private final HashMap<InetAddress, Integer> clientPorts = new HashMap<>();
    private final HashMap<InetAddress, String> clientFiles = new HashMap<>();
    private final ConcurrentHashMap<InetAddress, ScheduledFuture<?>> clientTimers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public C2S_UDPServer2() {
        loadConfig();
        try {
            socket = new DatagramSocket(serverPort);
            System.out.println("Server started on port: " + serverPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    // Load server configuration from Server.txt
    private void loadConfig() {
        try (FileInputStream fis = new FileInputStream("Server.txt")) {
            Properties prop = new Properties();
            prop.load(fis);
            this.serverPort = Integer.parseInt(prop.getProperty("port").trim());
        } catch (IOException e) {
            System.err.println("Error reading Server.txt: " + e.getMessage());
        }
    }

    public void createAndListenSocket() {
        byte[] incomingData = new byte[1024];

        while (true) {
            try {
                // Receive data packet
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                socket.receive(incomingPacket);
                
                // Extract client details
                InetAddress clientAddress = incomingPacket.getAddress();
                int clientPort = incomingPacket.getPort();
                String message = new String(incomingPacket.getData(), 0, incomingPacket.getLength());

                // Update client record
                clientPorts.put(clientAddress, clientPort);
                clientFiles.put(clientAddress, parseFileList(message));
                System.out.println("Received update from " + clientAddress);

                // Reset failure detection timer
                resetTimeout(clientAddress);

                // Broadcast the updated client list
                broadcastClientStatus();

                // Send acknowledgment response
                sendResponse(clientAddress, clientPort, "Update received.");

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

    // Reset timeout for client failure detection
    private void resetTimeout(InetAddress clientAddress) {
        if (clientTimers.containsKey(clientAddress)) {
            clientTimers.get(clientAddress).cancel(false);
        }

        Runnable timeoutTask = () -> {
            System.out.println("Client " + clientAddress + " has timed out!");
            clientPorts.remove(clientAddress);
            clientFiles.remove(clientAddress);
            clientTimers.remove(clientAddress);
            broadcastClientStatus();
        };

        ScheduledFuture<?> future = scheduler.schedule(timeoutTask, TIMEOUT_SECONDS, TimeUnit.SECONDS);
        clientTimers.put(clientAddress, future);
    }

    // Send client status to all connected nodes
    private void broadcastClientStatus() {
        StringBuilder statusMessage = new StringBuilder("Current Live Nodes:\n");
        for (InetAddress client : clientPorts.keySet()) {
            statusMessage.append(client).append(" | Files: ").append(clientFiles.get(client)).append("\n");
        }

        byte[] data = statusMessage.toString().getBytes();
        for (InetAddress client : clientPorts.keySet()) {
            try {
                DatagramPacket packet = new DatagramPacket(data, data.length, client, clientPorts.get(client));
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Send a response to the client
    private void sendResponse(InetAddress clientAddress, int clientPort, String message) {
        try {
            byte[] responseData = message.getBytes();
            DatagramPacket replyPacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
            socket.send(replyPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        C2S_UDPServer2 server = new C2S_UDPServer2();
        server.createAndListenSocket();
    }
}
