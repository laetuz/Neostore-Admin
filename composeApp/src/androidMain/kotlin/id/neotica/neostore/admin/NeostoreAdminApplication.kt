package id.neotica.neostore.admin

import android.app.Application
import id.neotica.neostore.admin.di.appModules
import id.neotica.neostore.admin.platform.AndroidAppContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NeostoreAdminApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidAppContext.appContext = applicationContext
        startKoin {
            androidContext(applicationContext)
            modules(*appModules)
        }
    }
}