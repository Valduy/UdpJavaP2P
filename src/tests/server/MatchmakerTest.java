package tests.server;

import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.company.server.Matchmaker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.DatagramSocket;
import java.net.SocketException;

public class MatchmakerTest {
    private static Matchmaker matchmaker;
    private static DatagramSocket client;

    @BeforeAll
    public static void setUp() throws SocketException {
        matchmaker = new Matchmaker(2);
        matchmaker.start();
        client = new DatagramSocket();
    }

    @Test
    public void helloTest(){
        var message = MessageHelper.getMessage(NetworkMessages.HLLO);

        while (true){
            //client.co
        }
    }

    @AfterAll
    public static void finish() throws InterruptedException {
        matchmaker.stop();
    }
}
