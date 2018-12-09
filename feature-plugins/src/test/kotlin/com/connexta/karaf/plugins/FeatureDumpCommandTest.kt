package com.connexta.karaf.plugins

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FeatureDumpCommandTest {
    @Rule
    @JvmField
    val tmpFolder = TemporaryFolder()

    private val feature = mockFeature()
    private val repository = mockRepository(listOf(feature))
    private val featureService = mockFeaturesService(listOf(feature), listOf(repository))
    private val command by lazy {
        val tmp = FeatureDumpCommand()
        tmp.setFeaturesService(featureService)
        tmp
    }

    @Test
    fun `should output to folder`() {
        val folder = tmpFolder.newFolder("features")
        command.outputFolder = folder.canonicalPath
        command.execute()
        assertThat(folder.list(), equalTo(arrayOf("${repository.name}.txt")))
    }

    @Test
    fun `should create the output folder if it doesn't exist`() {
        val folder = tmpFolder.newFolder()
        val path = folder.canonicalPath + "/test"
        command.outputFolder = path
        command.execute()
        assertThat(folder.list(), equalTo(arrayOf("test")))
    }

    @Test
    fun `when printing to the screen, should not output to folder`() {
        val folder = tmpFolder.newFolder("features")
        command.outputFolder = folder.canonicalPath
        command.printToScreen = true
        command.execute()
        assertThat(folder.list(), equalTo(emptyArray()))
    }

    @Test
    fun `should not output empty repositories`() {
        command.setFeaturesService(mockFeaturesService(emptyList()))
        val folder = tmpFolder.newFolder("features")
        command.outputFolder = folder.canonicalPath
        command.execute()
        assertThat(folder.list(), equalTo(emptyArray()))
    }
}