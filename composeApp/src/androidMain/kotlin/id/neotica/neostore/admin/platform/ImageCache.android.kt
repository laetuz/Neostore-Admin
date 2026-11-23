package id.neotica.neostore.admin.platform

import coil3.SingletonImageLoader

actual fun clearImageCache() {
    val loader = SingletonImageLoader.get(AndroidAppContext.appContext)
    loader.memoryCache?.clear()
    loader.diskCache?.clear()
}
