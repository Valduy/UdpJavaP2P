package network;

import java.net.InetAddress;
import java.util.Objects;

public class EndPoints {
    public final EndPoint publicEndPoint;
    public final EndPoint privateEndPoint;

    public EndPoints(EndPoint publicEndPoint, EndPoint privateEndPoint) {
        this.publicEndPoint = publicEndPoint;
        this.privateEndPoint = privateEndPoint;
    }

    public EndPoints(InetAddress publicAddress, int publicPort,
                    InetAddress privateAddress, int privatePort)
    {
        this(new EndPoint(publicAddress, publicPort), new EndPoint(privateAddress, privatePort));
    }

    public boolean isPublicEndPoint(EndPoint endPoint){
        return publicEndPoint.equals(endPoint);
    }

    public boolean isPrivateEndPoint(EndPoint endPoint){
        return privateEndPoint.equals(endPoint);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndPoints endPoints = (EndPoints) o;
        return Objects.equals(publicEndPoint, endPoints.publicEndPoint) && Objects.equals(privateEndPoint, endPoints.privateEndPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicEndPoint, privateEndPoint);
    }
}
