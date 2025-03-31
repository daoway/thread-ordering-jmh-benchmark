package com.blogspot.ostas.apps.concurrency.benchmarks;

// Common interface for all implementations
interface OrderedExecution {
    void first(Runnable printFirst) throws InterruptedException;

    void second(Runnable printSecond) throws InterruptedException;

    void third(Runnable printThird) throws InterruptedException;
}
