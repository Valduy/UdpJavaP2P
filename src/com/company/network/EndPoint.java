package com.company.network;

import java.net.InetAddress;
import java.util.Objects;

public class EndPoint {
    public final InetAddress address;
    public final int port;

    public EndPoint(InetAddress address, int port){
        this.address = address;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndPoint endPoint = (EndPoint) o;
        return port == endPoint.port && Objects.equals(address, endPoint.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }
}
