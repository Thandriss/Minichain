package ya.makhonko.plugins

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ya.makhonko.Block
import ya.makhonko.Block.Companion.getHash
import ya.makhonko.Block.Companion.isValid
import ya.makhonko.BlockProduction
import ya.makhonko.Server.flagFirst
import ya.makhonko.Server.port
import java.math.BigInteger
import java.util.logging.Logger

fun Application.configureRouting(blockProducing: BlockProduction) {
    Logger.getGlobal().info("Start at $port port")
    if (flagFirst.get()) {
        val producedBlock = blockProducing.initFirstBlock()
        Logger.getGlobal().info("Produced: $producedBlock")
        blockProducing.currentBlock.set(producedBlock)
        blockProducing.sendToNodes(producedBlock)
    }
    install(ContentNegotiation) {
        json()
    }
    routing {
        post("/update") {
            val stoppedBlock = blockProducing.currentBlock.get()
            val possibleBlock = call.receive<Block>()
            val hash = getHash(possibleBlock)
            if (stoppedBlock != null) {
                if (hash.isValid() &&
                    stoppedBlock.hash == possibleBlock.prev_hash &&
                    BigInteger(possibleBlock.index).subtract(BigInteger(stoppedBlock.index)) == BigInteger.ONE
                ) {
                    val isSet = blockProducing.currentBlock.compareAndSet(stoppedBlock, possibleBlock)
                    if (isSet) {
                        Logger.getGlobal().info("Updated: $possibleBlock")
                        blockProducing.sendToNodes(possibleBlock)
                    } else {
                        val block = blockProducing.findFresh()
                        Logger.getGlobal().info("Updated: $block")
                        blockProducing.currentBlock.set(block)
                        blockProducing.sendToNodes(block)
                    }
                }
            } else {
                Logger.getGlobal().info("Updated: $possibleBlock")
                blockProducing.currentBlock.set(possibleBlock)
                blockProducing.sendToNodes(possibleBlock)
            }
            call.respond(HttpStatusCode.Accepted)
        }
        get("/") {
            if (blockProducing.currentBlock.get() != null) {
                Logger.getGlobal().info("Send block: ${blockProducing.currentBlock.get()}")
                call.respond(blockProducing.currentBlock.get())
            }
        }
    }
}