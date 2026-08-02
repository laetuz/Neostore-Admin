package id.neotica.neostore.admin.platform

import java.io.File

actual fun exportIconToDownloads(iconBytes: ByteArray, fileName: String) {
    val downloadsDir = File(System.getProperty("user.home"), "Downloads")
    downloadsDir.mkdirs()
    val iconFile = File(downloadsDir, fileName)
    iconFile.writeBytes(iconBytes)
}