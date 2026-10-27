package com.thangnq.pingly.feature.ping.domain.repository

import com.thangnq.pingly.feature.ping.domain.model.Ping
import kotlinx.coroutines.flow.Flow

interface PingRepository {
    fun observePings(): Flow<List<Ping>>

    suspend fun addPing(message: String): Ping
}
