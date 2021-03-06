package com.company;

import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.company.network.UserStatus;
import com.company.server.Matchmaker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws SocketException {
        try {
            var streamReader = new InputStreamReader(System.in);
            var bufferedReader = new BufferedReader(streamReader);

            var matchmaker = new Matchmaker(2);
            matchmaker.start(4756);

            while (true){
                String command = bufferedReader.readLine();
                if (command.equals("end")){
                    break;
                }
            }

            matchmaker.stop();
        } catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }
}
