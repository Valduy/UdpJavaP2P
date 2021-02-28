package tests.network;

import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageHelperTests {
    @ParameterizedTest
    @MethodSource("getMessageTypeTestArguments")
    public void getMessageTypeTest(byte[] message, NetworkMessages messageType, boolean equalsResult){
        assertEquals(MessageHelper.getMessageType(message).equals(messageType), equalsResult);
    }



    private static Stream<Arguments> getMessageTypeTestArguments(){
        return Stream.of(
                Arguments.of(
                        MessageHelper.getMessage(NetworkMessages.HLLO),
                        NetworkMessages.HLLO,
                        true),
                Arguments.of(
                        MessageHelper.getMessage(NetworkMessages.INIT),
                        NetworkMessages.INIT,
                        true),
                Arguments.of(
                        MessageHelper.getMessage(NetworkMessages.INFO),
                        NetworkMessages.INFO,
                        true),
                Arguments.of(
                        MessageHelper.getMessage(NetworkMessages.GBYE),
                        NetworkMessages.GBYE,
                        true),
                Arguments.of(
                        MessageHelper.getMessage(NetworkMessages.GBYE),
                        NetworkMessages.HLLO,
                        false)
                );
    }
}
