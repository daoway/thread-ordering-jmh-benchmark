package com.blogspot.ostas.apps.concurrency.benchmarks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Implementation 2: Using Lock and Condition
class FooWithLockCondition implements OrderedExecution {
    private final Lock lock;
    private final Condition firstDone;
    private final Condition secondDone;
    private boolean firstCompleted;
    private boolean secondCompleted;

    public FooWithLockCondition() {
        lock = new ReentrantLock();
        firstDone = lock.newCondition();
        secondDone = lock.newCondition();
        firstCompleted = false;
        secondCompleted = false;
    }

    @Override
    public void first(Runnable printFirst) throws InterruptedException {
        lock.lock();
        try {
            printFirst.run();
            firstCompleted = true;
            firstDone.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void second(Runnable printSecond) throws InterruptedException {
        lock.lock();
        try {
            while (!firstCompleted) {
                firstDone.await();
            }
            printSecond.run();
            secondCompleted = true;
            secondDone.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void third(Runnable printThird) throws InterruptedException {
        lock.lock();
        try {
            while (!secondCompleted) {
                secondDone.await();
            }
            printThird.run();
        } finally {
            lock.unlock();
        }
    }
}
