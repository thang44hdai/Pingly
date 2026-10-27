package com.thangnq.pingly.feature.ping

import com.thangnq.pingly.feature.ping.data.InMemoryPingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class InMemoryPingRepositoryTest {
    @Test
    fun addsPingsWithUniqueIdsAndPublishesSnapshots() = runTest {
        val repository = InMemoryPingRepository()

        val first = repository.addPing("One")
        val second = repository.addPing("Two")

        assertEquals(1L, first.id)
        assertEquals(2L, second.id)
        assertEquals(listOf(second, first), repository.observePings().first())
    }
}
