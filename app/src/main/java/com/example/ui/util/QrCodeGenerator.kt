package com.example.ui.util

import java.nio.charset.StandardCharsets

data class QrMatrix(
    val size: Int,
    val modules: BooleanArray
) {
    fun isDark(row: Int, col: Int): Boolean {
        if (row !in 0 until size || col !in 0 until size) return false
        return modules[row * size + col]
    }
}

/**
 * Pure-Kotlin ISO/IEC 18004 QR Code encoder (Byte mode, ECC Level L, Versions 1..8).
 * Zero external dependencies or permissions required for fully offline Passport QR rendering.
 */
object QrCodeGenerator {

    private data class VersionSpec(
        val version: Int,
        val totalCodewords: Int,
        val ecCodewordsPerBlock: Int,
        val numBlocks: Int,
        val dataCodewords: Int,
        val alignmentCenters: IntArray
    )

    private val versions = listOf(
        VersionSpec(1, 26, 7, 1, 19, intArrayOf()),
        VersionSpec(2, 44, 10, 1, 34, intArrayOf(6, 18)),
        VersionSpec(3, 70, 15, 1, 55, intArrayOf(6, 22)),
        VersionSpec(4, 100, 20, 1, 80, intArrayOf(6, 26)),
        VersionSpec(5, 134, 26, 1, 108, intArrayOf(6, 30)),
        VersionSpec(6, 172, 18, 2, 136, intArrayOf(6, 34)),
        VersionSpec(7, 196, 20, 2, 156, intArrayOf(6, 22, 38)),
        VersionSpec(8, 242, 24, 2, 194, intArrayOf(6, 24, 42))
    )

    fun encode(text: String): QrMatrix {
        val rawBytes = text.toByteArray(StandardCharsets.UTF_8)
        // Byte mode header for versions 1..9 is 4 bits (mode) + 8 bits (length) = 12 bits
        val spec = versions.firstOrNull { spec ->
            val maxDataBytes = (spec.dataCodewords * 8 - 12) / 8
            rawBytes.size <= maxDataBytes
        } ?: versions.last()

        val maxPayload = (spec.dataCodewords * 8 - 12) / 8
        val payload = if (rawBytes.size > maxPayload) rawBytes.copyOf(maxPayload) else rawBytes

        val dataCodewords = buildDataCodewords(payload, spec.dataCodewords)
        val allCodewords = addEccAndInterleave(dataCodewords, spec)

        val size = spec.version * 4 + 17
        val modules = BooleanArray(size * size)
        val isFunction = BooleanArray(size * size)

        drawFunctionPatterns(size, spec, modules, isFunction)
        drawCodewords(size, allCodewords, modules, isFunction)

        var bestMask = 0
        var minPenalty = Int.MAX_VALUE
        for (mask in 0..7) {
            val candidate = modules.copyOf()
            applyMask(size, candidate, isFunction, mask)
            drawFormatBits(size, candidate, isFunction, mask)
            val penalty = computePenalty(size, candidate)
            if (penalty < minPenalty) {
                minPenalty = penalty
                bestMask = mask
            }
        }

        applyMask(size, modules, isFunction, bestMask)
        drawFormatBits(size, modules, isFunction, bestMask)

        return QrMatrix(size = size, modules = modules)
    }

    private fun buildDataCodewords(payload: ByteArray, dataCodewordsCount: Int): ByteArray {
        val bits = ArrayList<Boolean>(dataCodewordsCount * 8)

        fun appendBits(value: Int, count: Int) {
            for (i in count - 1 downTo 0) {
                bits.add(((value ushr i) and 1) != 0)
            }
        }

        // Byte mode indicator = 0100
        appendBits(0b0100, 4)
        // Character count indicator (8 bits for versions 1..9)
        appendBits(payload.size, 8)
        for (b in payload) {
            appendBits(b.toInt() and 0xFF, 8)
        }

        val capacityBits = dataCodewordsCount * 8
        val terminatorBits = minOf(4, capacityBits - bits.size)
        repeat(terminatorBits) { bits.add(false) }
        while (bits.size % 8 != 0) {
            bits.add(false)
        }

        val result = ByteArray(dataCodewordsCount)
        val byteLen = bits.size / 8
        for (i in 0 until byteLen) {
            var v = 0
            for (b in 0 until 8) {
                if (bits[i * 8 + b]) v = v or (1 shl (7 - b))
            }
            result[i] = v.toByte()
        }
        var padByte = 0xEC
        for (i in byteLen until dataCodewordsCount) {
            result[i] = padByte.toByte()
            padByte = if (padByte == 0xEC) 0x11 else 0xEC
        }
        return result
    }

