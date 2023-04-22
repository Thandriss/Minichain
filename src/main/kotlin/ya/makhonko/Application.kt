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
    port = System.getenv("PORT").toInt()
    val nodes = System.getenv("NODES")
    val divNodes = nodes.split(",")
    toSend = divNodes.toSet()
    if (System.getenv("FIRST") == "1")
        flagFirst.set(true)
    else
        flagFirst.set(false)
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