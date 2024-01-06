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
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 1)
@Warmup(iterations = 1, time = 3)
@Measurement(time = 3)
public class DigestInstanceBench {

    @Benchmark
    public void getInstanceMD5(Blackhole bh) throws NoSuchAlgorithmException {
        bh.consume(MessageDigest.getInstance("MD5"));
    }

    @Benchmark
    public void getInstanceMD5BC(Blackhole bh) throws NoSuchAlgorithmException, NoSuchProviderException {
        bh.consume(MessageDigest.getInstance("MD5", BouncyCastleProvider.PROVIDER_NAME));
    }

    @Benchmark
    public void getInstanceB2BC(Blackhole bh) throws NoSuchAlgorithmException, NoSuchProviderException {
        bh.consume(MessageDigest.getInstance("BLAKE2B-256", BouncyCastleProvider.PROVIDER_NAME));
    }

    @Benchmark
    public void getInstanceB3BC(Blackhole bh) throws NoSuchAlgorithmException, NoSuchProviderException {
        bh.consume(MessageDigest.getInstance("BLAKE3-256", BouncyCastleProvider.PROVIDER_NAME));
    }

    @Benchmark
    public void getInstanceBlake(Blackhole bh) {
        bh.consume(Blake3.initHash());
    }
}
