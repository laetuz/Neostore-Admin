package id.neotica.neostore.admin.data.local

import android.content.Context
import id.neotica.neostore.admin.domain.local.TokenStorage
import id.neotica.neostore.admin.platform.AndroidAppContext

class AndroidTokenStorage : TokenStorage {
    private val prefs = AndroidAppContext.appContext.getSharedPreferences(
        "neostore_admin",
        Context.MODE_PRIVATE
    )

    override fun saveToken(token: String, refreshToken: String) {
        prefs.edit()
            .putString(JWT_TOKEN, token)
            .putString(REFRESH_TOKEN, refreshToken)
            .apply()
    }

    override fun getToken(): String? = prefs.getString(JWT_TOKEN, null)

    override fun clearToken() {
        prefs.edit()
            .remove(JWT_TOKEN)
            .remove(REFRESH_TOKEN)
            .apply()
    }

    companion object {
        private const val JWT_TOKEN = "JWT_TOKEN"
        private const val REFRESH_TOKEN = "REFRESH_TOKEN"
    }
}