    private fun addEccAndInterleave(data: ByteArray, spec: VersionSpec): ByteArray {
        val blockDataLen = spec.dataCodewords / spec.numBlocks
        val ecLen = spec.ecCodewordsPerBlock
        val blocksData = Array(spec.numBlocks) { i ->
            data.copyOfRange(i * blockDataLen, (i + 1) * blockDataLen)
        }
        val blocksEcc = Array(spec.numBlocks) { i ->
            computeReedSolomonEcc(blocksData[i], ecLen)
        }

        val out = ByteArray(spec.totalCodewords)
        var idx = 0
        for (i in 0 until blockDataLen) {
            for (b in 0 until spec.numBlocks) {
                out[idx++] = blocksData[b][i]
            }
        }
        for (i in 0 until ecLen) {
            for (b in 0 until spec.numBlocks) {
                out[idx++] = blocksEcc[b][i]
            }
        }
        return out
    }

    private fun computeReedSolomonEcc(data: ByteArray, ecLen: Int): ByteArray {
        val gen = IntArray(ecLen + 1)
        gen[0] = 1
        var root = 1
        for (i in 0 until ecLen) {
            for (j in i + 1 downTo 1) {
                gen[j] = gen[j] xor gfMul(gen[j - 1], root)
            }
            root = gfMul(root, 0x02)
        }

        val rem = IntArray(ecLen)
        for (byte in data) {
            val factor = (byte.toInt() and 0xFF) xor rem[0]
            for (j in 0 until ecLen - 1) {
                rem[j] = rem[j + 1]
            }
            rem[ecLen - 1] = 0
            for (j in 0 until ecLen) {
                rem[j] = rem[j] xor gfMul(gen[j + 1], factor)
            }
        }
        return ByteArray(ecLen) { rem[it].toByte() }
    }

    private fun gfMul(x: Int, y: Int): Int {
        var a = x
        var b = y
        var res = 0
        while (b > 0) {
            if ((b and 1) != 0) res = res xor a
            a = a shl 1
            if ((a and 0x100) != 0) a = a xor 0x11D
            b = b ushr 1
        }
        return res
    }

    private fun drawFunctionPatterns(
        size: Int,
        spec: VersionSpec,
        modules: BooleanArray,
        isFunction: BooleanArray
    ) {
        fun setFunc(r: Int, c: Int, dark: Boolean) {
            if (r in 0 until size && c in 0 until size) {
                val idx = r * size + c
                modules[idx] = dark
                isFunction[idx] = true
            }
        }

        // Timing patterns
        for (i in 0 until size) {
            setFunc(6, i, i % 2 == 0)
            setFunc(i, 6, i % 2 == 0)
        }

        // 3 Finder patterns + separators
        fun drawFinder(top: Int, left: Int) {
            for (dr in -1..7) {
                for (dc in -1..7) {
                    val r = top + dr
                    val c = left + dc
                    if (r !in 0 until size || c !in 0 until size) continue
                    val dist = maxOf(kotlin.math.abs(dr - 3), kotlin.math.abs(dc - 3))
                    val dark = dist != 2 && dist != 4
                    setFunc(r, c, dark)
                }
            }
        }
        drawFinder(0, 0)
        drawFinder(0, size - 7)
        drawFinder(size - 7, 0)

        // Alignment patterns (V2+)
        val centers = spec.alignmentCenters
        for (rCenter in centers) {
            for (cCenter in centers) {
                val nearTopLeft = rCenter < 9 && cCenter < 9
                val nearTopRight = rCenter < 9 && cCenter > size - 10
                val nearBottomLeft = rCenter > size - 10 && cCenter < 9
                if (nearTopLeft || nearTopRight || nearBottomLeft) continue
                for (dr in -2..2) {
                    for (dc in -2..2) {
                        val dist = maxOf(kotlin.math.abs(dr), kotlin.math.abs(dc))
                        setFunc(rCenter + dr, cCenter + dc, dist != 1)
                    }
                }
            }
        }

        // Format info reserve (dummy false; filled after mask selection)
        for (i in 0..8) {
            if (i != 6) {
                setFunc(8, i, false)
                setFunc(i, 8, false)
            }
        }
        for (i in 0..7) {
            setFunc(8, size - 1 - i, false)
        }
        for (i in 0..6) {
            setFunc(size - 1 - i, 8, false)
        }
        // Dark module
        setFunc(size - 8, 8, true)

        // Version information (V7+)
        if (spec.version >= 7) {
            val verBits = computeVersionBits(spec.version)
            for (i in 0 until 18) {
                val dark = ((verBits ushr i) and 1) != 0
                val a = size - 11 + i % 3
                val b = i / 3
                setFunc(a, b, dark)
                setFunc(b, a, dark)
            }
        }
    }

