package com.thangnq.pingly.feature.ping.domain.usecase

import com.thangnq.pingly.feature.ping.domain.model.Ping
import com.thangnq.pingly.feature.ping.domain.repository.PingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObservePingFeed(private val repository: PingRepository) {
    operator fun invoke(): Flow<List<Ping>> = repository.observePings().map { pings ->
        pings.sortedByDescending(Ping::id)
    }
}
