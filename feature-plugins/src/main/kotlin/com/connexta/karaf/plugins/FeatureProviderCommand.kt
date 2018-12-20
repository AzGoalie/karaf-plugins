package com.connexta.karaf.plugins

import org.apache.karaf.features.Feature
import org.apache.karaf.shell.api.action.Argument
import org.apache.karaf.shell.api.action.Command
import org.apache.karaf.shell.api.action.Option
import org.apache.karaf.shell.api.action.lifecycle.Service
import org.osgi.framework.Bundle


data class Dependency(val bundle: Bundle? = null, val feature: Feature? = null, val isTransitive: Boolean = false) {
    init {
        require(bundle != null || feature != null)
        require(!(bundle == null && feature == null))
    }

    val isBundle = bundle != null
    val isFeature = !isBundle
}

@Command(
    scope = "feature",
    name = "provider",
    description = "Finds the minimum features/bundles needed for a given bundle ID"
)
@Service
class FeatureGraphCommand : BaseFeatureCommand() {
    @Argument(
        index = 0,
        name = "id",
        description = "The bundle ID or name or name/version",
        required = true,
        multiValued = false
    )
    lateinit var id: String

    @Option(name = "-d", aliases = ["--print-dependencies"])
    var printDependencies = false

    @Option(name = "-f", aliases = ["--print-features"])
    var printFeatures = false

    @Option(name = "-t", aliases = ["--print-tree"])
    var printTree = false

    override fun execute(): Any? {
        val bundle = bundleService.getBundle(id)
        val dependencyGraph = DependencyGraph(featuresService, bundleService, bundleContext)

        val dependencies = dependencyGraph.getDirectDependencies(bundle)
        if (dependencies.isEmpty()) {
            println("$bundle has no dependencies")
            if (printFeatures) {
                val features = dependencyGraph.getProvidingFeatures(bundle)
                println("Providing Features:")
                println("\t${features.joinToString("\n\t")}")
            }
            return null
        }

        if (printDependencies) {
            println("Direct bundle dependencies: \n\t${dependencies.joinToString("\n\t")}\n")
        }

        // Features providing direct dependencies
        val baseFeatures: Map<Bundle, Set<Feature>> =
            dependencies.associate { dep -> dep to dependencyGraph.getProvidingFeatures(dep) }
        if (printFeatures) {
            println("Features providing direct dependencies:")
            printFeatures(baseFeatures, dependencies, dependencyGraph)
        }

        val pickedDependencies = chooseDependencies(baseFeatures, dependencyGraph)
        // TODO: If we cleanup our feature files, we won't need to do this
        val filtered = pickedDependencies.filter { (bundle, feature) ->
            val features = pickedDependencies.filter(Dependency::isFeature)
            if (feature != null) {
                !features.flatMap { dependencyGraph.getTransitiveFeatures(it.feature!!) }
                    .contains(feature)
            } else {
                !features.flatMap { dependencyGraph.getTransitiveBundles(it.feature!!) }
                    .contains(bundle)
            }
        }.toSet()

        if (printTree) {
            printTree(filtered, dependencyGraph)
        } else {
            println("Chosen Features:")
            filtered.filter(Dependency::isFeature).forEach { println("\t${it.feature}") }
            println("Chosen Bundles:")
            filtered.filter(Dependency::isBundle).forEach { println("\t${it.bundle}") }
        }

        return null
    }

    private fun printFeatures(
        featureMap: Map<Bundle, Set<Feature>>,
        dependencies: Set<Bundle>,
        dependencyGraph: DependencyGraph
    ) {
        featureMap.forEach { (bundle, providingFeatures) ->
            println("\tBundle: $bundle")
            println("\tFeatures:")
            providingFeatures.forEach { feature ->
                val bundles = dependencyGraph.getDirectBundles(feature)
                val features = dependencyGraph.getDirectFeatures(feature)

                val satisfyingDirectDependencies = bundles.intersect(dependencies).size
                val extraFeatures = dependencyGraph.getTransitiveFeatures(feature).size
                val extraFeatureBundles = dependencyGraph.getTransitiveBundles(feature).size
                println("\t\t$feature")
                println("\t\t\t$satisfyingDirectDependencies satisfied direct dependencies")
                println("\t\t\t${bundles.size} direct bundles")
                println("\t\t\t${features.size} direct features")
                println("\t\t\t$extraFeatures total features")
                println("\t\t\t$extraFeatureBundles total bundles")
            }
        }
    }

    private fun chooseDependencies(
        featureMap: Map<Bundle, Set<Feature>>,
        dependencyGraph: DependencyGraph
    ): Set<Dependency> {
        val picked = featureMap.map { (bundle, providingFeatures) ->
            val numberOfBundleDependencies = when (val size = dependencyGraph.getTransitiveDependencies(bundle).size) {
                0 -> 1
                else -> size
            }
            val (pickedFeature, numberOfFeatureDependencies) = providingFeatures.map {
                it to (dependencyGraph.getTransitiveBundles(it).size)
            }.sortedBy { it.second }.first()

            val directBundles = dependencyGraph.getDirectBundles(pickedFeature)
            val satisfiedDependencies = directBundles.intersect(featureMap.keys).size
            if ((satisfiedDependencies / directBundles.size.toFloat() >= .7) || numberOfFeatureDependencies <= numberOfBundleDependencies * 4) {
                Dependency(feature = pickedFeature)
            } else {
                Dependency(bundle = bundle)
            }
        }.toSet()

        val bundleDependencies = picked.filter(Dependency::isBundle)
            .flatMap { dependencyGraph.getTransitiveDependencies(it.bundle!!) }
            .filterNot { picked.contains(Dependency(bundle = it)) }
            .map { Dependency(bundle = it, isTransitive = true) }
            .toSet()

        return picked + bundleDependencies
    }

    private fun printTree(dependencies: Set<Dependency>, dependencyGraph: DependencyGraph) {
        println("Chosen Features:")
        dependencies.filter(Dependency::isFeature).map { it.feature!! }
            .forEach { printFeatureTree(it, dependencyGraph) }
        println("Chosen Bundles:")
        dependencies.filter(Dependency::isBundle).filterNot(Dependency::isTransitive).map { it.bundle!! }
            .forEach { printBundleTree(it, dependencyGraph) }
    }

    private fun printFeatureTree(feature: Feature, dependencyGraph: DependencyGraph, indent: Int = 1) {
        val padding = "\t".repeat(indent)
        println("$padding $feature")
        dependencyGraph.getDirectBundles(feature).forEach { println("\t$padding $it") }
        dependencyGraph.getDirectFeatures(feature).forEach { printFeatureTree(it, dependencyGraph, indent + 1) }
    }

    private fun printBundleTree(
        bundle: Bundle,
        dependencyGraph: DependencyGraph,
        indent: Int = 1
    ) {
        val padding = "\t".repeat(indent)
        println("$padding $bundle")
        dependencyGraph.getTransitiveDependencies(bundle).forEach { printBundleTree(it, dependencyGraph, indent + 1) }
    }
}