package ya.makhonko

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import junit.framework.Assert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.UnrecognizedOptionException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ya.makhonko.Block.Companion.isValid
import ya.makhonko.Server.flagFirst
import ya.makhonko.Server.port
import ya.makhonko.Server.toSend
import ya.makhonko.plugins.configureRouting
import java.math.BigInteger
import java.util.logging.Logger
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApplicationTest {

    @Test
    fun checkingProduction() {
        toSend = setOf("127.0.0.1:8081")
        val blockProducing = BlockProduction()
        blockProducing.seed = 97L
        val firstBlock = blockProducing.initFirstBlock()
        val secondBlock = blockProducing.produceBlock(firstBlock)
        val thirdBlock = blockProducing.produceBlock(secondBlock)
        val forthBlock = blockProducing.produceBlock(thirdBlock)
        assertEquals(
            Block(
                "1",
                "32788811754881477914290611803402379045757330394158667527749082024303778000000",
                "31529927478172242704240467232484210722953249089332626406500298526114279000000",
                "S5Qt70AQF4LIFMMyDjHviLuiBf3SVAjH8ZlBpfbGsLuk0EwHRjpgkNERhm3TG9JRyi2QZ79qLBwQUYL5NPFqFpHAJzGpRyZRAtNEdJVqLDMMdf6zQQ1RCYuGo8vjIziB6cxsxBRnXWFX42ts0IPgycd09fBRQftSkmXzkmzZMP0IXiyEty30Szxu4SluB3KENXPeB6m9W8toasAfmXVwWRS1MORh78p8zQKmG2ZPDqDR0Umb9I0IiQmu9Vqwi1E0",
                "1759914"
            ),
            secondBlock
        )
        assertEquals(
            Block(
                "2",
                "31529927478172242704240467232484210722953249089332626406500298526114279000000",
                "-22515942540228951825207144799416508628944501572866090800462206511598951000000",
                "S5Qt70AQF4LIFMMyDjHviLuiBf3SVAjH8ZlBpfbGsLuk0EwHRjpgkNERhm3TG9JRyi2QZ79qLBwQUYL5NPFqFpHAJzGpRyZRAtNEdJVqLDMMdf6zQQ1RCYuGo8vjIziB6cxsxBRnXWFX42ts0IPgycd09fBRQftSkmXzkmzZMP0IXiyEty30Szxu4SluB3KENXPeB6m9W8toasAfmXVwWRS1MORh78p8zQKmG2ZPDqDR0Umb9I0IiQmu9Vqwi1E0",
                "612595"
            ),
            thirdBlock
        )
        assertEquals(
            Block(
                "3",
                "-22515942540228951825207144799416508628944501572866090800462206511598951000000",
                "11995918560551598250012321878056720500460091339100931001354209308995999000000",
                "S5Qt70AQF4LIFMMyDjHviLuiBf3SVAjH8ZlBpfbGsLuk0EwHRjpgkNERhm3TG9JRyi2QZ79qLBwQUYL5NPFqFpHAJzGpRyZRAtNEdJVqLDMMdf6zQQ1RCYuGo8vjIziB6cxsxBRnXWFX42ts0IPgycd09fBRQftSkmXzkmzZMP0IXiyEty30Szxu4SluB3KENXPeB6m9W8toasAfmXVwWRS1MORh78p8zQKmG2ZPDqDR0Umb9I0IiQmu9Vqwi1E0",
                "484008"
            ),
            forthBlock
        )
    }

    @Test
    fun checkArgs() {
        val thrown1 = assertThrows<IllegalArgumentException> {
            cmdParser(
                arrayOf(
                    "-p",
                    "8080",
                    "-n",
                    "127.0.0.18081,127.0.0.1:8082",
                    "-f"
                )
            )
        }
        val thrown2 = assertThrows<IllegalArgumentException> {
            cmdParser(
                arrayOf(
                    "-p",
                    "8080",
                    "-n",
                    "127.0.0.1:,.0.0.1:8082",
                    "-f"
                )
            )
        }
        val thrown3 = assertThrows<IllegalArgumentException> {
            cmdParser(
                arrayOf(
                    "-n",
                    "127.0.0.1:8081,127.0.0.1:8082",
                    "-f"
                )
            )
        }
        val thrown4 = assertThrows<IllegalArgumentException> {
            cmdParser(
                arrayOf(
                    "-p",
                    "8080",
                    "-f"
                )
            )
        }
        val thrown5 = assertThrows<IllegalArgumentException> {
            cmdParser(
                arrayOf(
                    "-p",
                    "80t2",
                    "-n",
                    "127.0.0.1:8081,127.0.0.1:8082",
                    "-f"
                )
            )
        }
        val thrown6 = assertThrows<IllegalArgumentException> {
            cmdParser(
                arrayOf()
            )
        }
        val unrecognizedOption = assertThrows<UnrecognizedOptionException> {
            cmdParser(
                arrayOf(
                    "-p",
                    "8080",
                    "-n",
                    "127.0.0.1:8081,127.0.0.1:8082",
                    "-f",
                    "-r"
                )
            )
        }
        assertEquals("Wrong address!", thrown1.message)
        assertEquals("Wrong address!", thrown2.message)
        assertEquals("Clarify port!", thrown3.message)
        assertEquals("Clarify nodes!", thrown4.message)
        assertEquals("Port is not a number!", thrown5.message)
        assertEquals("Clarify port!", thrown6.message)
        assertEquals("Unrecognized option: -r", unrecognizedOption.message)
       cmdParser(
            arrayOf(
                "-p",
                "8080",
                "-n",
                "127.0.0.1:8081,127.0.0.1:8082",
                "-f"
            )
        )
        assertEquals(flagFirst.get(), true)
        assertEquals(toSend, setOf("127.0.0.1:8081", "127.0.0.1:8082"))
    }

    @Test
    fun firstBlockProductionChecking() {
        toSend = setOf("127.0.0.1:8081")
        val blockProducing = BlockProduction()
        blockProducing.seed = 55555555L
        assertEquals(
            Block(
                "0",
                "0",
                "16213002647113641845979867022170167171341248411354047814409828141690162000000",
                "1w9XiQFaUK1KxIYIUkuS6gqWHG2hsJ86UEYw4mmP7m7KWGTCh4ve4HOlSMnWkIPiLkOHntuHyJh3xrMgTTICJOhIa7QcukKbh7uevKxKyOGtZipWPmIxQeWknN5ssrymudksaltFJrlqJZdb3HSUkNnzFEWABPkV5k8KhoZdkMrkQ8Wk50v5hOPFeI9X65Eoku3dIfnuZMWxAEzbXIWocrmwGQG7LN21seG8BewCmcfXbOYTxDpF0QggB5bNmvoH",
                "347267"
            ),
            blockProducing.initFirstBlock()
        )
        blockProducing.seed = 5L
        assertEquals(
            Block(
                "0",
                "0",
                "33909560280946788873225638515818700373881466206494887834881533360528909000000",
                "TkfLE6CZBARUrvV0nC5T2tnu2bBtyO9P2mrPpdKEFEeIcuQZBDvTsDhhvKQMmSctljo9cvQ7K3ZVMXOsqVRD9UDgfU1YIsE5ZblJbMlY2xjW6DJfaPJBazWas5Nh2uKBrFaZuhVR8texfsZd9EVAlCbSdsgJYNkpYsV7ZXwUhVqAj55THiAGBsFFKC8i1S4cIi8XuurL5REIm47TS9lkDi7mWzIZoFQ9NJx8OkgYKaV3QezMBKcBTXLUXIdzxHvC",
                "860303"
            ),
            blockProducing.initFirstBlock()
        )
        blockProducing.seed = -10000000L
        assertEquals(
            Block(
                "0",
                "0",
                "-18319287028516150190870479436625336166029544092900311598528320796805160000000",
                "DKLcXHIkaMqWM1LFVqTry6sZIVlups0K2UKIgEGFJMocAIY1l2yIbCrhUJ6bh52ctajufBJksjUbaj0iF77KEpTazQHXqU2UEVk0ZtpMGyxdomkQQ2ibtVh2kiTNjZvvkDEKmr8JAG3gN2Up9GteGmTG40J5MhscXCN0FFeZrmA3YjknJCaxqZYWzQBE2qRykbprbD7oNNty9nwJcpz9yjgzkncoHDChgWUY7Xpn4XIs39jgPLrS1ZG358j7OoQu",
                "2802395"
            ),
            blockProducing.initFirstBlock()
        )
    }
    @Test
    fun blockMethodsTests () {
        toSend = setOf("127.0.0.1:8081")
        val blockProducing = BlockProduction()
        blockProducing.seed = 55555555L
        val firstBlock = blockProducing.initFirstBlock()
        val secondBlock = blockProducing.produceBlock(firstBlock)
        var hash = Block.getHash(secondBlock)
        assertTrue(hash.isValid())
        hash = hash.add(BigInteger.ONE)
        assertFalse(hash.isValid())
    }
}