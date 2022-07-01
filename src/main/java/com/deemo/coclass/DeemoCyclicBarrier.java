package com.deemo.coclass;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * 七龙珠问题
 */
public class DeemoCyclicBarrier {

    public static void main(String[] args) {
        // CyclicBarrier 执行完回调后，会自动重置计数器
        CyclicBarrier cyclicBarrier = new CyclicBarrier(7, () -> {
            System.out.println("召~唤$神%龙！！！！！！");
        });

        for (int i = 0; i < 7; i++) {
            new Thread(() -> {
                // 循环利用
                while (true) {
                    try {
                        TimeUnit.SECONDS.sleep(new Random().nextInt(5));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println(Thread.currentThread().getName().split("-")[1] + " 星龙珠找到了！");
                    try {
                        cyclicBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        System.out.println("不等待召唤神龙了。。。");
                        throw new RuntimeException(e);
                    }
                    System.out.println(Thread.currentThread().getName().split("-")[1] + " 星龙珠又消失了^~");
                }
            }, "thread-0" + i).start();
        }
    }

}
