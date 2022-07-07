import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

public class ReceiveMessage extends Thread {
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

    public ReceiveMessage(DatagramSocket sock, ArrayList<String> peers, String teamname) {
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
            run = false;
        } else if (type.equals("snip")) {
            timestamp = Integer.parseInt(message[0].substring(4));
            BufferedReader timeNow = new BufferedReader(new FileReader("timer.txt"));
            while ((line = timeNow.readLine()) != null) { 
                last = line;
            }
            if (last != null) {
                tempTime = Integer.parseInt(last);
            } else {
                tempTime = 0;
            }
            
            if (timestamp < tempTime) {
                timestamp = tempTime;
            }
            String fullmessage = timestamp + " " + message[1];
            PrintWriter myWriter = new PrintWriter(new FileWriter("CommunicationLog.txt", true));
            myWriter.append(fullmessage);
            myWriter.close();
            File fileName = new File("CommunicationLog.txt");
            Scanner readStreams = new Scanner(fileName);
            while (readStreams.hasNextLine()) {
                System.out.println(readStreams.nextLine());
            }
            System.out.println("\n");
            readStreams.close();
        } else if (type.equals("peer")) {
            if (!peers.contains(message[1])){
                System.out.println("Peer added! \n");
                peers.add(message[1]);
                peersReceived.add(message[1] + " " + LocalDateTime.now());
            }
        } else if (type.equals("ack ")) {

        }
    }

    public String getPeersReceived() {
        StringBuilder peerMessage = new StringBuilder(peersReceived.size() + "\n");
        for (int i = 0; i < peersReceived.size(); i++) {
            peerMessage.append(peersReceived.get(i) + "\n");
        }
        return peerMessage.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean getStatus() {
        return run;
    }

    public void close() {
        sock.close();
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
