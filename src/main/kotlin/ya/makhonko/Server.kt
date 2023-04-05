package ya.makhonko

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import java.util.concurrent.atomic.AtomicBoolean

object Server {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }
    var port = 0
    var flagFirst = AtomicBoolean(false)
    lateinit var toSend: Set<String>
}