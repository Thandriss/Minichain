package ya.makhonko

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import ya.makhonko.Server.flagFirst
import ya.makhonko.Server.toSend
import ya.makhonko.Server.port
import ya.makhonko.plugins.configureRouting

fun main(args: Array<String>) {
    cmdParser(args)
    embeddedServer(Netty, port = port, module = Application::module).start(wait = true)
}

fun cmdParser(args: Array<String>) {
    val sumOptions = Options()
    val nodeOption = Option("n", "nodes", true, "Nodes in format: 127.0.0.1:8080,127.0.0.1:8081")
    nodeOption.args = 1
    nodeOption.setOptionalArg(false)
    val portOption = Option("p", "port", true, "Input number of port to open the server")
    portOption.args = 1
    portOption.setOptionalArg(false)
    portOption.argName = "port "
    val firstOption = Option("f", "first", false, "Generation of the first block")
    firstOption.setOptionalArg(true)
    firstOption.argName = "first block"
    val dbOption = Option("d", "database", true, "Set name for database")
    dbOption.args = 1
    dbOption.setOptionalArg(true)
    sumOptions.addOption(portOption)
    sumOptions.addOption(firstOption)
    sumOptions.addOption(nodeOption)
    sumOptions.addOption(dbOption)
    val defaultParser = DefaultParser()
    val parsed = defaultParser.parse(sumOptions, args)
    if (parsed.hasOption("p")) {
        val parsedPort = parsed.getOptionValues("p")[0].toIntOrNull()
            ?: throw IllegalArgumentException("Port is not a number!")
        port = parsedPort
    } else {
        throw IllegalArgumentException("Clarify port!")
    }
    if (parsed.hasOption("n")) {
        val nodes = parsed.getOptionValues("n")[0]
        val divNodes = nodes.split(",")
        if (divNodes.any { !it.matches(Regex("((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}:\\d{1,5}")) }) {
            throw IllegalArgumentException("Wrong address!")
        }
        toSend = divNodes.toSet()
    } else {
        throw IllegalArgumentException("Clarify nodes!")
    }
    if (parsed.hasOption("f")) flagFirst.set(true) else flagFirst.set(false)
}

fun Application.module() {
    val blockProducing = BlockProduction()
    launch(Dispatchers.IO) {
        configureRouting(blockProducing)
    }
    launch(Dispatchers.IO) {
        blockProducing.produceBlocks()
    }
}