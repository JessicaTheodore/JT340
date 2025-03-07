
package Project_1.P2P_New.src;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class P2P_UDPClient2 {

    private DatagramSocket socket;
    private List<InetAddress> peerIps;
    private int peerPort;
    private String directoryPath;
    private Scanner in;

    public P2P_UDPClient2() {
        loadConfig();
        in = new Scanner(System.in);
    }

    // Load configuration from P2Pconfig.txt
    private void loadConfig() {
        try (FileInputStream fis = new FileInputStream("P2Pconfig.txt")) {
            Properties prop = new Properties();
            prop.load(fis);

            this.peerPort = Integer.parseInt(prop.getProperty("port").trim());
            this.directoryPath = prop.getProperty("directory_path").trim();

            // Parse peer IPs
            String[] ips = prop.getProperty("peer_ips").split(",");
            peerIps = new ArrayList<>();
            for (String ip : ips) {
                peerIps.add(InetAddress.getByName(ip.trim()));
            }

            System.out.println("Loaded Config - Peer IPs: " + peerIps + ", Port: " + peerPort);
        } catch (IOException e) {
            System.err.println("Error reading P2Pconfig.txt: " + e.getMessage());
        }
    }

    public void createAndListenSocket() {
        try {
            socket = new DatagramSocket();
            byte[] incomingData = new byte[1024];

            char choice = 'y';

            while (choice == 'y' || choice == 'Y') {
                System.out.println("Enter your message:");
                String message = in.nextLine();
                
                if (message.equalsIgnoreCase("files")) {
                    message = "Files: " + getFileListing(directoryPath);
                }

                byte[] data = message.getBytes();

                // Send packet to all peers
                for (InetAddress peerIp : peerIps) {
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, peerIp, peerPort);
                    socket.send(sendPacket);
                    System.out.println("Message sent to peer: " + peerIp);
                }

                // Receive response
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                socket.receive(incomingPacket);
                String response = new String(incomingPacket.getData(), 0, incomingPacket.getLength());

                System.out.println("Response from peer: " + response);

                // Ask if user wants to continue
                System.out.println("Chat more? Y/N...");
                choice = in.nextLine().charAt(0);
            }

            System.out.println("Client shutting down.");
            socket.close();

        } catch (IOException e) {
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

    public static void main(String[] args) {
        P2P_UDPClient2 client = new P2P_UDPClient2();
        client.createAndListenSocket();
    }
}
