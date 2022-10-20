package cn.vlts.ulid4j.benchmark;

import cn.vlts.ulid4j.ULID;
import cn.vlts.ulid4j.ULIDFactory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author throwable
 * @version v1
 * @description Benchmark
 * @since 2022/10/20 11:00
 */
@Fork(1)
@Threads(10)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 5, time = 3)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BenchmarkTest {

    static ULIDFactory U;

    static ULIDFactory M;

    @Setup
    public void setup() {
        U = ULIDFactory.factory();
        M = ULIDFactory.monotonicFactory();
    }

    @Benchmark
    public UUID createUUID() {
        return UUID.randomUUID();
    }

    @Benchmark
    public String createUUIDToString() {
        return UUID.randomUUID().toString();
    }

    @Benchmark
    public ULID createULID() {
        return U.ulid();
    }

    @Benchmark
    public String createULIDToString() {
        return U.ulid().toString();
    }

    @Benchmark
    public ULID createMonotonicULID() {
        return M.ulid();
    }

    @Benchmark
    public String createMonotonicULIDToString() {
        return M.ulid().toString();
    }

    public static void main(String[] args) throws Exception {
        new Runner(new OptionsBuilder().build()).run();
    }
}
