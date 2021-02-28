package tests.network;

import com.company.network.EndPoint;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EndPointTests {
    @ParameterizedTest
    @MethodSource("equalsTestArguments")
    public void equalsTest(EndPoint endPoint1, EndPoint endPoint2, boolean equalsResult) {
        assertEquals(endPoint1.equals(endPoint2), equalsResult);
    }

    private static Stream<Arguments> equalsTestArguments() throws UnknownHostException {
        return Stream.of(
                Arguments.of(
                        new EndPoint(InetAddress.getByName("127.0.0.1"), 8000),
                        new EndPoint(InetAddress.getByName("127.0.0.1"), 8000),
                        true),
                Arguments.of(
                        new EndPoint(InetAddress.getByName("127.0.0.1"), 7000),
                        new EndPoint(InetAddress.getByName("127.0.0.1"), 5000),
                        false),
                Arguments.of(
                        new EndPoint(InetAddress.getByName("192.168.0.1"), 1234),
                        new EndPoint(InetAddress.getByName("192.168.0.2"), 1234),
                        false),
                Arguments.of(
                        new EndPoint(InetAddress.getByName("134.18.0.10"), 8765),
                        new EndPoint(InetAddress.getByName("134.18.0.10"), 5678),
                        false)
        );
    }
}
