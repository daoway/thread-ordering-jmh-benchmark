package com.blogspot.ostas.apps.concurrency.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 100, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class ThreadOrderingBenchmark {

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
