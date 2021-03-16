package com.company;

import com.company.server.matchmakers.Matchmaker;

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
