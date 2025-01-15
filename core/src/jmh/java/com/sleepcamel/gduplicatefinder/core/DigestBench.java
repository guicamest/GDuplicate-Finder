/*
 * Copyright 2012-2025 guicamest
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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1)
@State(Scope.Benchmark)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
public class DigestBench {

    private MessageDigest messageDigestInstance;

    @Param({"1024", "1048576", "536870912"})
    public int size;

    @Param({"SUN:MD5", "BC:MD5", "BC:BLAKE2B-256", "BC:BLAKE3-256"})
    public String providerAndAlgo;

    static {Security.addProvider(new BouncyCastleProvider());}

    private byte[] testBytes;

    @Setup
    public void prepare() throws NoSuchAlgorithmException, NoSuchProviderException {
        var parts = providerAndAlgo.split(":");
        messageDigestInstance = MessageDigest.getInstance(parts[1], parts[0]);
        testBytes = new byte[size];
    }

    @Benchmark
    public void digest(Blackhole bh) {
        bh.consume(messageDigestInstance.digest(testBytes));
    }

    @Benchmark
    public void digestMD5_inc(Blackhole bh) throws IOException {
        forEachBufferIn(testBytes, (buffer, len) -> messageDigestInstance.update(buffer, 0, len));
        bh.consume(messageDigestInstance.digest());
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
