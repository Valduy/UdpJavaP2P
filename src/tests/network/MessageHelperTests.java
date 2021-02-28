package tests.network;

import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class MessageHelperTests {
    @ParameterizedTest
    @MethodSource("equalsExtractedMessageTypeTestArguments")
    public void equalsExtractedMessageTypeTest(byte[] message, NetworkMessages messageType){
        assertEquals(MessageHelper.getMessageType(message), messageType);
    }

    @ParameterizedTest
    @MethodSource("notEqualsExtractedMessageTypeTestArguments")
    public void notEqualsExtractedMessageTypeTest(byte[] message, NetworkMessages messageType){
        assertNotEquals(MessageHelper.getMessageType(message), messageType);
    }

    @ParameterizedTest
    @MethodSource("messageWriteAndReadBytesTestArguments")
    public void messageWriteAndReadBytesTest(NetworkMessages type, byte[] data){
        var message = MessageHelper.getMessage(type, data);
        assertEquals(MessageHelper.getMessageType(message), type);
        assertArrayEquals(MessageHelper.toByteArray(message), data);
    }

    @ParameterizedTest
    @MethodSource("messageWriteAndReadStringTestArguments")
    public void messageWriteAndReadStringTest(NetworkMessages type, String data){
        var message = MessageHelper.getMessage(type, data);
        assertEquals(MessageHelper.getMessageType(message), type);
        assertEquals(MessageHelper.toString(message), data);
    }

    private static Stream<Arguments> equalsExtractedMessageTypeTestArguments(){
        return Stream.of(
                Arguments.of(
                        MessageHelper.getMessage(NetworkMessages.HLLO),
                        NetworkMessages.HLLO),
                Arguments.of(
                        MessageHelper.getMessage(NetworkMessages.INIT),
                        NetworkMessages.INIT),
                Arguments.of(
                        MessageHelper.getMessage(NetworkMessages.INFO),
                        NetworkMessages.INFO),
                Arguments.of(
                        MessageHelper.getMessage(NetworkMessages.GBYE),
                        NetworkMessages.GBYE)
        );
    }

    private static Stream<Arguments> notEqualsExtractedMessageTypeTestArguments(){
        return Stream.of(
                Arguments.of(
                        MessageHelper.getMessage(NetworkMessages.GBYE),
                        NetworkMessages.HLLO)
        );
    }

    private static Stream<Arguments> messageWriteAndReadBytesTestArguments(){
        return Stream.of(
                Arguments.of(
                        NetworkMessages.HLLO,
                        new byte[] {0, 1, 2}),
                Arguments.of(
                        NetworkMessages.INFO,
                        new byte[] {0, 1, 2, 9, 8, 7})
        );
    }

    private static Stream<Arguments> messageWriteAndReadStringTestArguments(){
        return Stream.of(
                Arguments.of(NetworkMessages.HLLO, "some message"),
                Arguments.of(NetworkMessages.GBYE, "another message...")
        );
    }
}
