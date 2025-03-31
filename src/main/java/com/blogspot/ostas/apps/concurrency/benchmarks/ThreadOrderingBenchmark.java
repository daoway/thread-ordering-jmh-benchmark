package com.blogspot.ostas.apps.concurrency.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 100, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class ThreadOrderingBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {
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
        
        public void runTest(OrderedExecution implementation, Blackhole blackhole) throws InterruptedException, ExecutionException {
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

    @Benchmark
    public void benchmarkWaitNotify(BenchmarkState state, Blackhole blackhole) throws InterruptedException, ExecutionException {
        state.runTest(state.waitNotifyImpl, blackhole);
    }
    
    @Benchmark
    public void benchmarkLockCondition(BenchmarkState state, Blackhole blackhole) throws InterruptedException, ExecutionException {
        state.runTest(state.lockConditionImpl, blackhole);
    }
    
    @Benchmark
    public void benchmarkCountDownLatch(BenchmarkState state, Blackhole blackhole) throws InterruptedException, ExecutionException {
        state.runTest(state.countDownLatchImpl, blackhole);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ThreadOrderingBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
