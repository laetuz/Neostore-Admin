package id.neotica.neostore.admin.platform

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image as SkiaImage

actual fun transcodeToPng(bytes: ByteArray): ByteArray? = try {
    val decoded = SkiaImage.makeFromEncoded(bytes)
    if (decoded.width <= 0 || decoded.height <= 0) {
        null
    } else {
        decoded.encodeToData(EncodedImageFormat.PNG)?.bytes
    }
} catch (e: Exception) {
    null
}
