package id.neotica.neostore.admin.platform

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel

actual class PlatformFile internal constructor(
    internal val file: java.io.File,
) {
    actual val name: String get() = file.name
    actual val path: String get() = file.absolutePath
    actual val extension: String get() = file.extension
    actual val size: Long get() = file.length()
    actual fun readBytes(): ByteArray = file.readBytes()
    actual fun openReadChannel(): ByteReadChannel = file.inputStream().toByteReadChannel()
}

internal fun platformFileFromJavaFile(file: java.io.File): PlatformFile = PlatformFile(file)

actual fun platformFileFromBytes(name: String, bytes: ByteArray): PlatformFile {
    val temp = java.io.File.createTempFile("app_file", ".tmp")
    temp.deleteOnExit()
    temp.writeBytes(bytes)
    return PlatformFile(temp)
}