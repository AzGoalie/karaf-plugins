package com.connexta.karaf.plugins

import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test

class FeatureCacheTest {
    private val feature = mockFeature(dependencies = listOf("dep1"))
    private val dependentFeature = mockFeature("dep1")
    private val featuresService = mockFeaturesService(listOf(feature, dependentFeature))

    private val cache = FeatureCache(featuresService)

    @Test
    fun `cache should contain feature`() {
        assertThat(cache[feature.id], equalTo(feature))
    }

    @Test
    fun `cache should contain dependent feature`() {
        assertThat(cache[dependentFeature.id], equalTo(dependentFeature))
    }

    @Test
    fun `dependent feature should be a dependency of feature`() {
        assertThat(cache.getDependencies(feature), equalTo(setOf(dependentFeature)))
    }

    @Test
    fun `feature should be installed`() {
        assertThat(cache.isInstalled(feature), `is`(true))
    }

    @Test
    fun `dependent feature should be installed`() {
        assertThat(cache.isInstalled(dependentFeature), `is`(true))
    }

    @Test
    fun `a non installed feature should return false`() {
        assertThat(cache.isInstalled(mockFeature()), `is`(false))
    }

    @Test
    fun `a non installed feature should have no dependencies`() {
        assertThat(cache.getDependencies(mockFeature()), equalTo(emptySet()))
    }

    @Test
    fun `a non installed feature should return null`() {
        assertThat(cache[mockFeature().id], nullValue())
    }
}