package com.deemo.lock;

import java.util.concurrent.locks.ReentrantLock;

public class DeemoReentrantLock {

    public static void main(String[] args) {
        ReentrantLock reentrantLock = new ReentrantLock();

        reentrantLock.lock();
        System.out.println(reentrantLock.isLocked());
        System.out.println(reentrantLock.getHoldCount());
        System.out.println(reentrantLock.getQueueLength());
        reentrantLock.unlock();

        re(reentrantLock);
    }

    private static void re(ReentrantLock reentrantLock) {
        reentrantLock.lock();
        System.out.println("re...");
        re(reentrantLock);
        reentrantLock.unlock();
    }

}
