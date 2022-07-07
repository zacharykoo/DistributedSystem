import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class Iteration3 {
    //Set port number
    public static final int DEFAULT_PORT_NUMBER = 55921;
    private static BufferedWriter responses;
    private static BufferedReader input;
    private static ArrayList<String> peers = new ArrayList<String>(); //List for the list of peers
    private String command = ""; //Command from server
    private int peersNumber = 0;
    private int numOfSource = 0;
    static Socket sock;
    private static DatagramSocket peersCommunicationSock;
    public static ReceiveMessage receiveMessageFromPeers;
    static SendMessage sendMessageToPeers;
    static AckThread ackListen;

    static SendLocationToPeers sendLocationToPeers;
    String address;
    String addrPort;
    int communicationPort;

    //Main function to start the program
    public static void main(String[] args) throws InterruptedException {
        Iteration3 client = new Iteration3();
        try {
            String teamName = args[0];
            String port = args[1];
            client.registryCommunication(teamName, port);
            System.out.println("Start peer communications \n");
            client.initiateThreads(teamName);
            client.peerCommunication();
            while(receiveMessageFromPeers.getStatus()) {
                Thread.sleep(5000);
            }
            sendMessageToPeers.run = false;
            client.registryCommunication(teamName, port);
            System.exit(0);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            sendLocationToPeers.close();
            sendMessageToPeers.close();
            receiveMessageFromPeers.close();
        }
    }

    public void initiateThreads(String teamName) throws IOException {
        peersCommunicationSock = new DatagramSocket(sock.getLocalPort());
        receiveMessageFromPeers = new ReceiveMessage(peersCommunicationSock, peers, teamName);
        //ackListen = new AckThread(peersCommunicationSock, peers, teamName);
        addrPort = address + ":" + peersCommunicationSock.getLocalPort() + "\n";
        System.out.println(addrPort + "\n");
        sendMessageToPeers = new SendMessage(peers, addrPort);
        sendLocationToPeers = new SendLocationToPeers(peers, teamName);
        FileWriter myWriter = new FileWriter("timer.txt");
        myWriter.write("0");
        myWriter.close();
        myWriter = new FileWriter("CommunicationLog.txt");
        myWriter.close();
    }

    public void peerCommunication() throws SocketException {
        receiveMessageFromPeers.start();
        sendLocationToPeers.sendLocation();
        sendMessageToPeers.start();
        //ackListen.start();
    }

    public void sendMessage(String message) throws IOException {
        responses.write(message); //Respond team name
        responses.flush();
    }

    public void sendTeamName(String teamname) throws IOException {
        System.out.println("get teamname requested!\n");
        sendMessage(teamname + "\n"); //Respond team name
    }

    public void sendCode () throws IOException {
        System.out.println("get code requested!\n");
        
        StringBuilder code = new StringBuilder();

        //Read file line by line
        File fileName = new File("CodeToSend.txt");
        Scanner readStreams = new Scanner(fileName);
        while (readStreams.hasNextLine()) {
            code.append(readStreams.nextLine() + "\n");
        }
        readStreams.close();
        //End of code
        sendMessage("Java\n" + code.toString() + "...\n");
    }

    public void receivedPeers() throws IOException {
        System.out.println("peer receive requested!\n");
        //Number of sources increases
        numOfSource ++;
        peers.clear(); //Remove old list for new list
        //record number of peers
        peersNumber = Integer.parseInt(input.readLine());
        //Put peers into a list
        for (int i = 0; i < peersNumber; i++) {
            command = input.readLine();
            peers.add(command);
        }
    }

    public String getPeersInformation() {
        StringBuilder peersMessage = new StringBuilder();
        //number of peers
        peersMessage.append(Integer.toString(peersNumber) + "\n");
        //Peers list
        for (int i = 0; i < peersNumber; i++) {
            peersMessage.append(peers.get(i) + "\n");
        }
        return peersMessage.toString();
    }

    public String sourceMessage() {
        StringBuilder sourceMessage = new StringBuilder();
        //Number of source
        sourceMessage.append(Integer.toString(numOfSource) + "\n");

        //Sources
        //Source location
        sourceMessage.append(sock.getLocalAddress().getHostAddress() + ":" + sock.getPort() + "\n");
        
        //Date
        Date date = new Date();
        SimpleDateFormat formating = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sourceMessage.append(formating.format(date) + "\n");

        return sourceMessage.toString();
    }

    public String peerInformation() {
        StringBuilder peersMessage = new StringBuilder();
        //Peers information
        peersMessage.append(Integer.toString(peersNumber) + "\n");
        for (int i = 0; i < peersNumber; i++) {
            peersMessage.append(peers.get(i) + "\n");
        }

        return peersMessage.toString();
    }

    public String getSnipMessages() throws IOException {
        StringBuilder snipMessage = new StringBuilder();
        int lines = 0;
        BufferedReader reader = new BufferedReader(new FileReader("CommunicationLog.txt"));
        while (reader.readLine() != null) lines++;
        snipMessage.append(Integer.toString(lines) + "\n");
        File fileName = new File("CommunicationLog.txt");
        Scanner readStreams = new Scanner(fileName);
        while (readStreams.hasNextLine()) {
            snipMessage.append(readStreams.nextLine() + "\n");
        }

        return snipMessage.toString();
    }

    public void sendReport() throws IOException {
        System.out.println("get report requested!\n");
        String report = "";
        report += getPeersInformation();
        report += sourceMessage();
        report += peerInformation();
        report += sendLocationToPeers.getPeerMessage();
        report += receiveMessageFromPeers.getPeersReceived();
        report += getSnipMessages();
        System.out.println(report);
        sendMessage(report);
    }

    public void sendLocation() throws IOException {
        System.out.println("get Location requested!\n");
        address = sock.getLocalAddress().toString().substring(1);
        communicationPort = sock.getLocalPort();
        sendMessage(address + ":" + communicationPort + "\n");
    }

    public void sendAck(String teamName) throws IOException {
        sendMessage("ack"+teamName + "\n");
        System.out.println("ack sent!\n");
    }

    //Start function
    public void registryCommunication(String teamname, String port) throws IOException {
        try {
            //Create socket and connect
            sock = new Socket("localhost", Integer.parseInt(port));
            System.out.println(teamname + " " + port);
            //Buffer for reading input from server
            input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            responses = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            boolean run = true;

            //Listen for commands
            while (run) {
                //Read input command from server
                command = input.readLine();
                //If command is get team name
                if (command.equals("get team name")) {
                    sendTeamName(teamname);
                //If command is get code
                } else if (command.equals("get code")) {
                    sendCode();
                //If command is receive peers
                } else if (command.equals("receive peers")) {
                    receivedPeers();
                    continue;
                //If command is get report
                } else if (command.equals("get report")) {
                    sendReport();
                //if command is close
                } else if (command.equals("get location")) {
                    sendLocation();
                } else if (command.equals("close")) {
                    System.out.println("Stop request!\n");
                    sock.close();
                    input.close();
                    responses.close();
                    run = false;
                    return;
                }
            }
        } catch (IOException ioe) {
            System.out.println("Something wrong with responses or command receieve. \n");
            ioe.printStackTrace();
        }
    }
}
