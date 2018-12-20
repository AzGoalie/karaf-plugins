package com.connexta.karaf.plugins

import org.apache.karaf.features.Feature
import org.apache.karaf.shell.api.action.Command
import org.apache.karaf.shell.api.action.Option
import org.apache.karaf.shell.api.action.lifecycle.Service
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.traverse.DepthFirstIterator
import java.io.File

@Command(scope = "feature", name = "dump", description = "Dumps the hierarchy of installed features")
@Service
class FeatureDumpCommand : BaseFeatureCommand() {
    @Option(
        name = "-p",
        aliases = ["--print-screen"],
        description = "Prints the output to the screen instead of saving to a folder",
        required = false,
        multiValued = false
    )
    var printToScreen: Boolean = false

    @Option(
        name = "-f",
        aliases = ["--folder"],
        description = "Folder to save feature trees",
        required = false,
        multiValued = false
    )
    var outputFolder: String = "features"

    override fun execute(): Any? {
        val folder = File(outputFolder)
        if (!printToScreen && !folder.exists() && !folder.mkdirs()) {
            println("Failed to create output folder $outputFolder")
        }

        featuresService.listRepositories()
            .map { repository ->
                repository.name to repository.features.filter { featuresService.isInstalled(it) }
            }
            .filter { it.second.isNotEmpty() }
            .forEach { (repoName, rootFeatures) ->
                val tree = rootFeatures.joinToString("\n") { printFeatures(it, featureGraph) }
                if (printToScreen) {
                    println("Repository $repoName")
                    println(tree)
                } else {
                    File("$outputFolder/$repoName.txt").writeText(tree)
                }
            }

        return null
    }

    private fun printFeatures(feature: Feature, graph: Graph<Feature, DefaultEdge>): String {
        val sb = StringBuilder()
        val iter = DepthFirstIterator(graph, feature)
        while (iter.hasNext()) {
            val f = iter.next()
            val padding = "\t".repeat(iter.stack.filter { it == DepthFirstIterator.SENTINEL }.size)
            sb.append("$padding $f").append("\n")
            f.bundles.forEach { sb.append("\t$padding ${it.location}").append("\n") }
        }
        return sb.toString()
    }
}