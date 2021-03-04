package tests.server;

import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.company.server.Matchmaker;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.*;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MatchmakerTest {
    private static int timePerAction;
    private static Matchmaker matchmaker;
    private static DatagramSocket client;

    @BeforeAll
    public static void setUp() throws SocketException {
        timePerAction = 10 * 1000;
        matchmaker = new Matchmaker(2);
        matchmaker.start();
        client = new DatagramSocket();
        client.setSoTimeout(1000);
    }

    @AfterAll
    public static void finish() throws InterruptedException {
        matchmaker.stop();
        client.close();
    }

    @Test
    @Order(1)
    public void helloTest() throws IOException, TimeoutException {
        var message = MessageHelper.getMessage(NetworkMessages.HLLO);
        var response = timeOutSendReceive(message, NetworkMessages.size);
        assertEquals(MessageHelper.getMessageType(response), NetworkMessages.HLLO);
    }

    @Test
    @Order(2)
    public void checkSingleClientInQueueTest(){
        assertEquals(matchmaker.getQueue().size(), 1);
        var first = matchmaker.getQueue().stream().findFirst().get();
        assertEquals(first.port, client.getLocalPort());
    }

//    public void infoTest(){
//
//    }

    private byte[] timeOutSendReceive(byte[] message, int bufferSize) throws IOException, TimeoutException {
        var endTime = System.currentTimeMillis() + timePerAction;
        var sended = new DatagramPacket(message, message.length, InetAddress.getLoopbackAddress(), matchmaker.getPort());
        var receive = new byte[bufferSize];
        var received = new DatagramPacket(receive, receive.length);

        while (true){
            client.send(sended);

            try{
                client.receive(received);
                return received.getData();
            }catch (SocketTimeoutException e){
                if (System.currentTimeMillis() > endTime){
                    throw new TimeoutException();
                }
            }
        }
    }
}
