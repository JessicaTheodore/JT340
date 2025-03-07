package Project_1.P2P_New.src;

import java.net.InetAddress;
import java.io.Serializable;

public class P2P_Protocol implements Serializable {

    private static final long serialVersionUID = 1L;

    // Control Info
    private int version = 1;
    private int length;
    private int flags;
    private int reserved;

    // Data
    private double timeStamp;
    private int port;
    private InetAddress ip;
    private int destPort;
    private InetAddress destIp;
    private String nodeName;
    private String fileList;

    public P2P_Protocol() {}

    // Getters
    public int getVersion() { return version; }
    public int getLength() { return length; }
    public int getFlags() { return flags; }
    public int getReserved() { return reserved; }
    public double getTimeStamp() { return timeStamp; }
    public int getPort() { return port; }
    public InetAddress getIp() { return ip; }
    public int getDestPort() { return destPort; }
    public InetAddress getDestIp() { return destIp; }
    public String getNodeName() { return nodeName; }
    public String getFileList() { return fileList; }

    // Setters
    public void setLength(int length) { this.length = length; }
    public void setFlags(int flags) { this.flags = flags; }
    public void setReserved(int reserved) { this.reserved = reserved; }
    public void setTimeStamp(double timeStamp) { this.timeStamp = timeStamp; }
    public void setPort(int port) { this.port = port; }
    public void setIp(InetAddress ip) { this.ip = ip; }
    public void setDestPort(int destPort) { this.destPort = destPort; }
    public void setDestIp(InetAddress destIp) { this.destIp = destIp; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public void setFileList(String fileList) { this.fileList = fileList; }

    // Convert packet to formatted string
    public String toPacketString() {
        return "V:" + version + "|L:" + length + "|F:" + flags + "|R:" + reserved + "|" +
               "TS:" + timeStamp + "|IP:" + ip + "|Port:" + port + "|DestIP:" + destIp + "|DestPort:" + destPort + 
               "|Node:" + nodeName + "|Files:" + fileList;
    }
}
