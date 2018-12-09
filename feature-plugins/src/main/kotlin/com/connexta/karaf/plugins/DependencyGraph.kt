package com.connexta.karaf.plugins

import org.apache.karaf.features.Feature
import org.apache.karaf.features.Repository

class DependencyGraph(
    private val cache: FeatureCache,
    val repository: Repository
) {
    internal val installedFeatures: Set<Feature> by lazy {
        repository.features
            .filter(cache::isInstalled)
            .toSet()
    }

    val cycles by lazy {
        installedFeatures
            .flatMap { findCycle(it) }
            .toSet()
    }

    private fun findCycle(feature: Feature, ancestors: List<Feature> = emptyList()): Set<List<Feature>> {
        val dependencies = cache.getDependencies(feature)
        return when {
            dependencies.isEmpty() -> return emptySet()
            ancestors.contains(feature) -> return setOf(ancestors.subList(ancestors.indexOf(feature), ancestors.size))
            else -> dependencies.flatMap { findCycle(it, ancestors + feature) }.toSet()
        }
    }

    fun isEmpty() = installedFeatures.isEmpty()

    fun isNotEmpty() = !isEmpty()
}