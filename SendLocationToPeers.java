import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class SendLocationToPeers{
    ArrayList<String> peers;
    int timestamp;
    String content;
    DatagramSocket sock;
    String teamName;
    String[] temp;
    String address;
    int port;
    ArrayList<String> peerSent;

    public SendLocationToPeers(ArrayList<String> peers, String teamName) throws SocketException {
        this.peers = peers;
        this.teamName = teamName;
        peerSent = new ArrayList<String> ();
        sock = new DatagramSocket();
    }

    public void close() {
        sock.close();
    }

    public void sendLocation() {
        for (int i = 0; i < peers.size(); i++) {
            content = "peer " + peers.get(i);
            String[] temp;
            byte[] packet = content.getBytes();
            try {
                for (int j = 0; j < peers.size(); j++ ) {
                    temp = peers.get(j).split(":");
                    address = temp[0];
                    port = Integer.parseInt(temp[1]);
                    DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, InetAddress.getByName(address), port);
                    sock.send(packetToSend);
                    peerSent.add(peers.get(i) + " " + LocalDateTime.now());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getPeerMessage() {
        StringBuilder peerMessage = new StringBuilder(peerSent.size() + "\n");
        for (int i = 0; i < peerSent.size(); i++) {
            peerMessage.append(peerSent.get(i) + "\n");
        }
        return peerMessage.toString();
    }
}
