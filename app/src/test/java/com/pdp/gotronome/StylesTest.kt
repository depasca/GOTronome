package com.pdp.gotronome

import com.pdp.gotronome.data.FOURFOURS
import com.pdp.gotronome.data.SIXEIGHTS
import com.pdp.gotronome.data.STYLES_ASSET
import com.pdp.gotronome.data.STYLE_METRONOME
import com.pdp.gotronome.data.THREEFOURS
import com.pdp.gotronome.data.TWOFOURS
import com.pdp.gotronome.data.TWOTWOS
import com.pdp.gotronome.data.beatsForTimeSignature
import com.pdp.gotronome.data.parseStyles
import com.pdp.gotronome.data.resolveStyle
import com.pdp.gotronome.data.stylesFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

class StylesTest {
    private val bundled = parseStyles(File("src/main/assets/$STYLES_ASSET").readText())

    @Test
    fun bundledFileDefinesEveryPlannedGroove() {
        val expected = mapOf(
            STYLE_METRONOME to emptySet(),
            "rock" to setOf(FOURFOURS, SIXEIGHTS),
            "swing" to setOf(FOURFOURS),
            "shuffle" to setOf(FOURFOURS),
            "waltz" to setOf(THREEFOURS),
            "bossa" to setOf(FOURFOURS),
            "march" to setOf(TWOFOURS, TWOTWOS),
        )
        assertEquals(expected, bundled.associate { it.id to it.grooves.keys })
        bundled.flatMap { it.grooves.entries }.forEach { (timeSignature, groove) ->
            assertEquals(beatsForTimeSignature(timeSignature) * groove.stepsPerBeat, groove.stepVoices.size)
        }
    }

    @Test
    fun metronomeIsValidEverywhereAndListedFirst() {
        listOf(FOURFOURS, THREEFOURS, TWOFOURS, TWOTWOS, SIXEIGHTS).forEach { timeSignature ->
            assertEquals(STYLE_METRONOME, stylesFor(bundled, timeSignature).first().id)
        }
    }

    @Test
    fun savedStyleWithoutGrooveFallsBackToMetronome() {
        assertEquals("swing", resolveStyle(bundled, "swing", FOURFOURS).id)
        assertEquals(STYLE_METRONOME, resolveStyle(bundled, "swing", THREEFOURS).id)
        assertEquals(STYLE_METRONOME, resolveStyle(bundled, "no-such-style", FOURFOURS).id)
    }

    @Test
    fun stepTokensBecomeVoiceMasks() {
        val styles = parseStyles(
            """
            style metronome Metronome
            style test Test # trailing comment
            groove 2/4 2 KH . S .
            """.trimIndent()
        )
        val groove = styles.last().grooves.getValue(TWOFOURS)
        assertEquals(2, groove.stepsPerBeat)
        assertEquals(
            listOf(Metronome.Voice.KICK or Metronome.Voice.HAT_CLOSED, 0, Metronome.Voice.SNARE, 0),
            groove.stepVoices
        )
    }

    @Test
    fun wrongStepCountIsRejectedWithLineNumber() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            parseStyles("style metronome Metronome\nstyle bad Bad\ngroove 4/4 2 K S")
        }
        assertEquals(true, error.message!!.contains(":3 "))
    }
}
