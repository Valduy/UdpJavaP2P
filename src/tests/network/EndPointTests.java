package tests.network;

import com.company.network.EndPoint;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class EndPointTests {
    @ParameterizedTest
    @MethodSource("equalsTestArguments")
    public void equalsTest(EndPoint endPoint1, EndPoint endPoint2) {
        assertEquals(endPoint1, endPoint2);
    }

    @ParameterizedTest
    @MethodSource("notEqualsTestArguments")
    public void notEqualsTest(EndPoint endPoint1, EndPoint endPoint2){
        assertNotEquals(endPoint1, endPoint2);
    }

    private static Stream<Arguments> equalsTestArguments() throws UnknownHostException {
        return Stream.of(
                Arguments.of(
                        new EndPoint(InetAddress.getByName("127.0.0.1"), 8000),
                        new EndPoint(InetAddress.getByName("127.0.0.1"), 8000))
        );
    }

    private static Stream<Arguments> notEqualsTestArguments() throws UnknownHostException {
        return Stream.of(
                Arguments.of(
                        new EndPoint(InetAddress.getByName("127.0.0.1"), 7000),
                        new EndPoint(InetAddress.getByName("127.0.0.1"), 5000)),
                Arguments.of(
                        new EndPoint(InetAddress.getByName("192.168.0.1"), 1234),
                        new EndPoint(InetAddress.getByName("192.168.0.2"), 1234)),
                Arguments.of(
                        new EndPoint(InetAddress.getByName("134.18.0.10"), 8765),
                        new EndPoint(InetAddress.getByName("134.18.0.10"), 5678))
        );
    }
}
