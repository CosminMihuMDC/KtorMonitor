package ro.cosminmihu.ktor.monitor.db

import app.cash.sqldelight.ColumnAdapter
import kotlinx.serialization.json.Json

internal val HeadersAdapter = object : ColumnAdapter<Map<String, List<String>>, String> {

    override fun decode(databaseValue: String): Map<String, List<String>> =
        Json.decodeFromString(databaseValue)

    override fun encode(value: Map<String, List<String>>): String =
        Json.encodeToString(value)
}