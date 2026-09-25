package com.pdp.gotronome.data

import android.content.Context
import com.pdp.gotronome.Metronome

const val STYLE_METRONOME = "metronome"
const val STYLES_ASSET = "styles.txt"
const val MAX_STEPS_PER_BEAT = 4

/** A bass line: [stepsPerBeat] sub-steps per beat over [bars] measures, semitone offsets from the root or [Metronome.BASS_REST]. */
data class BassLine(val stepsPerBeat: Int, val bars: Int, val notes: List<Int>)

/** One measure of a drum style: [stepsPerBeat] sub-steps per beat, each a voice bitmask, plus an optional bass line. */
data class Groove(val stepsPerBeat: Int, val stepVoices: List<Int>, val bass: BassLine? = null)

const val MAX_BASS_BARS = 4

/** Display names of the twelve roots, index = pitch class from C. */
val rootNames: List<String> = listOf("C", "C♯", "D", "E♭", "E", "F", "F♯", "G", "A♭", "A", "B♭", "B")

/** MIDI note of a root pitch class placed in the bass register, E1 (28) up to E♭2 (39). */
fun bassRootMidi(pitchClass: Int): Int = 28 + ((pitchClass - 4).mod(12))

/** Grooves keyed by time signature. The metronome style has none and is valid everywhere. */
data class Style(val id: String, val name: String, val grooves: Map<String, Groove>)

val metronomeStyle = Style(STYLE_METRONOME, "Metronome", emptyMap())

fun Style.isMetronome(): Boolean = id == STYLE_METRONOME

fun Style.supports(timeSignature: String): Boolean =
    isMetronome() || grooves.containsKey(timeSignature)

fun stylesFor(styles: List<Style>, timeSignature: String): List<Style> =
    styles.filter { it.supports(timeSignature) }

/**
 * The style to actually play: the saved one when it has a groove for [timeSignature],
 * otherwise the metronome. The saved preference itself is left untouched by design.
 */
fun resolveStyle(styles: List<Style>, savedId: String, timeSignature: String): Style =
    styles.firstOrNull { it.id == savedId && it.supports(timeSignature) }
        ?: styles.firstOrNull { it.isMetronome() }
        ?: metronomeStyle

private val voiceLetters: Map<Char, Int> = mapOf(
    'K' to Metronome.Voice.KICK,
    'S' to Metronome.Voice.SNARE,
    'H' to Metronome.Voice.HAT_CLOSED,
    'P' to Metronome.Voice.HAT_PEDAL,
    'R' to Metronome.Voice.RIDE,
    'X' to Metronome.Voice.CROSS_STICK,
)

private fun parseStep(token: String, line: Int): Int =
    if (token == ".") 0
    else token.fold(0) { mask, letter ->
        mask or (voiceLetters[letter]
            ?: throw IllegalArgumentException("$STYLES_ASSET:$line unknown voice '$letter'"))
    }

private fun parseGroove(fields: List<String>, line: Int): Pair<String, Groove> {
    require(fields.size >= 3) { "$STYLES_ASSET:$line groove needs <time signature> <steps per beat> <steps>" }
    val timeSignature = fields[0]
    require(timeSignature in timeSignatures) { "$STYLES_ASSET:$line unknown time signature '$timeSignature'" }
    val stepsPerBeat = fields[1].toIntOrNull()
    require(stepsPerBeat != null && stepsPerBeat in 1..MAX_STEPS_PER_BEAT) {
        "$STYLES_ASSET:$line steps per beat must be 1..$MAX_STEPS_PER_BEAT"
    }
    val tokens = fields.drop(2)
    val expected = beatsForTimeSignature(timeSignature) * stepsPerBeat
    require(tokens.size == expected) { "$STYLES_ASSET:$line expected $expected steps, got ${tokens.size}" }
    return timeSignature to Groove(stepsPerBeat, tokens.map { parseStep(it, line) })
}

private fun parseBassLine(fields: List<String>, style: Style, line: Int): Pair<String, BassLine> {
    require(fields.size >= 3) { "$STYLES_ASSET:$line bass needs <time signature> <steps per beat> <steps>" }
    val timeSignature = fields[0]
    require(style.grooves.containsKey(timeSignature)) { "$STYLES_ASSET:$line bass line before the $timeSignature groove" }
    val stepsPerBeat = fields[1].toIntOrNull()
    require(stepsPerBeat != null && stepsPerBeat in 1..MAX_STEPS_PER_BEAT) {
        "$STYLES_ASSET:$line steps per beat must be 1..$MAX_STEPS_PER_BEAT"
    }
    val tokens = fields.drop(2).filter { it != "|" }
    val perBar = beatsForTimeSignature(timeSignature) * stepsPerBeat
    require(tokens.isNotEmpty() && tokens.size % perBar == 0 && tokens.size / perBar <= MAX_BASS_BARS) {
        "$STYLES_ASSET:$line expected a multiple of $perBar steps up to $MAX_BASS_BARS bars, got ${tokens.size}"
    }
    val notes = tokens.map { token ->
        if (token == ".") Metronome.BASS_REST
        else token.toIntOrNull() ?: throw IllegalArgumentException("$STYLES_ASSET:$line bad bass note '$token'")
    }
    return timeSignature to BassLine(stepsPerBeat, tokens.size / perBar, notes)
}

/** Parses the shared styles file. Throws [IllegalArgumentException] naming the offending line. */
fun parseStyles(text: String): List<Style> {
    val styles = mutableListOf<Style>()
    text.lineSequence().forEachIndexed { index, raw ->
        val line = index + 1
        val fields = raw.substringBefore('#').trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (fields.isEmpty()) return@forEachIndexed
        when (fields[0]) {
            "style" -> {
                require(fields.size >= 3) { "$STYLES_ASSET:$line style needs <id> <name>" }
                styles += Style(fields[1], fields.drop(2).joinToString(" "), emptyMap())
            }
            "groove" -> {
                val current = styles.lastOrNull()
                    ?: throw IllegalArgumentException("$STYLES_ASSET:$line groove before any style")
                require(!current.isMetronome()) { "$STYLES_ASSET:$line the metronome style takes no grooves" }
                val (timeSignature, groove) = parseGroove(fields.drop(1), line)
                styles[styles.lastIndex] = current.copy(grooves = current.grooves + (timeSignature to groove))
            }
            "bass" -> {
                val current = styles.lastOrNull()
                    ?: throw IllegalArgumentException("$STYLES_ASSET:$line bass before any style")
                val (timeSignature, bass) = parseBassLine(fields.drop(1), current, line)
                val groove = current.grooves.getValue(timeSignature).copy(bass = bass)
                styles[styles.lastIndex] = current.copy(grooves = current.grooves + (timeSignature to groove))
            }
            else -> throw IllegalArgumentException("$STYLES_ASSET:$line unknown directive '${fields[0]}'")
        }
    }
    require(styles.any { it.isMetronome() }) { "$STYLES_ASSET has no metronome style" }
    return styles
}

fun loadStyles(context: Context): List<Style> =
    context.assets.open(STYLES_ASSET).bufferedReader().use { parseStyles(it.readText()) }
