package tests.network;

import com.company.network.EndPoints;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class EndPointsTests {
    @ParameterizedTest
    @MethodSource("equalsTestArguments")
    public void equalsTest(EndPoints endPoints1, EndPoints endPoints2){
        assertEquals(endPoints1, endPoints2);
    }

    @ParameterizedTest
    @MethodSource("notEqualsTestArguments")
    public void notEqualsTest(EndPoints endPoints1, EndPoints endPoints2){
        assertNotEquals(endPoints1, endPoints2);
    }

    private static Stream<Arguments> equalsTestArguments() throws UnknownHostException {
        return Stream.of(
                Arguments.of(
                        new EndPoints(
                                InetAddress.getByName("96.72.0.9"), 8000,
                                InetAddress.getByName("192.168.0.10"), 8000),
                        new EndPoints(
                                InetAddress.getByName("96.72.0.9"), 8000,
                                InetAddress.getByName("192.168.0.10"), 8000))
        );
    }

    private static Stream<Arguments> notEqualsTestArguments() throws UnknownHostException {
        return Stream.of(
                Arguments.of(
                        new EndPoints(
                                InetAddress.getByName("96.72.0.9"), 8001,
                                InetAddress.getByName("192.168.0.10"), 8001),
                        new EndPoints(
                                InetAddress.getByName("96.72.0.9"), 8000,
                                InetAddress.getByName("192.168.0.10"), 8000))
        );
    }
}
