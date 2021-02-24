package network;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageHelper {
    public static NetworkMessages getMessageType(byte[] message){
        var decoded = new String(message, 0, NetworkMessages.size, StandardCharsets.US_ASCII);
        return NetworkMessages.valueOf(decoded);
    }

    public static byte[] toByteArray(byte[] message){
        return Arrays.copyOfRange(message, NetworkMessages.size, message.length);
    }

    public static String toString(byte[] message){
        return new String(message, NetworkMessages.size,
                message.length - NetworkMessages.size, StandardCharsets.US_ASCII);
    }

    public static byte[] getMessage(NetworkMessages type){
        return type.label.getBytes(StandardCharsets.US_ASCII);
    }

    public static byte[] getMessage(NetworkMessages type, byte[] data){
        var typeBytes = type.label.getBytes(StandardCharsets.US_ASCII);
        var result = new byte[NetworkMessages.size + data.length];
        System.arraycopy(typeBytes, 0, result, 0, typeBytes.length);
        System.arraycopy(data, 0, result, typeBytes.length, data.length);
        return result;
    }
}
