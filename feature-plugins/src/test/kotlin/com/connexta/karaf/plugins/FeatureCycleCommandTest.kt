package com.connexta.karaf.plugins

import org.hamcrest.CoreMatchers.containsString
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class FeatureCycleCommandTest {
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
        val output = ByteArrayOutputStream()
        System.setOut(PrintStream(output))
        command.execute()
        assertThat(output.toString(), containsString("|>"))
    }

    @Test
    fun `there should be no output if there are no cycles`() {
        val output = ByteArrayOutputStream()
        System.setOut(PrintStream(output))
        val emptyFeaturesService = mockFeaturesService(listOf())
        command.setFeaturesService(emptyFeaturesService)
        command.execute()
        assertThat(output.toString(), containsString("No cycles found"))
    }
}