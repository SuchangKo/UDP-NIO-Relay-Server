import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by SuchangKo on 14. 12. 23..
 */
public class UDPClient
{
    public static void main(String args[]) throws Exception
    {



        System.out.println("INPUT port :");

        final BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader
                        (System.in));
        int tmp_portNumber = Integer.parseInt(inFromUser.readLine());
        final int portNumber = tmp_portNumber;
        System.out.println("INPUT something to be sent to server:");
        final DatagramSocket clientSocket = new DatagramSocket();
        final InetAddress IPAddress =
                InetAddress.getByName("192.168.0.102");

        final byte[][] sendData = {new byte[1024]};
        final byte[] receiveData = new byte[1024];

        System.out.println("[Send] "+"start");
        sendData[0] = "start".getBytes();
        DatagramPacket tmp_sendPacket =
                new DatagramPacket(sendData[0], sendData[0].length,
                        IPAddress, portNumber);

        clientSocket.send(tmp_sendPacket);

        Thread readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        DatagramPacket receivePacket =
                                new DatagramPacket(receiveData,
                                        receiveData.length);
                        clientSocket.receive(receivePacket);
                        String modifiedSentence =
                                new String(receivePacket.getData());
                        System.out.println("FROM SERVER:" +
                                modifiedSentence);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        Thread writeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {


                        String sentence = inFromUser.readLine();
                        System.out.println("[Send] " + sentence);
                        sendData[0] = sentence.getBytes();
                        DatagramPacket sendPacket =
                                new DatagramPacket(sendData[0], sendData[0].length,
                                        IPAddress, portNumber);

                        clientSocket.send(sendPacket);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        readThread.start();
        writeThread.start();
    }
}