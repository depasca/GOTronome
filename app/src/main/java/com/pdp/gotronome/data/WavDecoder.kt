package com.pdp.gotronome.data

import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Mono PCM in -1..1 with its sample rate. */
data class PcmClip(val sampleRate: Int, val frames: FloatArray)

private const val FORMAT_PCM = 1
private const val FORMAT_IEEE_FLOAT = 3
private const val FORMAT_EXTENSIBLE = 0xFFFE

private data class WavFormat(val formatTag: Int, val channels: Int, val sampleRate: Int, val bitsPerSample: Int)

private fun readFormat(chunk: ByteBuffer): WavFormat {
    val formatTag = chunk.short.toInt() and 0xFFFF
    val channels = chunk.short.toInt()
    val sampleRate = chunk.int
    chunk.int   // byte rate
    chunk.short // block align
    val bitsPerSample = chunk.short.toInt()
    val effectiveTag = if (formatTag == FORMAT_EXTENSIBLE && chunk.remaining() >= 10) {
        chunk.short; chunk.short; chunk.int // cbSize, valid bits, channel mask
        chunk.short.toInt() and 0xFFFF      // first two bytes of the sub-format GUID
    } else formatTag
    return WavFormat(effectiveTag, channels, sampleRate, bitsPerSample)
}

private fun readSample(data: ByteBuffer, format: WavFormat): Float = when {
    format.formatTag == FORMAT_IEEE_FLOAT && format.bitsPerSample == 32 -> data.float
    format.bitsPerSample == 16 -> data.short / 32768f
    format.bitsPerSample == 24 -> {
        val b0 = data.get().toInt() and 0xFF
        val b1 = data.get().toInt() and 0xFF
        val b2 = data.get().toInt()
        ((b2 shl 16) or (b1 shl 8) or b0) / 8388608f
    }
    format.bitsPerSample == 32 -> data.int / 2147483648f
    format.bitsPerSample == 8 -> ((data.get().toInt() and 0xFF) - 128) / 128f
    else -> throw IllegalArgumentException("unsupported WAV: tag ${format.formatTag}, ${format.bitsPerSample} bits")
}

/** Decodes a RIFF/WAVE file (PCM 8/16/24/32-bit or 32-bit float, any channel count) to mono. */
fun decodeWav(bytes: ByteArray): PcmClip {
    val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
    require(bytes.size >= 12 && tag(buffer, 0) == "RIFF" && tag(buffer, 8) == "WAVE") { "not a WAV file" }
    var format: WavFormat? = null
    var position = 12
    while (position + 8 <= bytes.size) {
        val id = tag(buffer, position)
        val size = buffer.getInt(position + 4)
        val body = position + 8
        if (id == "fmt ") {
            format = readFormat(buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).apply { position(body); limit(body + size) })
        } else if (id == "data") {
            val fmt = requireNotNull(format) { "WAV data chunk before fmt chunk" }
            val data = buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).apply { position(body); limit(minOf(body + size, bytes.size)) }
            val bytesPerFrame = fmt.channels * fmt.bitsPerSample / 8
            val frames = FloatArray(data.remaining() / bytesPerFrame) {
                var sum = 0f
                repeat(fmt.channels) { sum += readSample(data, fmt) }
                sum / fmt.channels
            }
            return PcmClip(fmt.sampleRate, frames)
        }
        position = body + size + (size and 1)
    }
    throw IllegalArgumentException("WAV file has no data chunk")
}

private fun tag(buffer: ByteBuffer, at: Int): String =
    String(charArrayOf(buffer.get(at).toInt().toChar(), buffer.get(at + 1).toInt().toChar(),
        buffer.get(at + 2).toInt().toChar(), buffer.get(at + 3).toInt().toChar()))
