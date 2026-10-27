package com.thangnq.pingly.feature.ping

import com.thangnq.pingly.feature.ping.domain.model.Ping
import com.thangnq.pingly.feature.ping.domain.repository.PingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakePingRepository(initial: List<Ping> = emptyList()) : PingRepository {
    private val pings = MutableStateFlow(initial)
    val addedMessages = mutableListOf<String>()

    override fun observePings(): Flow<List<Ping>> = pings

    override suspend fun addPing(message: String): Ping {
        addedMessages += message
        return Ping(addedMessages.size.toLong(), message).also { ping ->
            pings.value = pings.value + ping
        }
    }
}
