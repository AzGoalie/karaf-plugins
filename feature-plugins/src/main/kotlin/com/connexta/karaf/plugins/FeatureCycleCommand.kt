package com.connexta.karaf.plugins

import org.apache.karaf.shell.api.action.Command
import org.apache.karaf.shell.api.action.Option
import org.apache.karaf.shell.api.action.lifecycle.Service
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles

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

    override fun execute(): Any? {
        val cycles = SzwarcfiterLauerSimpleCycles(featureGraph).findSimpleCycles()
        if (cycles.isEmpty()) {
            println("No cycles found")
        } else {
            cycles.forEach { features ->
                println("|> ${features.first()}")
                (1 until features.size - 1).forEach { println("| ${features[it]}") }
                println("| ${features.last()}")
                println()
            }
        }
        return null
    }
}