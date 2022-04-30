package com.deemo.lock;

import java.util.concurrent.locks.StampedLock;

public class DeemoStampedLock {

    public static void main(String[] args) {
        StampedLock stampedLock = new StampedLock();

        long readLock = stampedLock.readLock();
        System.out.println("read...");
        stampedLock.unlock(readLock);

        // 支持响应中断
        // stampedLock.readLockInterruptibly();
        // stampedLock.writeLockInterruptibly();

        reRead(stampedLock);
        reWrite(stampedLock);
    }

    private static void reRead(StampedLock stampedLock) {
        long readLock = stampedLock.readLock();
        System.out.println("read...");
        reRead(stampedLock);
        stampedLock.unlock(readLock);
    }

    private static void reWrite(StampedLock stampedLock) {
        long writeLock = stampedLock.writeLock();
        System.out.println("write...");
        reWrite(stampedLock);
        stampedLock.unlock(writeLock);
    }

}
