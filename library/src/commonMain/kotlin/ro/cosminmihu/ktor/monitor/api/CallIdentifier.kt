package ro.cosminmihu.ktor.monitor.api

import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal val callIdentifier
    get() = Clock.System.now().toEpochMilliseconds().toString() + "-" + Random.nextLong()