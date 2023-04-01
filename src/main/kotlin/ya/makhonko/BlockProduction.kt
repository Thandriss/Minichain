package ya.makhonko

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import ya.makhonko.Block.Companion.isValid
import ya.makhonko.Server.client
import java.math.BigInteger
import java.net.ConnectException
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Logger

class BlockProduction {
    val dbHelper = DataSaver()
    val currentBlock: AtomicReference<Block> = AtomicReference<Block>()
    val nodesToSend = Server.toSend
    var seed = 55555555555L

    suspend fun produceBlocks() {
        while (true) {
            val frozenBlock = currentBlock.get()
            if (frozenBlock != null) {
                val producedBlock = produceBlock(frozenBlock)
                val fresh = findFresh()
                if (frozenBlock == fresh) {
                    val isSet = currentBlock.compareAndSet(frozenBlock, producedBlock)
                    if (isSet) {
                        Logger.getGlobal().info("Produced block: $producedBlock")
                        sendToNodes(producedBlock)
                        dbHelper.fillDB(producedBlock)
                    } else {
                        setFresh(fresh)
                    }
                } else {
                    setFresh(fresh)
                }
            }
        }
    }

    fun setFresh (freshBlock: Block) {
        Logger.getGlobal().info("Updated block: $freshBlock")
        currentBlock.set(freshBlock)
        sendToNodes(freshBlock)
        dbHelper.fillDB(freshBlock)
    }

    suspend fun sendToOneNode(node: String, producedBlock: Block) {
        try {
            client.post("http://$node/update") {
                contentType(ContentType.Application.Json)
                setBody(producedBlock)
            }
        } catch (_: ConnectException) {
        }
    }

    @Synchronized
    fun sendToNodes(producedBlock: Block) = runBlocking {
        for (node in nodesToSend) {
            sendToOneNode(node, producedBlock)
        }
    }

    @Synchronized
    fun produceBlock(block: Block): Block {
        val index = BigInteger(block.index).add(BigInteger.ONE)
        val prev_hash = BigInteger(block.hash)
        val data = produceRandomString()
        var nonce = BigInteger.ZERO
        var hash = Block.getHash(index, prev_hash, data, nonce)
        var isValid = hash.isValid()
        while (!isValid) {
            nonce = nonce.add(BigInteger.ONE)
            hash = Block.getHash(index, prev_hash, data, nonce)
            isValid = hash.isValid()
        }
        return Block(index.toString(), prev_hash.toString(), hash.toString(), data, nonce.toString())
    }

    @Synchronized
    fun initFirstBlock(): Block {
        val index = BigInteger.ZERO
        val prev_hash = BigInteger.ZERO
        val data = produceRandomString()
        var nonce = BigInteger.ZERO
        var hash = Block.getHash(index, prev_hash, data, nonce)
        var isValid = hash.isValid()
        while (!isValid) {
            nonce = nonce.add(BigInteger.ONE)
            hash = Block.getHash(index, prev_hash, data, nonce)
            isValid = hash.isValid()
        }
        return Block(index.toString(), prev_hash.toString(), hash.toString(), data, nonce.toString())
    }

    suspend fun findFresh(): Block {
        val frozenBlock = AtomicReference(currentBlock.get())
        for (node in nodesToSend) {
            val block = getBlock(node)
            if (block != null &&
                block.index > frozenBlock.get().index
            ) {
                frozenBlock.set(block)
            }
        }
        return frozenBlock.get()
    }

    private suspend fun getBlock(node: String): Block? {
        return try {
            val response = client.get("http://$node/")
            response.body<Block>()
        } catch (e: Exception) {
            null
        }
    }

    fun produceRandomString(): String {
        var random = Random(seed)
        val symbolsAmount = 256
        val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val result = (0 until symbolsAmount).map {alphabet[(alphabet.size * random.nextFloat()).toInt()] }
            .joinToString(separator = "")
        return result
    }
}