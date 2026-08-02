package id.neotica.neostore.admin.di

import id.neotica.neostore.admin.data.local.DesktopTokenStorage
import id.neotica.neostore.admin.domain.local.TokenStorage
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    singleOf(::DesktopTokenStorage).bind(TokenStorage::class)
}