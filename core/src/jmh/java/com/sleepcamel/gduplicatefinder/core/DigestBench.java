package com.sleepcamel.gduplicatefinder.core;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1)
@State(Scope.Thread)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
@Threads(3)
public class DigestBench {

    private MessageDigest digestInstance;

    @Param({"1024", "1048576", "536870912"})
    public int size;

    @Setup
    public void prepare() throws NoSuchAlgorithmException {
        digestInstance = MessageDigest.getInstance("MD5");
    }

    @Benchmark
    public void digestMD5OneInstance(Blackhole bh) {
        bh.consume(digestInstance.digest(new byte[size]));
    }

}
