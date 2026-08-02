package id.neotica.neostore.admin.platform

expect class PlatformKeyEvent {
    val char: Char?
    val isEscape: Boolean
    val isD: Boolean
    val isBackspace: Boolean
    val isEnter: Boolean
    val isCtrlDown: Boolean
    val isMetaDown: Boolean
}

expect fun installPlatformKeyDispatcher(handler: (PlatformKeyEvent) -> Boolean): () -> Unit

expect fun performPlatformPageDownScroll(count: Int)