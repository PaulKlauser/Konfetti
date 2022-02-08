package nl.dionsegijn.konfetti.core.emitter

import java.util.concurrent.TimeUnit

/**
 * Emitter class that holds the duration that the emitter will create confetti particles
 */
data class Emitter(
    val duration: Long,
    val timeUnit: TimeUnit = TimeUnit.MILLISECONDS
) {
    /**
     * Max amount of particles that will be created over the duration that is set
     */
    fun max(amount: Int): EmitterConfig = EmitterConfig(this).max(amount)

    /**
     * Amount of particles that will be created per second
     */
    fun perSecond(amount: Int): EmitterConfig = EmitterConfig(this).perSecond(amount)
}

/**
 * EmitterConfig class that will gold the Emitter configuration and amount of particles that
 * will be created over certain time
 */
class EmitterConfig(
    emitter: Emitter
) {

    /** Max time allowed to emit in milliseconds */
    var emittingTimeMs: Float = 0f

    /** Amount of time needed for each particle creation in milliseconds */
    var msPerConfetti: Float = 0f

    init {
        val (duration, timeUnit) = emitter
        this.emittingTimeMs = TimeUnit.MILLISECONDS.convert(duration, timeUnit).toFloat()
    }

    /**
     * Amount of particles created over the duration that is set
     */
    fun max(amount: Int): EmitterConfig {
        this.msPerConfetti = emittingTimeMs / amount
        return this
    }

    /**
     * Amount of particles that will be created per second
     */
    fun perSecond(amount: Int): EmitterConfig {
        this.msPerConfetti = 1000f / amount
        return this
    }
}
