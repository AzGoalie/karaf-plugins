package com.connexta.karaf.plugins

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.apache.karaf.features.Dependency
import org.apache.karaf.features.Feature
import org.apache.karaf.features.FeaturesService
import org.apache.karaf.features.Repository
import java.util.concurrent.atomic.AtomicInteger

private val idCounter = AtomicInteger()

private const val version = "1.0.0"

fun mockFeature(
    name: String = "Feature${idCounter.getAndIncrement()}",
    dependencies: List<String> = emptyList()
): Feature =
    mock {
        on { id } doReturn "id${idCounter.getAndIncrement()}"
        on { getName() } doReturn name
        on { version } doReturn version
        val mockDependencies = dependencies.map { name ->
            mock<Dependency> {
                on { getName() } doReturn name
                on { version } doReturn version
            }
        }.toList()
        on { getDependencies() } doReturn mockDependencies
    }

fun mockFeaturesService(
    features: List<Feature>,
    repositories: List<Repository> = emptyList()
): FeaturesService =
    mock {
        on { listInstalledFeatures() } doReturn features.toTypedArray()
        on { listRepositories() } doReturn repositories.toTypedArray()
        features.forEach { f ->
            on { getFeature(f.name, f.version) } doReturn f
        }
    }

fun mockFeaturesService(features: List<Feature>): FeaturesService =
    mockFeaturesService(features, listOf(mockRepository(features = features)))

fun mockRepository(
    features: List<Feature> = emptyList()
): Repository =
    mock {
        on { name } doReturn "repository ${idCounter.getAndIncrement()}"
        on { getFeatures() } doReturn features.toTypedArray()
    }