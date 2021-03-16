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
        connector1 = new MatchmakerConnector(client1, InetAddress.getLoopbackAddress(), matchmaker.getPort());
        connector2 = new MatchmakerConnector(client2, InetAddress.getLoopbackAddress(), matchmaker.getPort());
    }

    @AfterAll
    public static void finish() throws ExecutionException, InterruptedException {
        matchmaker.cancel();
        future.get();
        client1.close();
        client2.close();
    }

    static int r1;
    static int r2;

    @Test
    @Order(1)
    public void testConnection() throws ExecutionException, InterruptedException {
        var executor = Executors.newFixedThreadPool(2);
        var future1 = executor.submit(connector1);
        var future2 = executor.submit(connector2);
        var result1 = future1.get();
        var result2 = future2.get();
        assertEquals(result1, result2);
    }
}
