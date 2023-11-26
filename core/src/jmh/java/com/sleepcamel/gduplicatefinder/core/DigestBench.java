package com.sleepcamel.gduplicatefinder.core;

import org.apache.commons.codec.digest.Blake3;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

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

    @Benchmark
    public void digestMD5_inc(Blackhole bh) throws IOException {
        forEachBufferIn(bytes, (buffer, len) -> digestInstance.update(bytes, 0, len));
        bh.consume(digestInstance.digest());
    }

    @Benchmark
    public void digestBlake_inc(Blackhole bh) throws IOException {
        forEachBufferIn(bytes, (buffer, len) -> blake3.update(bytes, 0, len));
        bh.consume(blake3.doFinalize(outBytes));
    }

    private void forEachBufferIn(byte[] bytes, BiConsumer<byte[], Integer> consumer) throws IOException {
        try(var reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))){
            final int TRANSFER_BUFFER_SIZE = 8192;
            char[] buffer = new char[TRANSFER_BUFFER_SIZE];
            int nRead;
            while ((nRead = reader.read(buffer, 0, TRANSFER_BUFFER_SIZE)) >= 0) {
                consumer.accept(bytes, nRead);
            }
        }
    }
}
