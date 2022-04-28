package com.deemo.lock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    }

}
