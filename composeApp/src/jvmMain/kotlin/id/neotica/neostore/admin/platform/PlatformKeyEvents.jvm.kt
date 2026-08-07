package id.neotica.neostore.admin.platform

import kotlin.concurrent.thread
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.Robot
import java.awt.event.KeyEvent

actual class PlatformKeyEvent internal constructor(
    actual val char: Char?,
    actual val isEscape: Boolean,
    actual val isD: Boolean,
    actual val isBackspace: Boolean,
    actual val isEnter: Boolean,
    actual val isCtrlDown: Boolean,
    actual val isMetaDown: Boolean,
    actual val isShiftDown: Boolean,
) {
    internal companion object {
        fun fromAwt(event: KeyEvent): PlatformKeyEvent = PlatformKeyEvent(
            char = if (event.keyChar != KeyEvent.CHAR_UNDEFINED) event.keyChar else null,
            isEscape = event.keyCode == KeyEvent.VK_ESCAPE,
            isD = event.keyCode == KeyEvent.VK_D,
            isBackspace = event.keyCode == KeyEvent.VK_BACK_SPACE,
            isEnter = event.keyCode == KeyEvent.VK_ENTER,
            isCtrlDown = event.isControlDown,
            isMetaDown = event.isMetaDown,
            isShiftDown = event.isShiftDown,
        )
    }
}

actual fun installPlatformKeyDispatcher(handler: (PlatformKeyEvent) -> Boolean): () -> Unit {
    val dispatcher = KeyEventDispatcher { event ->
        if (event.id == KeyEvent.KEY_PRESSED) handler(PlatformKeyEvent.fromAwt(event)) else false
    }
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher)
    return {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher)
    }
}

actual fun performPlatformPageDownScroll(count: Int) {
    thread {
        val robot = Robot()
        Thread.sleep(50)
        robot.keyPress(KeyEvent.VK_META)
        robot.keyPress(KeyEvent.VK_TAB)
        robot.keyRelease(KeyEvent.VK_TAB)
        robot.keyRelease(KeyEvent.VK_META)
        Thread.sleep(100)
        repeat(count) {
            robot.keyPress(KeyEvent.VK_PAGE_DOWN)
            robot.keyRelease(KeyEvent.VK_PAGE_DOWN)
            Thread.sleep(50)
        }
    }
}