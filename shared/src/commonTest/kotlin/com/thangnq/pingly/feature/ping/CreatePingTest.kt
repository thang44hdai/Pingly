package com.thangnq.pingly.feature.ping

import com.thangnq.pingly.feature.ping.domain.model.Ping
import com.thangnq.pingly.feature.ping.domain.usecase.CreatePing
import com.thangnq.pingly.feature.ping.domain.usecase.CreatePingResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreatePingTest {
    @Test
    fun trimsMessageBeforeSaving() = runTest {
        val repository = FakePingRepository()

        val result = CreatePing(repository)("  Hello from Pingly  ")

        assertEquals(CreatePingResult.Created(Ping(1, "Hello from Pingly")), result)
        assertEquals(listOf("Hello from Pingly"), repository.addedMessages)
    }

    @Test
    fun rejectsBlankAndOverlongMessagesWithoutWriting() = runTest {
        val repository = FakePingRepository()
        val createPing = CreatePing(repository)

        assertEquals(
            CreatePingResult.Invalid(CreatePingResult.InvalidReason.Empty),
            createPing("  \n  "),
        )
        assertEquals(
            CreatePingResult.Invalid(
                CreatePingResult.InvalidReason.TooLong(CreatePing.MAX_MESSAGE_LENGTH)
            ),
            createPing("x".repeat(CreatePing.MAX_MESSAGE_LENGTH + 1)),
        )
        assertTrue(repository.addedMessages.isEmpty())
    }
}
