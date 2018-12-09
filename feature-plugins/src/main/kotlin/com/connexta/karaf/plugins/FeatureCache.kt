package com.connexta.karaf.plugins

import org.apache.karaf.features.Feature
import org.apache.karaf.features.FeaturesService

class FeatureCache(private val featuresService: FeaturesService) {
    private val cache: Map<String, Feature>
    private val featureDependencies: Map<Feature, Set<Feature>>

    init {
        with(featuresService) {
            cache = listInstalledFeatures()
                .associateBy(Feature::getId)

            featureDependencies = listInstalledFeatures()
                .associateBy({ it }) {
                    it.dependencies
                        .map { dep -> featuresService.getFeature(dep.name, dep.version) }
                        .toSet()
                }
        }
    }

    operator fun get(id: String) = cache[id]

    fun getDependencies(feature: Feature) = featureDependencies[feature] ?: emptySet()

    fun isInstalled(feature: Feature) = cache.containsKey(feature.id)
}