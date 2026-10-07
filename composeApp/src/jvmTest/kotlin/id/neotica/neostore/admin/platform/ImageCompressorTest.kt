package id.neotica.neostore.admin.platform

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Surface
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ImageCompressorTest {

    private fun sample(format: EncodedImageFormat): ByteArray {
        val surface = Surface.makeRasterN32Premul(8, 8)
        surface.canvas.clear(0xFF3366FF.toInt())
        return surface.makeImageSnapshot().encodeToData(format)!!.bytes
    }

    private fun isJpeg(bytes: ByteArray): Boolean =
        bytes.size > 3 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xD8.toByte() &&
            bytes[2] == 0xFF.toByte()

    @Test
    fun convertsPngToJpeg() {
        val out = compressJpeg(sample(EncodedImageFormat.PNG), 80)
        assertNotNull(out)
        assertTrue(isJpeg(out))
    }

    @Test
    fun convertsWebpToJpeg() {
        val out = compressJpeg(sample(EncodedImageFormat.WEBP), 80)
        assertNotNull(out)
        assertTrue(isJpeg(out))
    }

    @Test
    fun returnsNullForGarbage() {
        assertNull(compressJpeg(byteArrayOf(1, 2, 3, 4, 5), 80))
    }
}
