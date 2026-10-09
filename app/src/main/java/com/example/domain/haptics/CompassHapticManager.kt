package com.example.domain.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.domain.model.CardinalDirection

/**
 * Manages subtle, tactile haptic feedback for cardinal direction transitions.
 * Utilizes [VibratorManager] on Android 12+ (API 31+) with fallback to [Vibrator],
 * leveraging composition primitives (PRIMITIVE_TICK, PRIMITIVE_CLICK) for rich tactile realism.
 */
class CompassHapticManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var lastHapticTime: Long = 0L
    private val debounceThresholdMs = 120L // Prevent rapid jitter buzz

    /**
     * Emits a subtle tactile tick when crossing a cardinal direction.
     * North produces a distinctive signature tick, while East, South, and West
     * emit a light, mechanical dial tick.
     */
    fun performCardinalHaptic(direction: CardinalDirection) {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        val now = System.currentTimeMillis()
        if (now - lastHapticTime < debounceThresholdMs) return
        lastHapticTime = now

        val isNorth = direction == CardinalDirection.NORTH

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            playModernPrimitiveHaptic(vib, isNorth)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            playPredefinedHaptic(vib, isNorth)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            playOneShotHaptic(vib, isNorth)
        } else {
            playLegacyHaptic(vib, isNorth)
        }
    }

    private fun playModernPrimitiveHaptic(vib: Vibrator, isNorth: Boolean) {
        try {
            val primitive = if (isNorth) {
                VibrationEffect.Composition.PRIMITIVE_CLICK
            } else {
                VibrationEffect.Composition.PRIMITIVE_TICK
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && vib.areAllPrimitivesSupported(primitive)) {
                val scale = if (isNorth) 0.85f else 0.45f
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(primitive, scale)
                    .compose()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val attributes = VibrationAttributes.Builder()
                        .setUsage(VibrationAttributes.USAGE_TOUCH)
                        .build()
                    vib.vibrate(effect, attributes)
                } else {
                    vib.vibrate(effect)
                }
                return
            }
        } catch (_: Exception) {
            // Fall through to predefined fallback
        }

        playPredefinedHaptic(vib, isNorth)
    }

    private fun playPredefinedHaptic(vib: Vibrator, isNorth: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val effectId = if (isNorth) {
                    VibrationEffect.EFFECT_CLICK
                } else {
                    VibrationEffect.EFFECT_TICK
                }
                vib.vibrate(VibrationEffect.createPredefined(effectId))
                return
            } catch (_: Exception) {
                // Fall through to one-shot
            }
        }
        playOneShotHaptic(vib, isNorth)
    }

    private fun playOneShotHaptic(vib: Vibrator, isNorth: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val durationMs = if (isNorth) 16L else 8L
            val amplitude = if (isNorth) 85 else 40
            vib.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
        } else {
            playLegacyHaptic(vib, isNorth)
        }
    }

    @Suppress("DEPRECATION")
    private fun playLegacyHaptic(vib: Vibrator, isNorth: Boolean) {
        vib.vibrate(if (isNorth) 18L else 10L)
    }
}
