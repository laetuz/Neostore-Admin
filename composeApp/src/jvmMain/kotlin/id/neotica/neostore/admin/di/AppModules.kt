package id.neotica.neostore.admin.di

import id.neotica.neostore.admin.data.local.DesktopTokenStorage
import id.neotica.neostore.admin.data.remote.AuthRepositoryImpl
import id.neotica.neostore.admin.domain.remote.FileRepository
import id.neotica.neostore.admin.data.remote.FileRepositoryImpl
import id.neotica.neostore.admin.domain.local.TokenStorage
import id.neotica.neostore.admin.domain.remote.AuthRepository
import id.neotica.neostore.admin.ui.feature.upload.UploadViewModel
import id.neotica.neostore.admin.ui.feature.auth.LoginViewModel
import id.neotica.neostore.admin.ui.feature.detailapp.DetailAppViewModel
import id.neotica.neostore.admin.ui.feature.feed.FeedViewModel
import id.neotica.neostore.admin.ui.feature.registerapp.RegisterAppViewModel
import id.neotica.neostore.admin.ui.feature.updateapp.UpdateAppViewModel
import id.neotica.neostore.admin.utils.Constants.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModules = module {
    singleOf(::DesktopTokenStorage).bind(TokenStorage::class)

    singleOf(::FileRepositoryImpl).bind(FileRepository::class)
    single<AuthRepository> {
        AuthRepositoryImpl(
            get(),
            get(),
            BASE_URL
        )
    }

    viewModelOf(::UploadViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterAppViewModel)
    viewModelOf(::UpdateAppViewModel)
    viewModelOf(::FeedViewModel)
    viewModelOf(::DetailAppViewModel)
}

val networkModule = module {
    single<HttpClient> {
        val storage = get<TokenStorage>()

        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true; prettyPrint = true })
            }
            install(Logging) {
                logger = Logger.DEFAULT; level = LogLevel.HEADERS
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        storage.getToken()?.let { BearerTokens(it, "") }
                    }
                }
            }
        }
    }
}

val appModules = arrayOf(networkModule, dataModules)

fun initializeKoin() = startKoin { modules(*appModules) }