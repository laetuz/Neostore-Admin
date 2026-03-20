package id.neotica.neostore.admin.data.local

import id.neotica.neostore.admin.domain.local.TokenStorage
import java.util.prefs.Preferences

class DesktopTokenStorage: TokenStorage {
    private val prefs = Preferences.userRoot().node("id.neotica.neostore.admin")

    override fun saveToken(token: String, refreshToken: String) {
        prefs.put(JWT_TOKEN, token)
        prefs.put(REFRESH_TOKEN, refreshToken)
    }

    override fun getToken(): String? = prefs.get(JWT_TOKEN, null)

    override fun clearToken() {
        prefs.remove(JWT_TOKEN)
        prefs.remove(REFRESH_TOKEN)
    }

    companion object {
        private const val JWT_TOKEN = "JWT_TOKEN"
        private const val REFRESH_TOKEN = "REFRESH_TOKEN"
    }
}