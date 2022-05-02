package com.deemo.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DeemoCompletableFuture {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 任务一
        CompletableFuture<Void> cf1 = CompletableFuture.runAsync(() -> {
            System.out.println("T1: 洗水壶...");
            DeemoCompletableFuture.sleep(TimeUnit.SECONDS, 1);

            System.out.println("T1: 烧开水...");
            DeemoCompletableFuture.sleep(TimeUnit.SECONDS, 15);
        });

        // 任务二
        CompletableFuture<String> cf2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("T2: 洗茶壶...");
            DeemoCompletableFuture.sleep(TimeUnit.SECONDS, 1);

            System.out.println("T2: 洗茶杯...");
            DeemoCompletableFuture.sleep(TimeUnit.SECONDS, 2);

            System.out.println("T2: 拿茶叶...");
            DeemoCompletableFuture.sleep(TimeUnit.SECONDS, 1);

            return "红茶";
        });

        // 任务三：等待任务一和任务二完成后执行
        CompletableFuture<String> cf3 = cf1.thenCombine(cf2, (__, name) -> {
            System.out.println("拿到茶叶：" + name);
            System.out.println("泡茶...");

            return "上等好茶：" + name;
        });

        // 等待任务一执行结束
        System.out.println(cf3.get());
    }

    private static void sleep(TimeUnit tu, long timeout) {
        try {
            tu.sleep(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
