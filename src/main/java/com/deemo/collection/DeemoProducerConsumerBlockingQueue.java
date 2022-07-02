package com.deemo.collection;

import com.deemo.util.DeemoUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DeemoProducerConsumerBlockingQueue {

    private final static Integer COUNT = 20;

    public static void main(String[] args) {
        ShareData shareData = new ShareData(new ArrayBlockingQueue<>(10));
        DeemoProducerConsumerBlockingQueue producerConsumer = new DeemoProducerConsumerBlockingQueue();

        new Thread(() -> {
            producerConsumer.put(shareData);
        }, "Thread-Put").start();

        new Thread(() -> {
            producerConsumer.take(shareData);
        }, "Thread-Take").start();
    }

    private void put(ShareData shareData) {
        for (int i = 0; i < COUNT; i++) {
            try {
                shareData.getData().put(i);
                System.out.println(Thread.currentThread().getName() + "\t put: " + i + ", all data: " + shareData.getData());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void take(ShareData shareData) {
        DeemoUtils.sleep(new Random().nextInt(10));
        for (int i = 0; i < COUNT; i++) {
            try {
                System.out.println(Thread.currentThread().getName() + "\t take: " + shareData.getData().take());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Data
    @AllArgsConstructor
    private static class ShareData {
        private BlockingQueue<Integer> data;
    }
}