    private fun drawCodewords(
        size: Int,
        codewords: ByteArray,
        modules: BooleanArray,
        isFunction: BooleanArray
    ) {
        var bitIdx = 0
        val totalBits = codewords.size * 8
        var right = size - 1
        var upward = true
        while (right >= 1) {
            if (right == 6) right--
            for (vert in 0 until size) {
                val r = if (upward) size - 1 - vert else vert
                for (dx in 0..1) {
                    val c = right - dx
                    val cell = r * size + c
                    if (!isFunction[cell]) {
                        val dark = if (bitIdx < totalBits) {
                            val byteVal = codewords[bitIdx ushr 3].toInt() and 0xFF
                            ((byteVal ushr (7 - (bitIdx and 7))) and 1) != 0
                        } else {
                            false
                        }
                        modules[cell] = dark
                        bitIdx++
                    }
                }
            }
            upward = !upward
            right -= 2
        }
    }

    private fun applyMask(
        size: Int,
        modules: BooleanArray,
        isFunction: BooleanArray,
        mask: Int
    ) {
        for (r in 0 until size) {
            for (c in 0 until size) {
                val idx = r * size + c
                if (isFunction[idx]) continue
                val invert = when (mask) {
                    0 -> (r + c) % 2 == 0
                    1 -> r % 2 == 0
                    2 -> c % 3 == 0
                    3 -> (r + c) % 3 == 0
                    4 -> (r / 2 + c / 3) % 2 == 0
                    5 -> (r * c) % 2 + (r * c) % 3 == 0
                    6 -> ((r * c) % 2 + (r * c) % 3) % 2 == 0
                    else -> ((r + c) % 2 + (r * c) % 3) % 2 == 0
                }
                if (invert) modules[idx] = !modules[idx]
            }
        }
    }

    fun computeFormatBitsLevelL(mask: Int): Int {
        // Error correction level L = 01
        val data = (0b01 shl 3) or (mask and 0b111)
        var rem = data shl 10
        val poly = 0x537
        for (i in 4 downTo 0) {
            if ((rem ushr (i + 10)) != 0) {
                rem = rem xor (poly shl i)
            }
        }
        return ((data shl 10) or rem) xor 0x5412
    }

    private fun computeVersionBits(version: Int): Int {
        var rem = version shl 12
        val poly = 0x1F25
        for (i in 5 downTo 0) {
            if ((rem ushr (i + 12)) != 0) {
                rem = rem xor (poly shl i)
            }
        }
        return (version shl 12) or rem
    }

    private fun drawFormatBits(
        size: Int,
        modules: BooleanArray,
        @Suppress("UNUSED_PARAMETER") isFunction: BooleanArray,
        mask: Int
    ) {
        val bits = computeFormatBitsLevelL(mask)
        for (i in 0..5) {
            modules[8 * size + i] = ((bits ushr i) and 1) != 0
        }
        modules[8 * size + 7] = ((bits ushr 6) and 1) != 0
        modules[8 * size + 8] = ((bits ushr 7) and 1) != 0
        modules[7 * size + 8] = ((bits ushr 8) and 1) != 0
        for (i in 9..14) {
            modules[(14 - i) * size + 8] = ((bits ushr i) and 1) != 0
        }
        for (i in 0..7) {
            modules[(size - 1 - i) * size + 8] = ((bits ushr i) and 1) != 0
        }
        for (i in 8..14) {
            modules[8 * size + (size - 15 + i)] = ((bits ushr i) and 1) != 0
        }
        modules[(size - 8) * size + 8] = true
    }

    private fun computePenalty(size: Int, modules: BooleanArray): Int {
        var penalty = 0
        // Rule 1: 5+ consecutive same-color modules in row/col
        for (r in 0 until size) {
            var runColor = modules[r * size]
            var runLen = 1
            for (c in 1 until size) {
                val color = modules[r * size + c]
                if (color == runColor) {
                    runLen++
                } else {
                    if (runLen >= 5) penalty += 3 + (runLen - 5)
                    runColor = color
                    runLen = 1
                }
            }
            if (runLen >= 5) penalty += 3 + (runLen - 5)
        }
        for (c in 0 until size) {
            var runColor = modules[c]
            var runLen = 1
            for (r in 1 until size) {
                val color = modules[r * size + c]
                if (color == runColor) {
                    runLen++
                } else {
                    if (runLen >= 5) penalty += 3 + (runLen - 5)
                    runColor = color
                    runLen = 1
                }
            }
            if (runLen >= 5) penalty += 3 + (runLen - 5)
        }
        // Rule 2: 2x2 blocks
        for (r in 0 until size - 1) {
            for (c in 0 until size - 1) {
                val m0 = modules[r * size + c]
                if (m0 == modules[r * size + c + 1] &&
                    m0 == modules[(r + 1) * size + c] &&
                    m0 == modules[(r + 1) * size + c + 1]
                ) {
                    penalty += 3
                }
            }
        }
        return penalty
    }
}
