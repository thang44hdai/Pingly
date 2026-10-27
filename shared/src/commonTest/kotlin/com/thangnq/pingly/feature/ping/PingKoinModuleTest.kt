package com.thangnq.pingly.feature.ping

import com.thangnq.pingly.di.appModule
import com.thangnq.pingly.feature.ping.domain.repository.PingRepository
import com.thangnq.pingly.feature.ping.domain.usecase.CreatePing
import com.thangnq.pingly.feature.ping.domain.usecase.CreatePingResult
import com.thangnq.pingly.feature.ping.domain.usecase.ObservePingFeed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class PingKoinModuleTest {
    @Test
    fun resolvesFeatureBindingsAgainstOneRepository() = runTest {
        val application = koinApplication { modules(appModule) }
        try {
            val repository = application.koin.get<PingRepository>()
            val createPing = application.koin.get<CreatePing>()
            val observePingFeed = application.koin.get<ObservePingFeed>()

            assertSame(repository, application.koin.get<PingRepository>())
            assertIs<CreatePingResult.Created>(createPing("From Koin"))
            assertEquals("From Koin", observePingFeed().first().single().message)
        } finally {
            application.close()
        }
    }
}
