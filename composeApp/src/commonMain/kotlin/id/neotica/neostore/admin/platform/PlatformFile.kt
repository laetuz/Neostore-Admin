package id.neotica.neostore.admin.platform

import io.ktor.utils.io.ByteReadChannel

expect class PlatformFile {
    val name: String
    val path: String
    val extension: String
    val size: Long
    fun readBytes(): ByteArray
    fun openReadChannel(): ByteReadChannel
}

expect fun platformFileFromBytes(name: String, bytes: ByteArray): PlatformFile