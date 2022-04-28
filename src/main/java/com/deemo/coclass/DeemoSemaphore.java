package com.deemo.coclass;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 抢车位问题
 */
public class DeemoSemaphore {

    public static void main(String[] args) {
        // 三个许可证
        Semaphore semaphore = new Semaphore(3);

        for (int i = 0; i < 6; i++) {
            int finalI = i;
            new Thread(() -> {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("第 " + finalI + " 辆车抢到了车位");

                try {
                    TimeUnit.SECONDS.sleep(new Random().nextInt(5));
                    System.out.println("第 " + finalI + " 辆车离开了车位");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    semaphore.release();
                }
            }, "thread-0" + i).start();
        }
    }

}
