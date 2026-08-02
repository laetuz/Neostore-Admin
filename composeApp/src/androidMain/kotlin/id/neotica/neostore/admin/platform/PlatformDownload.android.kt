package id.neotica.neostore.admin.platform

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

actual fun exportIconToDownloads(iconBytes: ByteArray, fileName: String) {
    val context = AndroidAppContext.appContext

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val uri = context.contentResolver.insert(collection, values)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(iconBytes) }
        }
    } else {
        @Suppress("DEPRECATION")
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (dir != null) {
            dir.mkdirs()
            java.io.File(dir, fileName).writeBytes(iconBytes)
        }
    }
}
