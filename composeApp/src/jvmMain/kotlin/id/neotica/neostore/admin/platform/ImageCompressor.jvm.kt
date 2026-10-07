package id.neotica.neostore.admin.platform

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Surface
import org.jetbrains.skia.Image as SkiaImage

actual fun compressJpeg(bytes: ByteArray, quality: Int): ByteArray? = try {
    val decoded = SkiaImage.makeFromEncoded(bytes)
    if (decoded.width <= 0 || decoded.height <= 0) {
        null
    } else {
        val surface = Surface.makeRasterN32Premul(decoded.width, decoded.height)
        surface.canvas.apply {
            clear(0xFFFFFFFF.toInt())
            drawImage(decoded, 0f, 0f)
        }
        val snapshot = surface.makeImageSnapshot()
        snapshot.encodeToData(EncodedImageFormat.JPEG, quality.coerceIn(1, 100))?.bytes
    }
} catch (e: Exception) {
    null
}
