package id.neotica.neostore.admin.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

actual fun transcodeToPng(bytes: ByteArray): ByteArray? = try {
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
    val output = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    output.toByteArray()
} catch (e: Exception) {
    null
}
