package id.neotica.neostore.admin.platform

import android.content.ClipData
import android.content.Context

actual fun copyToClipboard(text: String) {
    val clipboard = AndroidAppContext.appContext.getSystemService(Context.CLIPBOARD_SERVICE)
        as android.content.ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("text", text))
}