package com.thangnq.pingly.feature.ping.domain.usecase

import com.thangnq.pingly.feature.ping.domain.model.Ping
import com.thangnq.pingly.feature.ping.domain.repository.PingRepository

sealed interface CreatePingResult {
    data class Created(val ping: Ping) : CreatePingResult
    data class Invalid(val reason: InvalidReason) : CreatePingResult

    sealed interface InvalidReason {
        data object Empty : InvalidReason
        data class TooLong(val maxLength: Int) : InvalidReason
    }
}

class CreatePing(private val repository: PingRepository) {
    suspend operator fun invoke(rawMessage: String): CreatePingResult {
        val message = rawMessage.trim()
        if (message.isEmpty()) {
            return CreatePingResult.Invalid(CreatePingResult.InvalidReason.Empty)
        }
        if (message.length > MAX_MESSAGE_LENGTH) {
            return CreatePingResult.Invalid(
                CreatePingResult.InvalidReason.TooLong(MAX_MESSAGE_LENGTH)
            )
        }
        return CreatePingResult.Created(repository.addPing(message))
    }

    companion object {
        const val MAX_MESSAGE_LENGTH = 120
    }
}
