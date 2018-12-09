package com.connexta.karaf.plugins.writers

import com.connexta.karaf.plugins.DependencyGraph
import com.connexta.karaf.plugins.FeatureCache
import org.apache.karaf.features.Feature

object DependencyTreeWriter {
    fun printAsTree(dependencyGraph: DependencyGraph, cache: FeatureCache): String {
        val sb = StringBuilder()
        sb.append("+- ${dependencyGraph.repository.name}\n")
        dependencyGraph.installedFeatures
            .forEach { printFeature(cache, sb, it, 1) }
        return sb.toString()
    }

    fun printCycles(dependencyGraph: DependencyGraph): String {
        val cycles = dependencyGraph.cycles
        val sb = StringBuilder()
        cycles.forEach { features ->
            var indent = 1
            sb.append("|> ${features.first().name}\n")
            (1 until features.size - 1).forEach { sb.append("|  ${" ".repeat(indent++)} ${features[it].name}\n") }
            sb.append("|__${"_".repeat(indent)} ${features.last().name}\n")
            sb.append("\n")
        }
        return sb.toString()
    }

    private fun printFeature(
        cache: FeatureCache,
        sb: StringBuilder,
        feature: Feature,
        indent: Int,
        ancestors: List<Feature> = emptyList()
    ) {
        val padding = "  ".repeat(indent) + "+-"
        sb.append("$padding ${feature.name}\n")
        if (cache.getDependencies(feature).isNotEmpty()) {
            sb.append("  $padding Features\n")
            cache.getDependencies(feature).forEach { dependency ->
                if (ancestors.contains(dependency)) {
                    sb.append("    $padding ${dependency.name} -- Cycle Found!\n")
                } else {
                    printFeature(cache, sb, dependency, indent + 2, ancestors + feature)
                }
            }
        }
        if (feature.bundles.isNotEmpty()) {
            sb.append("  $padding Bundles\n")
            feature.bundles.forEach { sb.append("    $padding ${it.location}\n") }
        }
    }
}