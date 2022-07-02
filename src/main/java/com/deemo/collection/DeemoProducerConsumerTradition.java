package com.deemo.collection;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DeemoProducerConsumerTradition {
    private final static Integer COUNT = 5;
    private final Lock lock;
    private final Condition zero;
    private final Condition notZero;

    public DeemoProducerConsumerTradition(boolean fair) {
        lock = new ReentrantLock(fair);
        zero = lock.newCondition();
        notZero =  lock.newCondition();
    }

    public static void main(String[] args) {
        ShareData shareData = new ShareData(0);
        DeemoProducerConsumerTradition producerConsumer = new DeemoProducerConsumerTradition(false);

        new Thread(() -> {
            for (int i = 0; i < COUNT; i++) {
                producerConsumer.increment(shareData);
            }
        }, "INCREMENT").start();

        new Thread(() -> {
            for (int i = 0; i < COUNT; i++) {
                producerConsumer.decrement(shareData);
            }
        }, "DECREMENT").start();
    }

    private void increment(ShareData shareData) {
        lock.lock();
        try {
            while (shareData.getData() != 0) {
                // 不等于 0，循环等待
                notZero.await();
            }

            // 操作数据
            shareData.setData(shareData.getData() + 1);
            System.out.println(Thread.currentThread().getName() + "\t increment 1: " + shareData.getData());

            // 唤醒（防止虚假唤醒）
            zero.signalAll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    private void decrement(ShareData shareData) {
        lock.lock();
        try {
            while (shareData.getData() == 0) {
                // 等于 0，循环等待
                zero.await();
            }

            // 操作数据
            shareData.setData(shareData.getData() - 1);
            System.out.println(Thread.currentThread().getName() + "\t decrement 1: " + shareData.getData());

            // 唤醒（防止虚假唤醒）
            notZero.signalAll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    @Data
    @AllArgsConstructor
    private static class ShareData {
        private Integer data;
    }

}
