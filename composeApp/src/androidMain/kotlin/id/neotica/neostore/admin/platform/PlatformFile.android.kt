package id.neotica.neostore.admin.platform

import android.net.Uri
import android.provider.OpenableColumns
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel

actual class PlatformFile internal constructor(
    internal val uri: Uri?,
    internal val bytes: ByteArray?,
    actual val name: String,
    actual val path: String,
    actual val extension: String,
    actual val size: Long,
) {
    actual fun readBytes(): ByteArray {
        bytes?.let { return it }
        val resolver = AndroidAppContext.appContext.contentResolver
        val stream = resolver.openInputStream(uri!!) ?: error("Cannot open ${uri}")
        return stream.use { it.readBytes() }
    }

    actual fun openReadChannel(): ByteReadChannel {
        bytes?.let { return ByteReadChannel(it) }
        val resolver = AndroidAppContext.appContext.contentResolver
        val stream = resolver.openInputStream(uri!!) ?: error("Cannot open ${uri}")
        return stream.toByteReadChannel()
    }
}

internal fun platformFileFromUri(uri: Uri): PlatformFile {
    val resolver = AndroidAppContext.appContext.contentResolver
    val name = resolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    } ?: uri.lastPathSegment ?: "file"

    val size = resolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (index >= 0 && cursor.moveToFirst() && !cursor.isNull(index)) cursor.getLong(index) else -1L
    } ?: -1L

    return PlatformFile(
        uri = uri,
        bytes = null,
        name = name,
        path = uri.toString(),
        extension = name.substringAfterLast('.', ""),
        size = size,
    )
}

actual fun platformFileFromBytes(name: String, bytes: ByteArray): PlatformFile = PlatformFile(
    uri = null,
    bytes = bytes,
    name = name,
    path = name,
    extension = name.substringAfterLast('.', ""),
    size = bytes.size.toLong(),
)