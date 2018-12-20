package com.connexta.karaf.plugins

import org.apache.karaf.bundle.core.BundleService
import org.apache.karaf.features.Feature
import org.apache.karaf.features.FeaturesService
import org.jgrapht.Graphs
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.traverse.BreadthFirstIterator
import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext

class DependencyGraph(
    featuresService: FeaturesService,
    bundleService: BundleService,
    bundleContext: BundleContext
) {
    private val featureGraph = DefaultDirectedGraph<Feature, DefaultEdge>(DefaultEdge::class.java)
    private val bundleGraph = DefaultDirectedGraph<Bundle, DefaultEdge>(DefaultEdge::class.java)
    private val featureToBundles: Map<Feature, Set<Bundle>>

    init {
        featureToBundles = featuresService.listInstalledFeatures()
            .associate { feature ->
                feature to feature.bundles.mapNotNull { bundle ->
                    bundleContext.getBundle(bundle.location)
                }.filter { it.bundleId >= bundleService.systemBundleThreshold }.toSet()
            }

        val features = featuresService.listInstalledFeatures()
        features.forEach { featureGraph.addVertex(it) }
        features.forEach { feature ->
            feature.dependencies.map { featuresService.getFeature(it.name, it.version) }
                .forEach { dependency -> featureGraph.addEdge(feature, dependency) }
        }

        val bundles = bundleContext.bundles.filter { it.bundleId >= bundleService.systemBundleThreshold }
        bundles.forEach { bundleGraph.addVertex(it) }
        bundles.forEach { bundle ->
            bundleService.getWiredBundles(bundle).values.filter { bundles.contains(it) }
                .forEach { bundleGraph.addEdge(bundle, it) }
        }
    }

    fun getDirectBundles(feature: Feature): Set<Bundle> = featureToBundles[feature]!!
    fun getDirectFeatures(feature: Feature): Set<Feature> = Graphs.successorListOf(featureGraph, feature).toSet()
    fun getDirectDependencies(bundle: Bundle): Set<Bundle> = Graphs.successorListOf(bundleGraph, bundle).toSet()
    fun getProvidingFeatures(bundle: Bundle): Set<Feature> = featureToBundles.filter { it.value.contains(bundle) }.keys

    fun getTransitiveFeatures(feature: Feature): Set<Feature> {
        val iter = BreadthFirstIterator(featureGraph, feature)
        val result = mutableSetOf<Feature>()

        // Skip the passed in feature
        iter.next()
        while (iter.hasNext()) {
            result.add(iter.next())
        }

        return result
    }

    fun getTransitiveBundles(feature: Feature): Set<Bundle> {
        val iter = BreadthFirstIterator(featureGraph, feature)
        val result = mutableSetOf<Bundle>()

        while (iter.hasNext()) {
            result.addAll(featureToBundles[iter.next()]!!)
        }

        return result
    }

    fun getTransitiveDependencies(bundle: Bundle): Set<Bundle> {
        val iter = BreadthFirstIterator(bundleGraph, bundle)
        val result = mutableSetOf<Bundle>()

        // Skip the passed in bundle
        iter.next()
        while (iter.hasNext()) {
            result.add(iter.next())
        }

        return result
    }

}
