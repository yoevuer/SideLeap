package hunoia.sideleap.settings.store

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.core.Serializer
import hunoia.sideleap.core.serialization.JsonHelper
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/24
 */

inline fun <reified T> Context.dataStore(fileName: String, defValue: T): DataStore<T> {
    val serializer = object : Serializer<T> {
        override val defaultValue: T = defValue

        override suspend fun readFrom(input: InputStream): T {
            val bytes = try { input.readBytes() } catch (_: Exception) { ByteArray(0) }
            return try {
                val string = bytes.decodeToString()
                if (string.isBlank()) return defaultValue
                JsonHelper.decodeFromString<T>(string)
            } catch (e: Exception) {
                Log.e("DataStore", "read $fileName failed: ${e::class.simpleName} ${e.message} size=${bytes.size}")
                defaultValue
            }
        }

        override suspend fun writeTo(t: T, output: OutputStream) {
            try {
                val string = JsonHelper.encodeToString(t)
                output.write(string.encodeToByteArray())
            } catch (e: Exception) {
                Log.e("DataStore", "write $fileName failed: ${e::class.simpleName} ${e.message}")
            }
        }
    }
    return MultiProcessDataStoreFactory.create(serializer) {
        File("${filesDir.absolutePath}/ds/$fileName")
    }
}
