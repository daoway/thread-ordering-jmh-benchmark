package com.blogspot.ostas.apps.concurrency.benchmarks;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@State(Scope.Thread)
public class BenchmarkState {
    final List<String> output = new ArrayList<>();
    final Runnable printFirst = () -> output.add("first");
    final Runnable printSecond = () -> output.add("second");
    final Runnable printThird = () -> output.add("third");

    OrderedExecution waitNotifyImpl;
    OrderedExecution lockConditionImpl;
    OrderedExecution countDownLatchImpl;

    ExecutorService executor;

    @Setup
    public void setup() {
        waitNotifyImpl = new FooWithWaitNotify();
        lockConditionImpl = new FooWithLockCondition();
        countDownLatchImpl = new FooWithCountDownLatch();
        executor = Executors.newFixedThreadPool(3);
    }

    @TearDown
    public void tearDown() {
        executor.shutdown();
    }

    public void runTest(OrderedExecution implementation, Blackhole blackhole)
            throws InterruptedException,
            ExecutionException {
        output.clear();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(3);

        Future<?> future3 = executor.submit(() -> {
            try {
                startLatch.await();
                implementation.third(printThird);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                finishLatch.countDown();
            }
        });

        Future<?> future2 = executor.submit(() -> {
            try {
                startLatch.await();
                implementation.second(printSecond);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                finishLatch.countDown();
            }
        });

        Future<?> future1 = executor.submit(() -> {
            try {
                startLatch.await();
                implementation.first(printFirst);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                finishLatch.countDown();
            }
        });

        // Start all threads at once
        startLatch.countDown();
        finishLatch.await();

        // Wait for all futures to complete
        future1.get();
        future2.get();
        future3.get();

        // Consume the result to prevent dead code elimination
        blackhole.consume(output);
    }
}
