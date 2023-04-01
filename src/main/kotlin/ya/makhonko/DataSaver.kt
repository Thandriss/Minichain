package ya.makhonko

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ya.makhonko.Server.name
import java.io.File

class DataSaver {
    private val file = if (name.isEmpty()) File("${System.currentTimeMillis()}.db") else File(name)

    init {
        if (!file.exists()) file.createNewFile()
    }

    @Synchronized
    fun fillDB(block: Block) {
        with(file) {
            appendText(Json.encodeToString(block))
            appendText("\n")
        }
    }
}