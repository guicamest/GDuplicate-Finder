package com.sleepcamel.gduplicatefinder.core;

import org.apache.commons.codec.digest.Blake3;
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
public class DigestBench {

    private MessageDigest digestInstance;
    private Blake3 blake3;

    @Param({"1024", "1048576", "536870912"})
    public int size;

    private byte[] bytes;
    private final byte[] outBytes = new byte[32];

    @Setup
    public void prepare() throws NoSuchAlgorithmException {
        digestInstance = MessageDigest.getInstance("MD5");
        blake3 = Blake3.initHash();
        bytes = new byte[size];
    }

    @Setup(Level.Iteration)
    public void reset() {
        digestInstance.reset();
        blake3.reset();
    }

    @Benchmark
    public void digestMD5(Blackhole bh) {
        bh.consume(digestInstance.digest(bytes));
    }

    @Benchmark
    public void digestBlake(Blackhole bh) {
        blake3.update(bytes);
        bh.consume(blake3.doFinalize(outBytes));
    }
}
