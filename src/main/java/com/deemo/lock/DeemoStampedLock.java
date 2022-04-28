package com.deemo.lock;

import java.util.concurrent.locks.StampedLock;

public class DeemoStampedLock {

    public static void main(String[] args) {
        StampedLock stampedLock = new StampedLock();

        long readLock = stampedLock.readLock();
        System.out.println("read...");

        long writeLock = stampedLock.writeLock();
        System.out.println("write...");
    }

}
