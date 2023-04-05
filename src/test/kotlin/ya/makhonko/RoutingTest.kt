package ya.makhonko

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ya.makhonko.plugins.configureRouting
import java.util.logging.Logger

class RoutingTest {

    @Test
    fun checkServerBehaviour() {
        runBlocking {
            Server.port = 8080
            Server.flagFirst.set(true)
            Server.toSend = setOf("localhost:8081")
            val blockProducing = BlockProduction()
            blockProducing.seed = 55555555L
            testApplication {
                val client = createClient {
                    install(ContentNegotiation) {
                        json()
                    }
                }
                application {
                    configureRouting(blockProducing)
                }
                val response = client.get("/")
                Assertions.assertEquals(response.status, HttpStatusCode.OK)
                val actual = response.body<Block>()
                val expected = Block(
                    "0",
                    "0",
                    "16213002647113641845979867022170167171341248411354047814409828141690162000000",
                    "1w9XiQFaUK1KxIYIUkuS6gqWHG2hsJ86UEYw4mmP7m7KWGTCh4ve4HOlSMnWkIPiLkOHntuHyJh3xrMgTTICJOhIa7QcukKbh7uevKxKyOGtZipWPmIxQeWknN5ssrymudksaltFJrlqJZdb3HSUkNnzFEWABPkV5k8KhoZdkMrkQ8Wk50v5hOPFeI9X65Eoku3dIfnuZMWxAEzbXIWocrmwGQG7LN21seG8BewCmcfXbOYTxDpF0QggB5bNmvoH",
                    "347267"
                )
                Assertions.assertEquals(expected, actual)
                val response2 = client.get("/abc")
                assertEquals(response2.status, HttpStatusCode.NotFound)
                val testBlock = Block(
                "2",
                "-16346848297816610177277472819678465847532546009992945195534181085709118000000",
                "11456792849688944863687221543830969588625239580559150127325152649766174000000",
                "gSR62l1Z1yp203HXdqCg7sii4MFIHHkYRp3GntVLNkFo6xllu6Uyk44cqfSm1Nl9MunQnzRjl51RkQk5uxWMjtqXRaBCqMZndD4p9mz9DSCZUX0l7GtBb26iHAYsdfTb8mE5hjQ3XayUML8SWQ3i1O0iuYzTXnNUXDWg8BexQHbTM6NCZzQHeHK9pEsg8lm3d9gbWzb5tkwogtATRwRSSj4Srdfhkn8Cz4ol17inmnOpCKf1m8chu6ygVZXKCjHV",
                "1337748"
                )
                val response3 = client.post("/update") {
                    contentType(ContentType.Application.Json)
                    setBody(testBlock)
                }
                assertEquals(response3.status, HttpStatusCode.Accepted)
            }
        }
    }
    @Test
    fun checkAverageResponse() {
        val responseAmount = 1000
        runBlocking {
            Server.port = 8080
            Server.flagFirst.set(true)
            Server.toSend = setOf("localhost:8081")
            val blockProducing = BlockProduction()
            blockProducing.seed = 55555555L
            testApplication {
                val client = createClient {
                    install(ContentNegotiation) {
                        json()
                    }
                }
                application {
                    configureRouting(blockProducing)
                }
                val start = System.currentTimeMillis()
                runBlocking {
                    repeat(responseAmount) {
                        launch(Dispatchers.IO) {
                            client.get("/")
                        }
                    }
                }
                val average = (System.currentTimeMillis() - start).toFloat() / responseAmount
                Logger.getGlobal().info("Average time: $average")
            }
        }
    }
}