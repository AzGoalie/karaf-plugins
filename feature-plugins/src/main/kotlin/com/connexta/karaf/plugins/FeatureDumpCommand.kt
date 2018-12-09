package com.connexta.karaf.plugins

import com.connexta.karaf.plugins.writers.DependencyTreeWriter
import org.apache.karaf.shell.api.action.Command
import org.apache.karaf.shell.api.action.Option
import org.apache.karaf.shell.api.action.lifecycle.Service
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
            .map { DependencyGraph(featureCache, it) }
            .filter(DependencyGraph::isNotEmpty)
            .forEach {
                val dependencyTree = DependencyTreeWriter.printAsTree(it, featureCache)
                if (!printToScreen) {
                    File("$outputFolder/${it.repository.name}.txt")
                        .writeText(dependencyTree)
                } else {
                    println(dependencyTree)
                }
            }

        return null
    }
}