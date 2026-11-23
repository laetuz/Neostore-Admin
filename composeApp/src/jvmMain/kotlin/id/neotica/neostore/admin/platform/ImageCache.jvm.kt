package id.neotica.neostore.admin.platform

import coil3.PlatformContext
import coil3.SingletonImageLoader

actual fun clearImageCache() {
    val loader = SingletonImageLoader.get(PlatformContext.INSTANCE)
    loader.memoryCache?.clear()
    loader.diskCache?.clear()
}
