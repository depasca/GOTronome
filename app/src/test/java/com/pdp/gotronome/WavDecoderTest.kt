package com.pdp.gotronome

import com.pdp.gotronome.data.decodeWav
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WavDecoderTest {
    private fun wav(channels: Int, bits: Int, rate: Int, body: ByteBuffer.() -> Unit, bodyBytes: Int): ByteArray {
        val buffer = ByteBuffer.allocate(44 + bodyBytes).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray()).putInt(36 + bodyBytes).put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray()).putInt(16).putShort(1).putShort(channels.toShort()).putInt(rate)
            .putInt(rate * channels * bits / 8).putShort((channels * bits / 8).toShort()).putShort(bits.toShort())
        buffer.put("data".toByteArray()).putInt(bodyBytes)
        buffer.body()
        return buffer.array()
    }

    @Test
    fun stereo16BitIsMixedToMono() {
        val bytes = wav(2, 16, 44100, { putShort(16384); putShort(-16384); putShort(32767); putShort(32767) }, 8)
        val clip = decodeWav(bytes)
        assertEquals(44100, clip.sampleRate)
        assertArrayEquals(floatArrayOf(0f, 32767f / 32768f), clip.frames, 1e-6f)
    }

    @Test
    fun mono24BitIsScaledToFullScale() {
        val bytes = wav(1, 24, 48000, { put(0); put(0); put(0x40); put(0); put(0); put(0xC0.toByte()) }, 6)
        assertArrayEquals(floatArrayOf(0.5f, -0.5f), decodeWav(bytes).frames, 1e-6f)
    }
}
