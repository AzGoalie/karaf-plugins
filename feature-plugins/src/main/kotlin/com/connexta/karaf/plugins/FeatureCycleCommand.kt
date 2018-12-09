package com.connexta.karaf.plugins

import com.connexta.karaf.plugins.writers.DependencyTreeWriter
import org.apache.karaf.shell.api.action.Command
import org.apache.karaf.shell.api.action.Option
import org.apache.karaf.shell.api.action.lifecycle.Service
import java.io.File

@Command(scope = "feature", name = "cycles", description = "Prints cycles found in dependency graph")
@Service
class FeatureCycleCommand : BaseFeatureCommand() {
    @Option(
        name = "-p",
        aliases = ["--print-screen"],
        description = "Prints the output to the screen instead of saving to a folder",
        required = false,
        multiValued = false
    )
    var printToScreen: Boolean = true

    @Option(
        name = "-f",
        aliases = ["--folder"],
        description = "Folder to save cycles to",
        required = false,
        multiValued = false
    )
    var outputFolder: String = "features/cycles"

    override fun execute(): Any? {
        val folder = File(outputFolder)
        if (!printToScreen && !folder.exists() && !folder.mkdirs()) {
            println("Failed to create output folder $outputFolder")
        }

        featuresService.listRepositories()
            .map { DependencyGraph(featureCache, it) }
            .filter(DependencyGraph::isNotEmpty)
            .forEach {
                if (it.cycles.isNotEmpty()) {
                    val cycles = DependencyTreeWriter.printCycles(it)
                    if (printToScreen) {
                        println("${it.repository.name} has cycles!")
                        println(cycles)
                    } else {
                        File("$outputFolder/${it.repository.name}.txt")
                            .writeText(cycles)
                    }
                }
            }
        return null
    }
}