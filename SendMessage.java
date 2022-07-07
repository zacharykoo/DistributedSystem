import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

public class SendMessage extends Thread{
    ArrayList<String> peers;
    int timestamp = 0;
    String content;
    DatagramSocket sock;
    Scanner keyboard = new Scanner(System.in);
    String[] temp;
    String addrPort, address;
    int port;
    String line, last;
    boolean run;
    
    public SendMessage(ArrayList<String> peers, String addrPort) throws SocketException {
        this.peers = peers;
        this.addrPort = addrPort;
        run = true;
        sock = new DatagramSocket();
    }

    public SendMessage(ArrayList<String> peers, String addrPort, int timestamp) throws SocketException {
        this.peers = peers;
        this.addrPort = addrPort;
        if (timestamp > this.timestamp) {
            this.timestamp = timestamp;
        }
        sock = new DatagramSocket();
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void close() {
        sock.close();
    }

    public void run() {
        while(run) {
            String message = keyboard.nextLine();
            try (BufferedReader timeNow = new BufferedReader(new FileReader("timer.txt"))) {
                while ((line = timeNow.readLine()) != null) { 
                    last = line;
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            int tempTime = Integer.parseInt(last);
            if (timestamp < tempTime) {
                timestamp = tempTime;
            }
            content = "snip" + timestamp + " " + message + " " + addrPort;
            byte[] packet = content.getBytes();
            try {
                for (int i = 0; i < peers.size(); i++ ) {
                    temp = peers.get(i).split(":");
                    address = temp[0];
                    port = Integer.parseInt(temp[1]);
                    DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, InetAddress.getByName(address), port);
                    sock.send(packetToSend);
                }
                timestamp += 1;
                FileWriter myWriter = new FileWriter("timer.txt");
                myWriter.write(Integer.toString(timestamp) + "\n");
                myWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
