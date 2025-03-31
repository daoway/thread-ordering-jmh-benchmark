package com.blogspot.ostas.apps.concurrency.benchmarks;

import java.util.concurrent.CountDownLatch;

// Implementation 3: Using CountDownLatch
class FooWithCountDownLatch implements OrderedExecution {
    private final CountDownLatch firstLatch;
    private final CountDownLatch secondLatch;

    public FooWithCountDownLatch() {
        firstLatch = new CountDownLatch(1);
        secondLatch = new CountDownLatch(1);
    }

    @Override
    public void first(Runnable printFirst) throws InterruptedException {
        printFirst.run();
        firstLatch.countDown();
    }

    @Override
    public void second(Runnable printSecond) throws InterruptedException {
        firstLatch.await();
        printSecond.run();
        secondLatch.countDown();
    }

    @Override
    public void third(Runnable printThird) throws InterruptedException {
        secondLatch.await();
        printThird.run();
    }
}
