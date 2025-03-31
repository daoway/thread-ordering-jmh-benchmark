package com.blogspot.ostas.apps.concurrency.benchmarks;

// Implementation 1: Using synchronized and wait/notify
class FooWithWaitNotify implements OrderedExecution {
    private boolean firstDone;
    private boolean secondDone;
    private final Object lock;

    public FooWithWaitNotify() {
        firstDone = false;
        secondDone = false;
        lock = new Object();
    }

    @Override
    public void first(Runnable printFirst) throws InterruptedException {
        synchronized (lock) {
            printFirst.run();
            firstDone = true;
            lock.notifyAll();
        }
    }

    @Override
    public void second(Runnable printSecond) throws InterruptedException {
        synchronized (lock) {
            while (!firstDone) {
                lock.wait();
            }
            printSecond.run();
            secondDone = true;
            lock.notifyAll();
        }
    }

    @Override
    public void third(Runnable printThird) throws InterruptedException {
        synchronized (lock) {
            while (!secondDone) {
                lock.wait();
            }
            printThird.run();
        }
    }
}
