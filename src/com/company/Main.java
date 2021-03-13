package com.company;

import com.company.server.matchmakers.Matchmaker;
import com.company.server.matchmakers.MatchmakerException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        try {
            var port = Integer.parseInt(args[0]);
            var matchmaker = new Matchmaker(2, port);
            var executor = Executors.newSingleThreadExecutor();
            var future = executor.submit(matchmaker);
            future.get();
        } catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
        }
    }
}
