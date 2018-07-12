package com.sleepcamel.fileduplicatefinder.core.domain

import org.junit.Assert
import org.junit.Test
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

class DuplicateFinderProgressTest {

    @Test
    fun serDeser() {
        val duplicateFinderProgress = DuplicateFinderProgress()
        val path = File("").toPath()
        val attrs = Files.readAttributes(path, BasicFileAttributes::class.java)
        duplicateFinderProgress.fileFound(FileWrapper(LocalPathWithAttributes(path, attrs)))
        val createTempFile = File.createTempFile("dfp", "test")
        createTempFile.outputStream().use {
            ObjectOutputStream(it).writeObject(duplicateFinderProgress)
        }
        val dfp = createTempFile.inputStream().use {
            ObjectInputStream(it).readObject() as DuplicateFinderProgress
        }
        Assert.assertEquals(dfp.foundFiles.size, duplicateFinderProgress.foundFiles.size)
        println("a")
    }
}