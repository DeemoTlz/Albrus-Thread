package com.deemo.thread;

import com.deemo.util.DeemoUtils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

public class DeemoABA {
    private static final AtomicInteger atomicInteger = new AtomicInteger(100);
    private static final AtomicReference<Integer> atomicReference = new AtomicReference<>(100);
    private static final AtomicStampedReference<Integer> atomicStampedReference = new AtomicStampedReference<>(100, 0);

    public static void main(String[] args) {
        System.out.println("AtomicInteger problem of ABA.");
        new Thread(() -> {
            System.out.println("100 to 200: " + atomicInteger.compareAndSet(100, 200) + ", now is: " + atomicInteger.get());
            System.out.println("200 to 100: " + atomicInteger.compareAndSet(200, 100) + ", now is: " + atomicInteger.get());
        }, "t1").start();
        Thread t2 = new Thread(() -> {
            DeemoUtils.sleep(2);
            System.out.println("100 to 300: " + atomicInteger.compareAndSet(100, 300) + ", now is: " + atomicInteger.get());
        }, "t2");
        t2.start();
        DeemoUtils.join(t2);

        System.out.println("AtomicReference problem of ABA.");
        new Thread(() -> {
            System.out.println("100 to 200: " + atomicReference.compareAndSet(100, 200) + ", now is: " + atomicReference.get());
            System.out.println("200 to 100: " + atomicReference.compareAndSet(200, 100) + ", now is: " + atomicReference.get());
        }, "tt1").start();
        Thread tt2 = new Thread(() -> {
            DeemoUtils.sleep(2);
            System.out.println("100 to 300: " + atomicReference.compareAndSet(100, 300) + ", now is: " + atomicReference.get());
        }, "tt2");
        tt2.start();
        DeemoUtils.join(tt2);

        System.out.println("AtomicStampedReference problem of ABA.");
        new Thread(() -> {
            System.out.println("100 to 200: " + atomicStampedReference.compareAndSet(100, 200, 0, 1) + ", now is: " + atomicStampedReference.getReference());
            System.out.println("200 to 100: " + atomicStampedReference.compareAndSet(200, 100, 1, 2) + ", now is: " + atomicStampedReference.getReference());
        }, "ttt1").start();
        new Thread(() -> {
            DeemoUtils.sleep(2);
            System.out.println("100 to 300: " + atomicStampedReference.compareAndSet(100, 300, 0 ,1) + ", now is: " + atomicStampedReference.getReference());
        }, "ttt2").start();
    }
}
