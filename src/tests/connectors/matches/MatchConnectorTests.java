package tests.connectors.matches;

import com.company.server.matches.Match;
import com.company.server.matches.MatchException;
import connectors.ConnectorException;
import connectors.matches.MatchConnector;
import org.junit.jupiter.api.*;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MatchConnectorTests {
    private static Match match;
    private static DatagramSocket client1;
    private static DatagramSocket client2;
    private static MatchConnector connector1;
    private static MatchConnector connector2;

    @BeforeAll
    public static void setUp() throws SocketException, MatchException {
        match = new Match(2);
        match.start(30 * 1000);
        client1 = new DatagramSocket();
        client2 = new DatagramSocket();
        connector1 = new MatchConnector();
        connector2 = new MatchConnector();
    }

    @AfterAll
    public static void finish() throws ConnectorException, MatchException {
        connector1.stop();
        connector2.stop();
        match.stop();
        client1.close();
        client2.close();
    }

    @Test
    @Order(1)
    public void testConnection() throws ConnectorException {
        var invocations = new Boolean[] {false, false};
        connector1.addConnected((o, e) -> invocations[0] = true);
        connector2.addConnected((o, e) -> invocations[1] = true);
        connector1.start(client1, InetAddress.getLoopbackAddress(), match.getPort());
        connector2.start(client2, InetAddress.getLoopbackAddress(), match.getPort());
        while (!Arrays.stream(invocations).allMatch(i -> i)) Thread.onSpinWait();
    }
}