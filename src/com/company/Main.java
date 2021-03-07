package com.company;

import com.company.network.LanIpHelper;
import com.company.server.matches.MatchException;
import com.company.server.matchmakers.Matchmaker;
import com.company.server.matchmakers.MatchmakerException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws SocketException, UnknownHostException {
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
        } catch (IOException | MatchmakerException e){
            e.printStackTrace();
        }
    }
}
