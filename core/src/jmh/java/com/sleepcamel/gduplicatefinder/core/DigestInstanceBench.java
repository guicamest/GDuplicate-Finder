package com.sleepcamel.gduplicatefinder.core;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 1)
@Warmup(iterations = 1, time = 3)
@Measurement(time = 3)
public class DigestInstanceBench {

    @Benchmark
    public void getInstance(Blackhole bh) throws NoSuchAlgorithmException {
        bh.consume(MessageDigest.getInstance("MD5"));
    }

}
