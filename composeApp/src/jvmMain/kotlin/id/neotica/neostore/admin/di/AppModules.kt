package id.neotica.neostore.admin.di

import id.neotica.neostore.admin.data.FileRepository
import id.neotica.neostore.admin.data.FileRepositoryImpl
import id.neotica.neostore.admin.ui.feature.UploadViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModules = module {
    viewModelOf(::UploadViewModel)

    singleOf(::FileRepositoryImpl).bind(FileRepository::class)
}

fun initializeKoin() = startKoin { modules(appModules) }