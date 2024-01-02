/*
 * Copyright 2012-2024 guicamest
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sleepcamel.gduplicatefinder.core;

import org.apache.commons.codec.digest.Blake3;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1)
@State(Scope.Benchmark)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
public class DigestBench {

    private MessageDigest digestInstance;
    private Blake3 blake3;

    @Param({"1024", "1048576", "536870912"})
    public int size;

    private byte[] testBytes;
    private final byte[] outBytes = new byte[32];

    @Setup
    public void prepare() throws NoSuchAlgorithmException {
        digestInstance = MessageDigest.getInstance("MD5");
        blake3 = Blake3.initHash();
        testBytes = new byte[size];
    }

    @Setup(Level.Iteration)
    public void reset() {
        digestInstance.reset();
        blake3.reset();
    }

    @Benchmark
    public void digestMD5(Blackhole bh) {
        bh.consume(digestInstance.digest(testBytes));
    }

    @Benchmark
    public void digestBlake(Blackhole bh) {
        blake3.update(testBytes);
        bh.consume(blake3.doFinalize(outBytes));
    }

    @Benchmark
    public void digestMD5_inc(Blackhole bh) throws IOException {
        forEachBufferIn(testBytes, (buffer, len) -> digestInstance.update(buffer, 0, len));
        bh.consume(digestInstance.digest());
    }

    @Benchmark
    public void digestBlake_inc(Blackhole bh) throws IOException {
        forEachBufferIn(testBytes, (buffer, len) -> blake3.update(buffer, 0, len));
        bh.consume(blake3.doFinalize(outBytes));
    }

    final int TRANSFER_BUFFER_SIZE = 16 * 1024;
    byte[] readBuffer = new byte[TRANSFER_BUFFER_SIZE];
    private void forEachBufferIn(byte[] allBytes, BiConsumer<byte[], Integer> consumer) throws IOException {
        try(var reader = new BufferedInputStream(new ByteArrayInputStream(allBytes), TRANSFER_BUFFER_SIZE)){
            int nRead;
            while ((nRead = reader.read(readBuffer, 0, TRANSFER_BUFFER_SIZE)) >= 0) {
                consumer.accept(readBuffer, nRead);
            }
        }
    }
}
