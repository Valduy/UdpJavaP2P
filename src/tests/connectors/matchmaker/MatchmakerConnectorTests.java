package tests.connectors.matchmaker;

import com.company.server.matchmakers.Matchmaker;
import com.company.server.matchmakers.MatchmakerException;
import connectors.ConnectorException;
import connectors.matchmaker.MatchmakerConnector;
import org.junit.jupiter.api.*;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MatchmakerConnectorTests {
    private static DatagramSocket client1;
    private static DatagramSocket client2;
    private static Matchmaker matchmaker;
    private static MatchmakerConnector connector1;
    private static MatchmakerConnector connector2;
    private static volatile Boolean[] invocations;

    @BeforeAll
    public static void setUp() throws SocketException, MatchmakerException {
        matchmaker = new Matchmaker(2);
        matchmaker.start();
        client1 = new DatagramSocket();
        client2 = new DatagramSocket();
        connector1 = new MatchmakerConnector();
        connector2 = new MatchmakerConnector();
    }

    @AfterAll
    public static void finish() throws ConnectorException {
        connector1.stop();
        connector2.stop();
        client1.close();
        client2.close();
    }

    @Test
    @Order(1)
    public void startTest() throws ConnectorException {
        invocations = new Boolean[] {false, false};
        connector1.addFound((o, e) -> invocations[0] = true);
        connector2.addFound((o, e) -> invocations[1] = true);
        connector1.start(client1, InetAddress.getLoopbackAddress(), matchmaker.getPort());
        connector2.start(client2, InetAddress.getLoopbackAddress(), matchmaker.getPort());
        while (Arrays.stream(invocations).allMatch(i -> i)) Thread.onSpinWait();
    }

    @Test
    @Order(2)
    public void resultTest() {
        assertEquals(connector1.getMatchPort(), connector2.getMatchPort());
    }

    @Test
    @Order(3)
    public void stopTest() throws ConnectorException, InterruptedException {
        connector1.start(client1, InetAddress.getLoopbackAddress(), matchmaker.getPort());
        Thread.sleep(1000);
        connector1.stop();
    }
}
