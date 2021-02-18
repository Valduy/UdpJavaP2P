package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) {
        try {
            var streamReader = new InputStreamReader(System.in);
            var bufferedReader = new BufferedReader(streamReader);

            var matchmaker = new Matchmaker();
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
