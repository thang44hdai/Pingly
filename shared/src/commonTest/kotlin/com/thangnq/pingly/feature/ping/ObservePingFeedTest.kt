package com.thangnq.pingly.feature.ping

import com.thangnq.pingly.feature.ping.domain.model.Ping
import com.thangnq.pingly.feature.ping.domain.usecase.ObservePingFeed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObservePingFeedTest {
    @Test
    fun newestPingAppearsFirst() = runTest {
        val repository = FakePingRepository(
            listOf(Ping(1, "First"), Ping(3, "Third"), Ping(2, "Second"))
        )

        val ids = ObservePingFeed(repository)().first().map(Ping::id)

        assertEquals(listOf(3L, 2L, 1L), ids)
    }
}
