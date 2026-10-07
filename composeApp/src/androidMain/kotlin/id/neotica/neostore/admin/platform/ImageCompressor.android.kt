package id.neotica.neostore.admin.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import java.io.ByteArrayOutputStream

actual fun compressJpeg(bytes: ByteArray, quality: Int): ByteArray? = try {
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

    val flattened = Bitmap.createBitmap(decoded.width, decoded.height, Bitmap.Config.ARGB_8888)
    Canvas(flattened).apply {
        drawColor(Color.WHITE)
        drawBitmap(decoded, 0f, 0f, null)
    }

    val output = ByteArrayOutputStream()
    flattened.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(1, 100), output)
    output.toByteArray()
} catch (e: Exception) {
    null
}
