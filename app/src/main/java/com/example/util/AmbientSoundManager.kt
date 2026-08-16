package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

enum class AmbientSoundType(val displayName: String, val iconLabel: String) {
    NONE("Silent", "🔇"),
    RAIN("Rainfall", "🌧️"),
    WHITE_NOISE("White Noise", "💨"),
    TIBETAN_BOWL("Singing Bowl", "🔔"),
    FOREST_STREAM("Forest Stream", "🍃")
}

class AmbientSoundManager {
    private var audioTrack: AudioTrack? = null
    private var soundJob: Job? = null
    private var currentType = AmbientSoundType.NONE

    fun playSound(type: AmbientSoundType, scope: CoroutineScope) {
        if (currentType == type && audioTrack != null) return
        stopSound()
        if (type == AmbientSoundType.NONE) return

        currentType = type
        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(sampleRate / 4)

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
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
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()

            soundJob = scope.launch(Dispatchers.Default) {
                val buffer = ShortArray(1024)
                var phase = 0.0
                var brownNoise = 0.0

                while (isActive && audioTrack != null) {
                    when (type) {
                        AmbientSoundType.RAIN -> {
                            for (i in buffer.indices) {
                                val white = (Random.nextDouble() * 2.0 - 1.0)
                                brownNoise = (brownNoise + (0.04 * white)) / 1.04
                                val rainDrop = if (Random.nextDouble() < 0.003) (Random.nextDouble() * 0.4) else 0.0
                                val sample = (brownNoise * 0.35 + white * 0.05 + rainDrop).coerceIn(-1.0, 1.0)
                                buffer[i] = (sample * 16000).toInt().toShort()
                            }
                        }
                        AmbientSoundType.WHITE_NOISE -> {
                            for (i in buffer.indices) {
                                val white = (Random.nextDouble() * 2.0 - 1.0) * 0.18
                                buffer[i] = (white * Short.MAX_VALUE).toInt().toShort()
                            }
                        }
                        AmbientSoundType.TIBETAN_BOWL -> {
                            val freq = 216.0 // 432Hz harmonic / soothing fundamental
                            for (i in buffer.indices) {
                                val harmonic1 = sin(phase * 2.0 * Math.PI) * 0.35
                                val harmonic2 = sin(phase * 4.0 * Math.PI) * 0.15
                                val harmonic3 = sin(phase * 6.0 * Math.PI) * 0.08
                                val sample = harmonic1 + harmonic2 + harmonic3
                                buffer[i] = (sample * 18000).toInt().toShort()
                                phase += freq / sampleRate
                                if (phase > 1.0) phase -= 1.0
                            }
                        }
                        AmbientSoundType.FOREST_STREAM -> {
                            for (i in buffer.indices) {
                                val white = (Random.nextDouble() * 2.0 - 1.0)
                                brownNoise = (brownNoise + (0.02 * white)) / 1.02
                                val waterGurgle = sin(phase * 2.0 * Math.PI) * 0.12
                                val sample = (brownNoise * 0.28 + waterGurgle).coerceIn(-1.0, 1.0)
                                buffer[i] = (sample * 16000).toInt().toShort()
                                phase += 6.0 / sampleRate
                                if (phase > 1.0) phase -= 1.0
                            }
                        }
                        AmbientSoundType.NONE -> break
                    }

                    audioTrack?.write(buffer, 0, buffer.size)
                }
            }
        } catch (e: Exception) {
            // Graceful fallback on audio init failures
            stopSound()
        }
    }

    fun stopSound() {
        soundJob?.cancel()
        soundJob = null
        currentType = AmbientSoundType.NONE
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignore release exceptions
        } finally {
            audioTrack = null
        }
    }
}
