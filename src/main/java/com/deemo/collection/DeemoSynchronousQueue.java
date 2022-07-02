package com.deemo.collection;

import com.deemo.util.DeemoUtils;

import java.util.Random;
import java.util.concurrent.SynchronousQueue;

public class DeemoSynchronousQueue {
    private static final Integer COUNT = 5;

    public static void main(String[] args) {
        SynchronousQueue<Integer> synchronousQueue = new SynchronousQueue<>();
        Random random = new Random();

        new Thread(() -> {
            try {
                for (int i = 0; i < COUNT; i++) {
                    DeemoUtils.sleep(random.nextInt(5));
                    System.out.println(Thread.currentThread().getName() + "\t take: " + synchronousQueue.take());
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "Thread-take").start();

        new Thread(() -> {
            try {
                for (int i = 0; i < COUNT; i++) {
                    DeemoUtils.sleep(random.nextInt(5));
                    synchronousQueue.put(i);
                    System.out.println(Thread.currentThread().getName() + "\t put: " + i + " succeed.");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "Thread-put").start();
    }

}
