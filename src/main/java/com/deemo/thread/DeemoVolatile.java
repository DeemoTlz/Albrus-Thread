package com.deemo.thread;

import com.deemo.util.DeemoUtils;

public class DeemoVolatile {

    public static void main(String[] args) {
        Data data = new Data();

        for (int i = 0; i < 20; i++) {
            new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    data.add(1);
                }
            }, "Thread-Volatile-" + i).start();
        }

        // 默认存在 main 线程和 GC 线程
        while (Thread.activeCount() > 2) {
            Thread.yield();
        }

        System.out.println("main thread is over, the value is: " + data.getData());
    }

    private static void sayOkByVolatile() {
        Data data = new Data();

        new Thread(() -> {
            System.out.println("Thread: " + Thread.currentThread().getName() + "\t come in...");
            DeemoUtils.sleep(3);
            data.add(66);
            System.out.println("Thread: " + Thread.currentThread().getName() + "\t added " + 66 + " is over.");
        }, "Thread-Volatile").start();

        while (data.getData() == 0) {
            // System.out.println(data.getData());
        }

        System.out.println("main thread is over.");
    }

    private static class Data {
        int data = 0;
        // volatile int data = 0;

        public void add(int value) {
            this.data += value;
        }

        public int getData() {
            return data;
        }

        public void setData(int data) {
            this.data = data;
        }
    }
}
