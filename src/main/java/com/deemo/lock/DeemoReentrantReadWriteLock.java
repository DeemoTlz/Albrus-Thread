package com.deemo.lock;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class DeemoReentrantReadWriteLock {

    public static void main(String[] args) {
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();

        writeLock.lock();
        System.out.println("write...");

        System.out.println(readWriteLock.getReadLockCount());
        System.out.println(readWriteLock.isWriteLocked());

        readLock.lock();
        System.out.println("read...");

        System.out.println(readWriteLock.getReadLockCount());
        System.out.println(readWriteLock.isWriteLocked());

        writeLock.unlock();
        readLock.unlock();

        System.out.println("condition...");
        System.out.println(writeLock.newCondition());
        // java.lang.UnsupportedOperationException
        // System.out.println(readLock.newCondition());

        // reRead(readLock);
        reWrite(writeLock);
    }

    private static void reRead(ReentrantReadWriteLock.ReadLock readLock) {
        readLock.lock();
        System.out.println("read...");
        reRead(readLock);
        readLock.unlock();
    }

    private static void reWrite(ReentrantReadWriteLock.WriteLock writeLock) {
        writeLock.lock();
        System.out.println("write...");
        reWrite(writeLock);
        writeLock.unlock();
    }

}
