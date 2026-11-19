package com.example.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Lightweight procedural minimalist sound effects synthesizer.
 * Uses pure sine harmonics with soft exponential decay — zero bundled audio assets required.
 */
class SoundEffectsPlayer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Pentatonic chime frequencies (C5, D5, E5, G5, A5, C6) for pleasant ascending combos
    private val escapeNotes = doubleArrayOf(523.25, 587.33, 659.25, 783.99, 880.00, 1046.50)

    fun playEscape(comboIndex: Int) {
        val freq = escapeNotes[comboIndex.coerceAtLeast(0) % escapeNotes.size]
        scope.launch {
            playTone(freqStart = freq, freqEnd = freq * 1.18, durationMs = 110, volume = 0.22f)
        }
    }

    fun playBlocked() {
        scope.launch {
            playTone(freqStart = 185.0, freqEnd = 120.0, durationMs = 135, volume = 0.28f)
        }
    }

    fun playHint() {
        scope.launch {
            playTone(freqStart = 660.0, freqEnd = 880.0, durationMs = 160, volume = 0.18f)
        }
    }

    fun playVictory() {
        scope.launch {
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
            for (note in notes) {
                playTone(freqStart = note, freqEnd = note, durationMs = 120, volume = 0.22f)
            }
        }
    }

    private fun playTone(freqStart: Double, freqEnd: Double, durationMs: Int, volume: Float) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * durationMs) / 1000
            if (numSamples <= 0) return

            val pcm = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val freq = freqStart + (freqEnd - freqStart) * t
                phase += 2.0 * PI * freq / sampleRate
                // Smooth attack + exponential release envelope
                val attack = (i / (sampleRate * 0.006)).coerceAtMost(1.0)
                val decay = exp(-5.2 * t)
                val sample = sin(phase) * attack * decay * volume
                pcm[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )
            val bufferSize = maxOf(minBuf, pcm.size * 2)
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 30L)
            track.stop()
            track.release()
        } catch (_: Throwable) {
            // Silently ignore audio failures on muted/headless environments
        }
    }
}
