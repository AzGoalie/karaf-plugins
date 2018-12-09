package com.connexta.karaf.plugins

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FeatureCycleCommandTest {
    @Rule
    @JvmField
    val tmpFolder = TemporaryFolder()

    private val features = listOf(
        mockFeature("f1", listOf("f2")),
        mockFeature("f2", listOf("f1"))
    )
    private val repository = mockRepository(features)
    private val featuresService = mockFeaturesService(
        features,
        listOf(repository)
    )
    private val command by lazy {
        val tmp = FeatureCycleCommand()
        tmp.setFeaturesService(featuresService)
        tmp.printToScreen = false
        tmp
    }

    @Test
    fun `should print when there are cycles`() {
        val folder = tmpFolder.newFolder("cycles")
        command.outputFolder = folder.canonicalPath
        command.execute()
        assertThat(folder.list(), equalTo(arrayOf("${repository.name}.txt")))
    }

    @Test
    fun `there should be no output if there are no cycles`() {
        val folder = tmpFolder.newFolder("cycles")
        val emptyFeaturesService = mockFeaturesService(listOf())
        command.setFeaturesService(emptyFeaturesService)
        command.outputFolder = folder.canonicalPath
        command.execute()
        assertThat(folder.list(), equalTo(emptyArray()))
    }
}