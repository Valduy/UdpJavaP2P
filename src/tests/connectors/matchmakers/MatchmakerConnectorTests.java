package tests.connectors.matchmakers;

import com.company.server.matchmakers.Matchmaker;
import com.company.server.matchmakers.MatchmakerException;
import connectors.ConnectorException;
import connectors.matchmakers.MatchmakerConnector;
import org.junit.jupiter.api.*;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MatchmakerConnectorTests {
    private static DatagramSocket client1;
    private static DatagramSocket client2;
    private static Matchmaker matchmaker;
    private static MatchmakerConnector connector1;
    private static MatchmakerConnector connector2;
    private static Future<?> future;

    @BeforeAll
    public static void setUp() throws SocketException, MatchmakerException {
        matchmaker = new Matchmaker(2, 54321);
        var executor = Executors.newSingleThreadExecutor();
        future = executor.submit(matchmaker);
        client1 = new DatagramSocket();
        client2 = new DatagramSocket();
        connector1 = new MatchmakerConnector();
        connector2 = new MatchmakerConnector();
    }

    @AfterAll
    public static void finish() throws ConnectorException, ExecutionException, InterruptedException {
        connector1.stop();
        connector2.stop();
        matchmaker.cancel();
        future.get();
        client1.close();
        client2.close();
    }

    @Test
    @Order(1)
    public void startTest() throws ConnectorException {
        var invocations = new Boolean[] {false, false};
        connector1.addConnected((o, e) -> invocations[0] = true);
        connector2.addConnected((o, e) -> invocations[1] = true);
        connector1.start(client1, InetAddress.getLoopbackAddress(), matchmaker.getPort());
        connector2.start(client2, InetAddress.getLoopbackAddress(), matchmaker.getPort());
        while (!Arrays.stream(invocations).allMatch(i -> i)) Thread.onSpinWait();
    }

    @Test
    @Order(2)
    public void resultTest() throws ConnectorException {
        assertEquals(connector1.getResult(), connector2.getResult());
    }

    @Test
    @Order(3)
    public void stopTest() throws ConnectorException, InterruptedException {
        connector1.start(client1, InetAddress.getLoopbackAddress(), matchmaker.getPort());
        Thread.sleep(1000);
        connector1.stop();
    }
}
