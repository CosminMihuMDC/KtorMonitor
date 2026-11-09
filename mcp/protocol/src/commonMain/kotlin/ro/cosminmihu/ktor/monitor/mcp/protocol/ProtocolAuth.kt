package ro.cosminmihu.ktor.monitor.mcp.protocol

import kotlin.random.Random

/*
 * Mutual challenge/response authentication, so the shared token never travels over the socket
 * and an impostor listening on the port can neither learn it nor pose as the companion.
 *
 *   app       -> companion : Hello(client_nonce)
 *   companion -> app       : Challenge(server_nonce, proof = HMAC(token, "companion" | client_nonce | server_nonce))
 *   app       -> companion : Auth(proof = HMAC(token, "app" | server_nonce | client_nonce))
 *   companion -> app       : HelloAck
 *
 * Without a token both proofs are `null`.
 */

private const val NONCE_BYTES = 16

internal fun newNonce(): String = Random.Default.nextBytes(NONCE_BYTES).toHex()

internal fun companionProof(token: String, clientNonce: String, serverNonce: String): String =
    hmacSha256(token.encodeToByteArray(), "companion|$clientNonce|$serverNonce".encodeToByteArray()).toHex()

internal fun appProof(token: String, clientNonce: String, serverNonce: String): String =
    hmacSha256(token.encodeToByteArray(), "app|$serverNonce|$clientNonce".encodeToByteArray()).toHex()

/** Compares without exiting early on the first different character. */
internal fun constantTimeEquals(a: String?, b: String?): Boolean {
    if (a == null || b == null) return false
    var diff = a.length xor b.length
    for (i in 0 until minOf(a.length, b.length)) diff = diff or (a[i].code xor b[i].code)
    return diff == 0
}

private fun ByteArray.toHex(): String = joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }

internal fun hmacSha256(key: ByteArray, message: ByteArray): ByteArray {
    val blockSize = 64
    val normalizedKey = (if (key.size > blockSize) sha256(key) else key).copyOf(blockSize)
    val inner = ByteArray(blockSize) { (normalizedKey[it].toInt() xor 0x36).toByte() }
    val outer = ByteArray(blockSize) { (normalizedKey[it].toInt() xor 0x5c).toByte() }
    return sha256(outer + sha256(inner + message))
}

private val K = uintArrayOf(
    0x428a2f98u, 0x71374491u, 0xb5c0fbcfu, 0xe9b5dba5u, 0x3956c25bu, 0x59f111f1u, 0x923f82a4u, 0xab1c5ed5u,
    0xd807aa98u, 0x12835b01u, 0x243185beu, 0x550c7dc3u, 0x72be5d74u, 0x80deb1feu, 0x9bdc06a7u, 0xc19bf174u,
    0xe49b69c1u, 0xefbe4786u, 0x0fc19dc6u, 0x240ca1ccu, 0x2de92c6fu, 0x4a7484aau, 0x5cb0a9dcu, 0x76f988dau,
    0x983e5152u, 0xa831c66du, 0xb00327c8u, 0xbf597fc7u, 0xc6e00bf3u, 0xd5a79147u, 0x06ca6351u, 0x14292967u,
    0x27b70a85u, 0x2e1b2138u, 0x4d2c6dfcu, 0x53380d13u, 0x650a7354u, 0x766a0abbu, 0x81c2c92eu, 0x92722c85u,
    0xa2bfe8a1u, 0xa81a664bu, 0xc24b8b70u, 0xc76c51a3u, 0xd192e819u, 0xd6990624u, 0xf40e3585u, 0x106aa070u,
    0x19a4c116u, 0x1e376c08u, 0x2748774cu, 0x34b0bcb5u, 0x391c0cb3u, 0x4ed8aa4au, 0x5b9cca4fu, 0x682e6ff3u,
    0x748f82eeu, 0x78a5636fu, 0x84c87814u, 0x8cc70208u, 0x90befffau, 0xa4506cebu, 0xbef9a3f7u, 0xc67178f2u,
).map { it.toInt() }.toIntArray()

internal fun sha256(data: ByteArray): ByteArray {
    val h = uintArrayOf(
        0x6a09e667u, 0xbb67ae85u, 0x3c6ef372u, 0xa54ff53au, 0x510e527fu, 0x9b05688cu, 0x1f83d9abu, 0x5be0cd19u,
    ).map { it.toInt() }.toIntArray()
    val bitLength = data.size.toLong() * 8
    val paddedSize = ((data.size + 9 + 63) / 64) * 64
    val padded = data.copyOf(paddedSize)
    padded[data.size] = 0x80.toByte()
    for (i in 0 until 8) padded[paddedSize - 1 - i] = (bitLength ushr (8 * i)).toByte()

    val w = IntArray(64)
    for (chunk in 0 until paddedSize step 64) {
        for (i in 0 until 16) {
            val o = chunk + i * 4
            w[i] = ((padded[o].toInt() and 0xff) shl 24) or ((padded[o + 1].toInt() and 0xff) shl 16) or
                ((padded[o + 2].toInt() and 0xff) shl 8) or (padded[o + 3].toInt() and 0xff)
        }
        for (i in 16 until 64) {
            val s0 = w[i - 15].rotateRight(7) xor w[i - 15].rotateRight(18) xor (w[i - 15] ushr 3)
            val s1 = w[i - 2].rotateRight(17) xor w[i - 2].rotateRight(19) xor (w[i - 2] ushr 10)
            w[i] = w[i - 16] + s0 + w[i - 7] + s1
        }
        var a = h[0]; var b = h[1]; var c = h[2]; var d = h[3]
        var e = h[4]; var f = h[5]; var g = h[6]; var hh = h[7]
        for (i in 0 until 64) {
            val s1 = e.rotateRight(6) xor e.rotateRight(11) xor e.rotateRight(25)
            val ch = (e and f) xor (e.inv() and g)
            val t1 = hh + s1 + ch + K[i] + w[i]
            val s0 = a.rotateRight(2) xor a.rotateRight(13) xor a.rotateRight(22)
            val maj = (a and b) xor (a and c) xor (b and c)
            val t2 = s0 + maj
            hh = g; g = f; f = e; e = d + t1; d = c; c = b; b = a; a = t1 + t2
        }
        h[0] += a; h[1] += b; h[2] += c; h[3] += d; h[4] += e; h[5] += f; h[6] += g; h[7] += hh
    }
    return ByteArray(32) { (h[it / 4] ushr (24 - 8 * (it % 4))).toByte() }
}
