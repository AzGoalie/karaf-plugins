package com.connexta.karaf.plugins

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class DependencyGraphTest {
    private val feature = mockFeature()
    private val featuresService = mockFeaturesService(listOf(feature))
    private val cache = FeatureCache(featuresService)

    @Test
    fun `an empty dependency graph should be empty`() {
        val graph = DependencyGraph(cache, mockRepository())
        assertThat(graph.isEmpty(), `is`(true))
    }

    @Test
    fun `an empty dependency graph should not be not empty`() {
        val graph = DependencyGraph(cache, mockRepository())
        assertThat(graph.isNotEmpty(), `is`(false))
    }

    @Test
    fun `an empty dependency graph should have no cycles`() {
        val graph = DependencyGraph(cache, mockRepository())
        assertThat(graph.cycles, equalTo(emptySet()))
    }

    @Test
    fun `a graph with an installed feature should not be empty`() {
        val graph = DependencyGraph(cache, mockRepository(listOf(feature)))
        assertThat(graph.isEmpty(), `is`(false))
    }

    @Test
    fun `a graph with an installed feature should be not empty`() {
        val graph = DependencyGraph(cache, mockRepository(listOf(feature)))
        assertThat(graph.isNotEmpty(), `is`(true))
    }

    @Test
    fun `a graph with an installed feature should have no cycles`() {
        val graph = DependencyGraph(cache, mockRepository(listOf(feature)))
        assertThat(graph.cycles, equalTo(emptySet()))
    }

    @Test
    fun `a graph with a cycle should have a cycle`() {
        val features = listOf(
            mockFeature("f1", listOf("f2")),
            mockFeature("f2", listOf("f1"))
        )
        val cycleCache = FeatureCache(mockFeaturesService(features))
        val graph = DependencyGraph(cycleCache, mockRepository(features))
        assertThat(graph.cycles, equalTo(setOf(features, features.reversed())))
    }

    @Test
    fun `a graph with a cycle that is not the root that is not the root should return a subtree`() {
        val features = listOf(
            mockFeature("f1", listOf("f2")),
            mockFeature("f2", listOf("f3")),
            mockFeature("f3", listOf("f2"))
        )
        val subTree = setOf(listOf(features[1], features[2]), listOf(features[2], features[1]))
        val cycleCache = FeatureCache(mockFeaturesService(features))
        val graph = DependencyGraph(cycleCache, mockRepository(features))
        assertThat(graph.cycles, equalTo(subTree))
    }
}