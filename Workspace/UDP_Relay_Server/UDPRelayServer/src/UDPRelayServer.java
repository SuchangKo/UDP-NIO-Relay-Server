import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * Created by SuchangKo on 14. 12. 23..
 */
public class UDPRelayServer {
    int port_1 = 10000;
    int port_2 = 10001;
    static int BUF_SZ = 1024;
    static SocketAddress socketAddress[] = new SocketAddress[]{null,null};
    public static void main(String args[]){
        UDPRelayServer udpRelayServer = new UDPRelayServer();
        udpRelayServer.process();
    }

    public class Con {
        ByteBuffer req;
        ByteBuffer resp;
        SocketAddress sa;

        public Con(int BUF_SZ) {
            req = ByteBuffer.allocate(BUF_SZ);
        }
    }

    private void process() {
        try {
            final Selector selectors[] = new Selector[2];
            DatagramChannel channels[] = new DatagramChannel[2];
            InetSocketAddress isas[] = new InetSocketAddress[2];

            isas[0] = new InetSocketAddress(port_1);
            isas[1] = new InetSocketAddress(port_2);
            final SelectionKey clientKeys[] = new SelectionKey[2];
            for(int i = 0 ; i < 2 ; i++){
                channels[i] = DatagramChannel.open();
                channels[i].socket().bind(isas[i]);
                channels[i].configureBlocking(false);
                selectors[i]  = Selector.open();
                clientKeys[i] = channels[i].register(selectors[i],SelectionKey.OP_READ);
                clientKeys[i].attach(new Con(BUF_SZ));

            }

            Thread threads[] = new Thread[2];
            for(int i = 0; i < 2 ; i++){
                final int index = i;
                threads[i] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                selectors[index].select();
                                Iterator selectedKeys = selectors[index].selectedKeys().iterator();
                                while (selectedKeys.hasNext()) {
                                    try {
                                        SelectionKey key = (SelectionKey) selectedKeys.next();
                                        selectedKeys.remove();
                                        if (!key.isValid()) {
                                            continue;
                                        }
                                        if (key.isReadable()) {
                                            read(key,index);
                                            key.interestOps(SelectionKey.OP_WRITE);
                                        } else if (key.isWritable()) {
                                            if(socketAddress[index == 0 ? 1 : 0] != null) {
                                                write(key, clientKeys[index == 0 ? 1 : 0], socketAddress[index == 0 ? 1 : 0]);
                                            }
                                            key.interestOps(SelectionKey.OP_READ);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                threads[i].start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key,int index) throws IOException {
        DatagramChannel chan = (DatagramChannel)key.channel();
        Con con = (Con)key.attachment();
        con.sa = chan.receive(con.req);
        socketAddress[index] = con.sa;
        String messageFromClient = new String(con.req.array(), "UTF-8");
        con.resp = Charset.forName("UTF-8").newEncoder().encode(CharBuffer.wrap(messageFromClient));
    }

    private void write(SelectionKey recv_key,SelectionKey dest_key, SocketAddress sa) throws IOException {
        if(sa != null) {
            //relay
            DatagramChannel chan = (DatagramChannel) dest_key.channel();
            Con con = (Con) recv_key.attachment();
            chan.send(con.resp, sa);
            recv_key.attach(new Con(BUF_SZ));
        }else{
            //echo
            DatagramChannel chan = (DatagramChannel)recv_key.channel();
            Con con = (Con)recv_key.attachment();
            chan.send(con.resp, con.sa);
            recv_key.attach(new Con(BUF_SZ));
        }
    }

}
