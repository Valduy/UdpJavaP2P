package tests.server.matches;

import com.company.network.*;
import com.company.server.matches.MatchException;
import com.company.server.matches.Match;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MatchTest {
    private static int timePerAction;
    private static Match match;
    private static Future<?> future;
    private static DatagramSocket client1;
    private static DatagramSocket client2;
    private static byte[] message1;
    private static byte[] message2;
    private static Gson gson;

    @BeforeAll
    private static void setUp() throws SocketException, MatchException {
        timePerAction = 10 * 1000;
        gson = new Gson();
        match = new Match(2, 30 * 1000);
        var executor = Executors.newSingleThreadExecutor();
        future = executor.submit(match);
        client1 = new DatagramSocket();
        client2 = new DatagramSocket();
        message1 = getHelloMessage(client1);
        message2 = getHelloMessage(client2);
    }

    @AfterAll
    private static void finish() throws ExecutionException, InterruptedException {
        match.cancel();
        future.get();
        client1.close();
        client2.close();
    }

    @Test
    @Order(1)
    public void helloTest() throws IOException, TimeoutException {
        var response1 = timeOutSendReceive(client1, message1, NetworkMessages.size);
        assertEquals(MessageHelper.getMessageType(response1), NetworkMessages.HLLO);

        var response2 = timeOutSendReceive(client2, message2, NetworkMessages.size);
        assertEquals(MessageHelper.getMessageType(response2), NetworkMessages.HLLO);
    }

    @Test
    @Order(2)
    public void stateChangingTest() throws IOException, TimeoutException {
        waitInitial(client1, message1, 512);
    }

    @Test
    @Order(3)
    public void connectionTest() throws IOException, TimeoutException {
        var response1 = timeOutSendReceive(client1, message1, 512);
        assertEquals(MessageHelper.getMessageType(response1), NetworkMessages.INIT);
        var data1 = MessageHelper.toString(response1);
        var reader1 = new JsonReader(new StringReader(data1));
        reader1.setLenient(true);
        P2PConnectionMessage connectionMessage1 = gson.fromJson(reader1, P2PConnectionMessage.class);

        var response2 = timeOutSendReceive(client1, message2, 512);
        assertEquals(MessageHelper.getMessageType(response2), NetworkMessages.INIT);
        var data2 = MessageHelper.toString(response2);
        var reader2 = new JsonReader(new StringReader(data2));
        reader2.setLenient(true);
        P2PConnectionMessage connectionMessage2 = gson.fromJson(reader2, P2PConnectionMessage.class);
    }

    private byte[] timeOutSendReceive(DatagramSocket socket, byte[] message, int bufferSize)
            throws IOException, TimeoutException
    {
        var endTime = System.currentTimeMillis() + timePerAction;
        var sended = new DatagramPacket(message, message.length, InetAddress.getLoopbackAddress(), match.getPort());
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

    private static byte[] getHelloMessage(DatagramSocket socket){
        var endPoint = new EndPoint(socket.getLocalAddress(), socket.getLocalPort());
        var data = gson.toJson(endPoint);
        return MessageHelper.getMessage(NetworkMessages.HLLO, data);
    }

    private void waitInitial(DatagramSocket socket, byte[] message, int bufferSize)
            throws IOException, TimeoutException
    {
        var endTime = System.currentTimeMillis() + timePerAction;

        while (true){
            var received = timeOutSendReceive(socket, message, bufferSize);

            if (MessageHelper.getMessageType(received) == NetworkMessages.INIT){
                return;
            }

            if (System.currentTimeMillis() > endTime){
                throw new TimeoutException();
            }
        }
    }
}
