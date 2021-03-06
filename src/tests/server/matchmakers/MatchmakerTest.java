package tests.server.matchmakers;

import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.company.network.UserStatus;
import com.company.server.matchmakers.Matchmaker;
import com.company.server.matchmakers.MatchmakerException;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MatchmakerTest {
    private static int timePerAction;
    private static Matchmaker matchmaker;
    private static DatagramSocket client;

    @BeforeAll
    public static void setUp() throws SocketException, MatchmakerException {
        timePerAction = 10 * 1000;
        matchmaker = new Matchmaker(2);
        matchmaker.start();
        client = new DatagramSocket();
        var socketTimeout = 1000;
        client.setSoTimeout(socketTimeout);
    }

    @AfterAll
    public static void finish() throws MatchmakerException {
        matchmaker.stop();
        client.close();
    }

    @Test
    @Order(1)
    public void helloTest() throws IOException, TimeoutException {
        var message = MessageHelper.getMessage(NetworkMessages.HLLO);
        var response = timeOutSendReceive(client, message, NetworkMessages.size);
        assertEquals(MessageHelper.getMessageType(response), NetworkMessages.HLLO);
    }

    @Test
    @Order(2)
    public void checkClientInQueueTest(){
        assertEquals(matchmaker.getQueue().size(), 1);
        var first = matchmaker.getQueue().stream().findFirst().get();
        assertEquals(first.port, client.getLocalPort());
    }

    @Test
    @Order(3)
    public void infoClientInQueueTest() throws IOException, TimeoutException {
        assertEquals(getUserStatus(client), UserStatus.WAIT);
    }

    @Test
    @Order(4)
    public void byeClientTest() throws IOException, TimeoutException {
        var message = MessageHelper.getMessage(NetworkMessages.GBYE);
        var response = timeOutSendReceive(client, message, NetworkMessages.size);
        assertEquals(MessageHelper.getMessageType(response), NetworkMessages.GBYE);
    }

    @Test
    @Order(5)
    public void checkClientQueueEmptiness(){
        assertEquals(matchmaker.getQueue().size(), 0);
    }

    @Test
    @Order(6)
    public void infoClientIsAbsentTest() throws IOException, TimeoutException {
        assertEquals(getUserStatus(client), UserStatus.ABSN);
    }

    @Test
    @Order(7)
    public void matchCreationTest() throws IOException, TimeoutException {
        try (var otherClient = new DatagramSocket()) {
            var message = MessageHelper.getMessage(NetworkMessages.HLLO);
            timeOutSendReceive(client, message, NetworkMessages.size);
            timeOutSendReceive(otherClient, message, NetworkMessages.size);
            waitConnection(client);
            waitConnection(otherClient);
            message = MessageHelper.getMessage(NetworkMessages.INIT);
            var responseSize = NetworkMessages.size + Integer.BYTES;
            var forClient = timeOutSendReceive(client, message, responseSize);
            var forOtherClient = timeOutSendReceive(otherClient, message, responseSize);
            var portForClient = ByteBuffer.wrap(forClient).getInt();
            var portForOtherClient = ByteBuffer.wrap(forOtherClient).getInt();
            assertEquals(portForClient, portForOtherClient);
        }
    }

    private byte[] timeOutSendReceive(DatagramSocket socket, byte[] message, int bufferSize)
            throws IOException, TimeoutException
    {
        var endTime = System.currentTimeMillis() + timePerAction;
        var sended = new DatagramPacket(message, message.length, InetAddress.getLoopbackAddress(), matchmaker.getPort());
        var receive = new byte[bufferSize];
        var received = new DatagramPacket(receive, receive.length);

        while (true){
            socket.send(sended);

            try{
                socket.receive(received);
                return received.getData();
            }catch (SocketTimeoutException e){
                if (System.currentTimeMillis() > endTime){
                    throw new TimeoutException();
                }
            }
        }
    }

    private UserStatus getUserStatus(DatagramSocket socket) throws IOException, TimeoutException {
        var message = MessageHelper.getMessage(NetworkMessages.INFO);
        var responseLength = NetworkMessages.size + UserStatus.size;
        var response = timeOutSendReceive(socket, message, responseLength);
        assertEquals(MessageHelper.getMessageType(response), NetworkMessages.INFO);
        var data = MessageHelper.toString(response);
        return UserStatus.valueOf(data);
    }

    private void waitConnection(DatagramSocket socket) throws IOException, TimeoutException {
        var endTime = System.currentTimeMillis() + timePerAction;

        while (getUserStatus(socket) != UserStatus.CONN){
            if (System.currentTimeMillis() > endTime){
                throw new TimeoutException();
            }
        }
    }
}
