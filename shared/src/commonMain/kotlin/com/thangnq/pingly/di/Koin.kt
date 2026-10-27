package com.thangnq.pingly.di

import com.thangnq.pingly.feature.ping.data.InMemoryPingRepository
import com.thangnq.pingly.feature.ping.domain.repository.PingRepository
import com.thangnq.pingly.feature.ping.domain.usecase.CreatePing
import com.thangnq.pingly.feature.ping.domain.usecase.ObservePingFeed
import com.thangnq.pingly.feature.home.presentation.HomeViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.dsl.module

private val pingModule = module {
    single<PingRepository> { InMemoryPingRepository() }
    factory { CreatePing(get()) }
    factory { ObservePingFeed(get()) }
}

private val homeModule = module {
    viewModel { HomeViewModel(get(), get()) }
}

val appModule = module {
    includes(pingModule, homeModule)
}

fun initKoin(configuration: KoinAppDeclaration? = null) {
    startKoin {
        includes(configuration)
        modules(appModule)
    }
}
