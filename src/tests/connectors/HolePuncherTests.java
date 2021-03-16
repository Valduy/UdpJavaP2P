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
    public static void setUp() throws SocketException, ConnectorException, MatchException {
        match = new Match(2, 30 * 1000);
        var executor = Executors.newSingleThreadExecutor();
        future = executor.submit(match);
        client1 = new DatagramSocket();
        client2 = new DatagramSocket();
        connector1 = new MatchConnector(client1, InetAddress.getLoopbackAddress(), match.getPort());
        connector2 = new MatchConnector(client2, InetAddress.getLoopbackAddress(), match.getPort());
    }

    @AfterAll
    public static void finish() throws ExecutionException, InterruptedException {
        match.cancel();
        future.get();
        client1.close();
        client2.close();
    }

    @Test
    @Order(1)
    public void testHolePunching() throws ExecutionException, InterruptedException {
        connectViaMatch();
        var puncher1 = new HolePuncher(client1, result1);
        var puncher2 = new HolePuncher(client2, result2);
        var executor = Executors.newFixedThreadPool(2);
        var future1 = executor.submit(puncher1);
        var future2 = executor.submit(puncher2);
        future1.get();
        future2.get();
    }

    private static void connectViaMatch() throws ExecutionException, InterruptedException {
        var executor = Executors.newFixedThreadPool(2);
        var future1 = executor.submit(connector1);
        var future2 = executor.submit(connector2);
        result1 = future1.get();
        result2 = future2.get();
    }
}
