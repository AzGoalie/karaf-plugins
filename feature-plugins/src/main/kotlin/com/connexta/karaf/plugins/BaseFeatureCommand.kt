package com.connexta.karaf.plugins

import org.apache.karaf.bundle.core.BundleService
import org.apache.karaf.features.Feature
import org.apache.karaf.features.FeaturesService
import org.apache.karaf.shell.api.action.Action
import org.apache.karaf.shell.api.action.lifecycle.Reference
import org.jetbrains.annotations.TestOnly
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.osgi.framework.BundleContext

abstract class BaseFeatureCommand : Action {
    @Reference
    protected lateinit var featuresService: FeaturesService

    @Reference
    protected lateinit var bundleService: BundleService

    @Reference
    protected lateinit var bundleContext: BundleContext

    @TestOnly
    internal fun setFeaturesService(featuresService: FeaturesService) {
        this.featuresService = featuresService
    }

    protected val featureGraph by lazy { createFeatureGraph() }

    private fun createFeatureGraph(): Graph<Feature, DefaultEdge> {
        val featureGraph = DefaultDirectedGraph<Feature, DefaultEdge>(DefaultEdge::class.java)
        val features = featuresService.listInstalledFeatures()
        features.forEach { featureGraph.addVertex(it) }
        features.forEach { feature ->
            feature.dependencies.map { featuresService.getFeature(it.name, it.version) }
                .forEach { dependency -> featureGraph.addEdge(feature, dependency) }
        }
        return featureGraph
    }
}