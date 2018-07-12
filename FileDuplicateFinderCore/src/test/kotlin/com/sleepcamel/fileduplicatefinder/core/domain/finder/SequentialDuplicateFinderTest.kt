package com.sleepcamel.fileduplicatefinder.core.domain.finder

import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper
import com.sleepcamel.fileduplicatefinder.core.domain.LocalPathWithAttributes
import org.junit.Assert
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes


class SequentialDuplicateFinderTest{

    @Test
    fun testNoLoop(){
        val systemTmp = Paths.get(System.getProperty("java.io.tmpdir"))
        val parent = Files.createTempDirectory(systemTmp, "sdft")
        val recursiveChild = parent.resolve(Paths.get("bad"))
        Assert.assertNotNull(Files.createSymbolicLink(recursiveChild, parent))

        val parentAsFW = FileWrapper(LocalPathWithAttributes(parent, Files.readAttributes(parent, BasicFileAttributes::class.java)))

        val sequentialDuplicateFinder = SequentialDuplicateFinder(mutableListOf(parentAsFW))
        val scan = sequentialDuplicateFinder.scan()
        while( scan.isActive ){
            print(scan.isActive)
        }
        sequentialDuplicateFinder.findDuplicates()
        while( !sequentialDuplicateFinder.findProgress.finishedFindingDuplicates ){
            Thread.sleep(500L)
            print(sequentialDuplicateFinder.findProgress.totalFiles)
        }
    }

    @Test
    fun testNoSubChildLoop(){
        val systemTmp = Paths.get(System.getProperty("java.io.tmpdir"))
        val parent = Files.createTempDirectory(systemTmp, "sdft")
        val recursiveChild = parent.resolve(Paths.get("bad"))
        Assert.assertNotNull(Files.createSymbolicLink(recursiveChild, systemTmp))

        val parentAsFW = FileWrapper(LocalPathWithAttributes(parent, Files.readAttributes(parent, BasicFileAttributes::class.java)))

        val sequentialDuplicateFinder = SequentialDuplicateFinder(mutableListOf(parentAsFW))
        val scan = sequentialDuplicateFinder.scan()
        while( scan.isActive ){
            print(scan.isActive)
        }
        sequentialDuplicateFinder.findDuplicates()
        while( !sequentialDuplicateFinder.findProgress.finishedFindingDuplicates ){
            Thread.sleep(500L)
            print(sequentialDuplicateFinder.findProgress.totalFiles)
        }
    }

    // TODO None of these tests fail on Windows, they should!
    @Test
    fun testNoSubSubChildLoop(){
        val systemTmp = Paths.get(System.getProperty("java.io.tmpdir"))
        val parent = Files.createTempDirectory(systemTmp, "sdft")
        val child = Files.createDirectory(parent.resolve(Paths.get("child")))
        Files.createFile(child.resolve(Paths.get("justafile"))).toFile().writeText("JUSTTEXT")
        val recursiveChild = child.resolve(Paths.get("bad"))
        Assert.assertNotNull(Files.createSymbolicLink(recursiveChild, child))

        val parentAsFW = FileWrapper(LocalPathWithAttributes(parent, Files.readAttributes(parent, BasicFileAttributes::class.java)))
        val sequentialDuplicateFinder = SequentialDuplicateFinder(mutableListOf(parentAsFW))
        val scan = sequentialDuplicateFinder.scan()
        while( scan.isActive ){
            print(scan.isActive)
        }
        sequentialDuplicateFinder.findDuplicates()
        while( !sequentialDuplicateFinder.findProgress.finishedFindingDuplicates ){
            Thread.sleep(500L)
            print(sequentialDuplicateFinder.findProgress.totalFiles)
        }
    }
}