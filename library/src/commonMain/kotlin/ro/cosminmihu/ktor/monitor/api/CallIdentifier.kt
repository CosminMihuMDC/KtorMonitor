package ro.cosminmihu.ktor.monitor.api

import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
internal val callIdentifier: String
    get() {
        val timestamp = Clock.System.now().toEpochMilliseconds().toString()
        val randomLong = Random.nextLong().toString()
        val uuid = Uuid.random()
        val raw = "$timestamp-$randomLong-$uuid"
        val hash = abs(raw.hashCode()).toString(16)
        return "$raw-$hash"
    }
