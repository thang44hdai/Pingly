package com.thangnq.pingly.feature.ping.data

import com.thangnq.pingly.feature.ping.domain.model.Ping
import com.thangnq.pingly.feature.ping.domain.repository.PingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryPingRepository : PingRepository {
    private val mutex = Mutex()
    private val pings = MutableStateFlow<List<Ping>>(emptyList())
    private var nextId = 0L

    override fun observePings(): Flow<List<Ping>> = pings.asStateFlow()

    override suspend fun addPing(message: String): Ping = mutex.withLock {
        val ping = Ping(id = ++nextId, message = message)
        pings.value = listOf(ping) + pings.value
        ping
    }
}
