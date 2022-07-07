import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class AckThread extends Thread{
    DatagramSocket sock;
    DatagramPacket message;
    ArrayList<String> peers;
    boolean run = true;
    long timestamp;
    long startTime = System.currentTimeMillis();
    long estimatedTime;
    int tempTime;
    String last, line;
    ArrayList<String> peersReceived;
    String teamName;

    public AckThread(DatagramSocket sock, ArrayList<String> peers, String teamname) {
        this.sock = sock;
        this.peers = peers;
        this.teamName = teamname;
        peersReceived = new ArrayList<String>();
    }

    public void sendAck(SenderInfo senderInfo, String teamname) throws IOException {
        String ackMessage = "ack" + teamname;
        byte[] packet = ackMessage.getBytes();
        DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, 
            InetAddress.getByName(senderInfo.address), senderInfo.port);
        sock.send(packetToSend);
    }

    public void readPeerMessages () throws IOException {
        byte[] receiveData = new byte[1024];
        DatagramPacket peersInput = new DatagramPacket(receiveData, receiveData.length);
        sock.receive(peersInput);

        SenderInfo sender = new SenderInfo(peersInput.getAddress().getHostAddress(), peersInput.getPort());

        String[] message = new String(peersInput.getData(), 0, peersInput.getLength()).split(" ", 2);
        String type = message[0].substring(0, 4);
        if (type.equals("stop")) {
            sendAck(sender, teamName);
        }
    }
    
    public void run() {
        while(run) {
            try {
                readPeerMessages();
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout!\n");
            } catch (Exception e) {
                e.printStackTrace();
            } 
        }
    }
}
