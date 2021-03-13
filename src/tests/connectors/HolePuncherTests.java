package tests.connectors;

import com.company.network.P2PConnectionMessage;
import com.company.server.matches.Match;
import com.company.server.matches.MatchException;
import connectors.ConnectorException;
import connectors.HolePuncher;
import connectors.matches.MatchConnector;
import org.junit.jupiter.api.*;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HolePuncherTests {
    private static Match match;
    private static Future<?> future;
    private static DatagramSocket client1;
    private static DatagramSocket client2;
    private static MatchConnector connector1;
    private static MatchConnector connector2;
    private static HolePuncher puncher1;
    private static HolePuncher puncher2;
    private static P2PConnectionMessage result1;
    private static P2PConnectionMessage result2;

    @BeforeAll
    public static void setUp() throws SocketException, ConnectorException {
        match = new Match(2, 30 * 1000);
        var executor = Executors.newSingleThreadExecutor();
        future = executor.submit(match);
        client1 = new DatagramSocket();
        client2 = new DatagramSocket();
        connector1 = new MatchConnector(client1, InetAddress.getLoopbackAddress(), match.getPort());
        connector2 = new MatchConnector(client2, InetAddress.getLoopbackAddress(), match.getPort());
        puncher1 = new HolePuncher();
        puncher2 = new HolePuncher();
    }

    @AfterAll
    public static void finish() throws ConnectorException, ExecutionException, InterruptedException {
        match.cancel();
        future.get();
        puncher1.stop();
        puncher2.stop();
        client1.close();
        client2.close();
    }

    @Test
    @Order(1)
    public void testHolePunching() throws ExecutionException, InterruptedException {
        connectViaMatch();
        var invocations = new Boolean[] {false, false};
        puncher1.addPunched((o, e) -> invocations[0] = true);
        puncher2.addPunched((o, e) -> invocations[1] = true);
        puncher1.start(client1, result1);
        puncher2.start(client2, result2);
        while (!Arrays.stream(invocations).allMatch(i -> i)) Thread.onSpinWait();
    }

    @Test
    @Order(2)
    public void failureTest() throws ConnectorException {
        puncher1.addPunched((o, e) -> assertThrows(ConnectorException.class, () -> puncher1.getClients()));
        puncher1.start(client1, connector1.call());
    }

    private static void connectViaMatch() throws ExecutionException, InterruptedException {
        var invocations = new Boolean[] {false, false};
        var executor = Executors.newFixedThreadPool(2);
        var future1 = executor.submit(connector1);
        var future2 = executor.submit(connector2);
        result1 = future1.get();
        result2 = future2.get();
        while (!Arrays.stream(invocations).allMatch(i -> i)) Thread.onSpinWait();
    }
}
