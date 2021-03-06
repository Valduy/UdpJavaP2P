package tests.connectors.matchmaker;

import com.company.server.Matchmaker;
import connectors.matchmaker.MatchmakerConnector;
import connectors.matchmaker.MatchmakerConnectorException;
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
    private static volatile Boolean[] invokations;

    @BeforeAll
    public static void setUp() throws SocketException {
        matchmaker = new Matchmaker(2);
        matchmaker.start();
        client1 = new DatagramSocket();
        client2 = new DatagramSocket();
        connector1 = new MatchmakerConnector();
        connector2 = new MatchmakerConnector();
    }

    @Test
    @Order(1)
    public void startTest() throws MatchmakerConnectorException {
        invokations = new Boolean[] {false, false};
        connector1.addFound((o, e) -> invokations[0] = true);
        connector2.addFound((o, e) -> invokations[1] = true);
        connector1.start(client1, InetAddress.getLoopbackAddress(), matchmaker.getPort(), 10 * 1000);
        connector2.start(client2, InetAddress.getLoopbackAddress(), matchmaker.getPort(), 10 * 1000);
        while (Arrays.stream(invokations).allMatch(i -> i)) Thread.onSpinWait();
    }

    @Test
    @Order(2)
    public void resultTest() {
        assertEquals(connector1.getMatchPort(), connector2.getMatchPort());
    }

    @Test
    @Order(3)
    public void stopTest() throws MatchmakerConnectorException, InterruptedException {
        connector1.start(client1, InetAddress.getLoopbackAddress(), matchmaker.getPort(), 10 * 1000);
        Thread.sleep(1000);
        connector1.stop();
    }
}
