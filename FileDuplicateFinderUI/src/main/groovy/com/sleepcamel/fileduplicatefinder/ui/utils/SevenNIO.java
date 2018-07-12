package com.sleepcamel.fileduplicatefinder.ui.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

//https://bugs.openjdk.java.net/browse/JDK-8008469
public class SevenNIO {

    static class Counter extends SimpleFileVisitor<Path> {
        private int dirCount;
        private int fileCount;
        int dirCount() {
            return dirCount;
        }
        int fileCount() {
            return fileCount;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            dirCount++;
            return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attrs) {
            fileCount++;
            return FileVisitResult.CONTINUE;
        }
    }

    public static void main(String[] args) throws IOException {
        for (FileStore store: FileSystems.getDefault().getFileStores()) {
            System.out.printf("%s: %s%n", store.name(), store.type());
        }
        System.exit(0);

        Path top = FileSystems.getDefault().getPath("z:\\");//args[0]);
        Counter counter = new Counter();
        long start = System.currentTimeMillis();
        Files.walkFileTree(top, EnumSet.noneOf(FileVisitOption.class), 2, counter);
        long end = System.currentTimeMillis();
        System.out.format("Found %d directories and %d files in %d ms %n", counter.dirCount(), counter.fileCount,
        (end - start));
    }
}