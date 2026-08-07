package id.neotica.neostore.admin.platform

actual class PlatformKeyEvent internal constructor(
    actual val char: Char?,
    actual val isEscape: Boolean,
    actual val isD: Boolean,
    actual val isBackspace: Boolean,
    actual val isEnter: Boolean,
    actual val isCtrlDown: Boolean,
    actual val isMetaDown: Boolean,
    actual val isShiftDown: Boolean,
)

actual fun installPlatformKeyDispatcher(handler: (PlatformKeyEvent) -> Boolean): () -> Unit = { }

actual fun performPlatformPageDownScroll(count: Int) { }