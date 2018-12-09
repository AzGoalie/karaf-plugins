package com.connexta.karaf.plugins.writters

import com.connexta.karaf.plugins.*
import com.connexta.karaf.plugins.writers.DependencyTreeWriter
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class DependencyTreeWriterTest {
    private val features = listOf(
        mockFeature("f1", listOf("f2")),
        mockFeature("f2", listOf("f1"))
    )
    private val repository = mockRepository(features)
    private val cache = FeatureCache(mockFeaturesService(features, listOf(repository)))
    private val dependencyGraph = DependencyGraph(cache, repository)

    @Test
    fun `should have 2 cycles with 2 lines each`() {
        val result = DependencyTreeWriter.printCycles(dependencyGraph)
            .lines()
            .filter { it.isNotBlank() }
        assertThat(result.size, equalTo(4))
    }

    @Test
    fun `should print cycle and not just root`() {
        val features = listOf(
            mockFeature("f1", listOf("f2")),
            mockFeature("f2", listOf("f3")),
            mockFeature("f3", listOf("f2"))
        )
        val cache = FeatureCache(mockFeaturesService(features))
        val cycleGraph = DependencyGraph(cache, mockRepository(features))
        val result = DependencyTreeWriter.printCycles(cycleGraph).lines().filter { it.isNotBlank() }
        assertThat(result.size, equalTo(4))
    }

    @Test
    fun `should not print anything if no cycles`() {
        val noCycleGraph = DependencyGraph(cache, mockRepository())
        assertThat(DependencyTreeWriter.printCycles(noCycleGraph), equalTo(""))
    }

    @Test
    fun `should print repository in a tree`() {
        val result = DependencyTreeWriter.printAsTree(dependencyGraph, cache).lines()
            .filter { it.isNotBlank() }
        // Repository name, f1/features|f2/Features|f1, f2|Features/f1/Features|f2
        assertThat(result.size, equalTo(11))
    }
}