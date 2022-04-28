package com.deemo.thread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class DeemoThread {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<Integer> futureTask = new FutureTask<>(() -> {
            TimeUnit.SECONDS.sleep(5);
            return 10;
        });

        new Thread(futureTask, "deemo-thread-01").start();

        while (!futureTask.isDone()) {
            System.out.println("wait...");
        }
        System.out.println(futureTask.get());
        System.out.println("done.");
    }

}
