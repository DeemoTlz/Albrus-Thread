package com.deemo.coclass;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 教室锁门问题
 */
public class DeemoCountDownLatch {

    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(6);

        for (int i = 0; i < 6; i++) {
            new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(new Random().nextInt(5));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("小小" + Thread.currentThread().getName().split("-")[1] + "离开了教室。。。");
                countDownLatch.countDown();
            }, "thread-0" + i).start();
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            System.out.println("女朋友叫班长不等了。。。");
            throw new RuntimeException(e);
        }

        System.out.println("人都走完了，班长该锁门了。");
    }

}
