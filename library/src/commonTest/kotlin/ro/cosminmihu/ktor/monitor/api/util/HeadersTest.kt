package ro.cosminmihu.ktor.monitor.api.util

import io.ktor.http.Headers
import io.ktor.http.headersOf
import ro.cosminmihu.ktor.monitor.SanitizedHeader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HeadersTest {

    @Test
    fun testAuthorizationHeaderSanitizedCaseInsensitive() {
        val headers = Headers.build {
            append("Authorization", "secretToken")
            append("authorization", "anotherSecret")
            append("AUTHORIZATION", "yetAnotherSecret")
            append("Content-Type", "application/json")
        }
        val rules = listOf(
            SanitizedHeader(placeholder = "***") { headerName -> headerName.equals("Authorization", ignoreCase = true) }
        )
        val sanitized = headers.sanitizedHeaders(rules)

        // Assuming the sanitization process normalizes header names to the case used in the rule,
        // or preserves original casing but still sanitizes based on case-insensitive match.
        // For this example, let's assume keys in the output map might match the *first* encountered case
        // or a normalized one. The critical part is that all variants are sanitized.

        // Check if "Authorization" (original case) is sanitized
        assertEquals(listOf("***"), sanitized["Authorization"], "Header 'Authorization' should be sanitized.")
        // Check if "authorization" (lowercase) is sanitized
        assertEquals(listOf("***"), sanitized["authorization"], "Header 'authorization' should be sanitized.")
        // Check if "AUTHORIZATION" (uppercase) is sanitized
        assertEquals(listOf("***"), sanitized["AUTHORIZATION"], "Header 'AUTHORIZATION' should be sanitized.")
        // Check that "Content-Type" remains untouched
        assertEquals(listOf("application/json"), sanitized["Content-Type"], "Header 'Content-Type' should not be sanitized.")
    }

    @Test
    fun testBasicSanitizationWithDefaultPlaceholder() {
        val headers = headersOf("Sensitive-Data", "confidential")
        val rules = listOf(
            SanitizedHeader { it == "Sensitive-Data" } // Default placeholder "***"
        )
        val sanitized = headers.sanitizedHeaders(rules)
        assertEquals(listOf("***"), sanitized["Sensitive-Data"])
    }

    @Test
    fun testBasicSanitizationWithCustomPlaceholder() {
        val headers = headersOf("Sensitive-Data", "confidential")
        val rules = listOf(
            SanitizedHeader("[REDACTED]") { it == "Sensitive-Data" }
        )
        val sanitized = headers.sanitizedHeaders(rules)
        assertEquals(listOf("[REDACTED]"), sanitized["Sensitive-Data"])
    }

    @Test
    fun testNoSanitizationWhenPredicateNotMatched() {
        val headers = headersOf("Non-Sensitive-Data", "public")
        val rules = listOf(
            SanitizedHeader { it == "Sensitive-Data" }
        )
        val sanitized = headers.sanitizedHeaders(rules)
        assertEquals(listOf("public"), sanitized["Non-Sensitive-Data"])
    }

    @Test
    fun testMultipleHeadersSanitization() {
        val headers = Headers.build {
            append("Authorization", "token")
            append("X-Api-Key", "key")
            append("Content-Type", "application/xml")
        }
        val rules = listOf(
            SanitizedHeader { it.equals("Authorization", ignoreCase = true) },
            SanitizedHeader("[HIDDEN]") { it.equals("X-Api-Key", ignoreCase = true) }
        )
        val sanitized = headers.sanitizedHeaders(rules)
        assertEquals(listOf("***"), sanitized["Authorization"])
        assertEquals(listOf("[HIDDEN]"), sanitized["X-Api-Key"])
        assertEquals(listOf("application/xml"), sanitized["Content-Type"])
    }

    @Test
    fun testMultipleMatchingPredicatesFirstOneTakesPrecedence() {
        val headers = headersOf("X-Specific-Data", "very-secret")
        val rules = listOf(
            SanitizedHeader("SpecificPlaceholder") { it == "X-Specific-Data" },
            SanitizedHeader("GenericPlaceholder") { it.startsWith("X-") }
        )
        val sanitized = headers.sanitizedHeaders(rules)
        assertEquals(listOf("SpecificPlaceholder"), sanitized["X-Specific-Data"])
    }

    @Test
    fun testHeaderWithMultipleValuesIsSanitized() {
        val headers = Headers.build {
            append("Set-Cookie", "session=123")
            append("Set-Cookie", "user=abc")
        }
        val rules = listOf(
            SanitizedHeader { it.equals("Set-Cookie", ignoreCase = true) }
        )
        val sanitized = headers.sanitizedHeaders(rules)
        assertEquals(listOf("***"), sanitized["Set-Cookie"])
    }

    @Test
    fun testEmptyHeaderValueSanitizedIfKeyMatches() {
        val headers = headersOf("Empty-Val-Header", "")
        val rules = listOf(
            SanitizedHeader { it == "Empty-Val-Header" }
        )
        val sanitized = headers.sanitizedHeaders(rules)
        assertEquals(listOf("***"), sanitized["Empty-Val-Header"])
    }

    @Test
    fun testEmptyHeaderValueNotSanitizedIfKeyDoesNotMatch() {
        val headers = headersOf("Empty-Val-Header", "")
        val rules = listOf(
            SanitizedHeader { it == "Other-Header" }
        )
        val sanitized = headers.sanitizedHeaders(rules)
        assertEquals(listOf(""), sanitized["Empty-Val-Header"])
    }

    @Test
    fun testSpecialCharactersInHeaderNamesAndValues() {
        // This test primarily ensures that special characters don't break the iteration/matching,
        // assuming predicates are written to handle them if necessary.
        val headers = Headers.build {
            append("X-Token!@#", "value$%^")
            append("Normal-Header", "normal_value")
        }
        val rules = listOf(
            SanitizedHeader { it == "X-Token!@#" }
        )
        val sanitized = headers.sanitizedHeaders(rules)
        assertEquals(listOf("***"), sanitized["X-Token!@#"])
        assertEquals(listOf("normal_value"), sanitized["Normal-Header"])
    }

    @Test
    fun testEmptySanitizedHeadersListNoSanitization() {
        val headers = Headers.build {
            append("Authorization", "token")
            append("Content-Type", "application/json")
        }
        val rules = emptyList<SanitizedHeader>()
        val sanitized = headers.sanitizedHeaders(rules)
        assertEquals(listOf("token"), sanitized["Authorization"])
        assertEquals(listOf("application/json"), sanitized["Content-Type"])
        assertEquals(headers.entries().size, sanitized.size, "Number of headers should remain the same")
    }

    @Test
    fun testHeaderNotPresentInOriginalHeaders() {
        val headers = headersOf("Actual-Header", "present")
        val rules = listOf(SanitizedHeader { it == "Non-Existent-Header" })
        val sanitized = headers.sanitizedHeaders(rules)
        assertNull(sanitized["Non-Existent-Header"])
        assertEquals(listOf("present"), sanitized["Actual-Header"])
    }
}
