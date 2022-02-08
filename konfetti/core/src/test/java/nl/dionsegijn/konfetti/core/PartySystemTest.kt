package nl.dionsegijn.konfetti.core

import android.graphics.Rect
import nl.dionsegijn.konfetti.core.emitter.Emitter
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito

class PartySystemTest {

    private val rect: Rect = Mockito.mock(Rect::class.java).apply {
        Mockito.`when`(height()).thenReturn(1000)
        Mockito.`when`(contains(anyInt(), anyInt())).thenReturn(true)
    }

    // Average between for each frame
    private val deltaTimeMs = 17f

    @Test
    fun `Test creating particle every 25ms`() {
        val party = Party(
            emitter = Emitter(100L).max(4)
        )
        val system = PartySystem(party, pixelDensity = 1f)

        Assert.assertTrue(system.enabled)
        Assert.assertFalse(system.isDoneEmitting())

        val r1 = system.render(deltaTimeMs, rect) // render 2, total deltaTime = 0.017f
        Assert.assertEquals(0, r1.size) // Expected 0, Every 0.025ms a new particle should be created

        val r2 = system.render(deltaTimeMs, rect) // render 2, total deltaTime = 2 * 0.017f = 0.034f
        Assert.assertEquals(1, r2.size) // Expected 1, one for every 0.025ms

        val r3 = system.render(deltaTimeMs, rect) // render 3, total deltaTime = 3 * 0.017f = 0.051f
        Assert.assertEquals(2, r3.size) // expected 2, one for every 0.025ms
    }

    @Test
    fun `Test creating Particles with high initial deltaTime`() {
        val party = Party(
            emitter = Emitter(100L).max(2)
        )
        val system = PartySystem(party, pixelDensity = 1f)

        Assert.assertTrue(system.enabled)
        Assert.assertFalse(system.isDoneEmitting())

        // Particles are removed because alpha is 0 with so much time passed
        val r1 = system.render(60000f, rect)
        Assert.assertEquals(0, r1.size)
    }

    @Test
    fun `Test PartySystem set to disabled stops generating particles`() {
        val party = Party(
            emitter = Emitter(100L).max(4)
        )
        val system = PartySystem(party, pixelDensity = 1f)

        Assert.assertTrue(system.enabled)
        Assert.assertFalse(system.isDoneEmitting())

        val r1 = system.render(deltaTimeMs, rect) // render 2, total deltaTime = 0.017f
        Assert.assertEquals(0, r1.size) // Expected 0, Every 0.025ms a new particle should be created

        val r2 = system.render(deltaTimeMs, rect) // render 2, total deltaTime = 2 * 0.017f = 0.034f
        Assert.assertEquals(1, r2.size) // Expected 1, one for every 0.025ms

        // System set to false, emitter will no longer asked for new particles
        system.enabled = false
        Assert.assertFalse(system.enabled)

        // Should not longer create new particles even though time has passed
        val r3 = system.render(deltaTimeMs, rect)
        Assert.assertEquals(1, r3.size)
    }

    @Test
    fun `Test PartySystem is done Emitting`() {
        val party = Party(
            timeToLive = 1f,
            fadeOutEnabled = false,
            emitter = Emitter(100).max(2)
        )
        val system = PartySystem(party, pixelDensity = 1f)

        // Set drawArea to 1 pixel to let every particle directly disappear for this test
        Mockito.`when`(rect.height()).thenReturn(1)
        Assert.assertTrue(system.enabled)

        system.render(deltaTimeMs, rect) // dt: 0.017f
        system.render(deltaTimeMs, rect) // dt: 0.034f
        system.render(deltaTimeMs, rect) // dt: 0.051f
        system.render(deltaTimeMs, rect) // dt: 0.068f
        system.render(deltaTimeMs, rect) // dt: 0.085f

        // should still run because emitter isn't done yet, total delta time is < 100ms
        Assert.assertFalse(system.isDoneEmitting())

        system.render(deltaTimeMs, rect) // dt: 0.102f // duration is higher than 100ms

        Assert.assertEquals(0, system.getActiveParticleAmount())
        Assert.assertTrue(system.isDoneEmitting())
    }

    @Test
    fun `Test PartySystem remove dead particles`() {
        val party = Party(
            timeToLive = 18f, // removes particles after two frames
            fadeOutEnabled = false,
            emitter = Emitter(100L).max(5) // Create particle every 20ms
        )
        val system = PartySystem(party, pixelDensity = 1f)

        // Every 20ms a new particle is created and every two frames they're removed

        system.render(deltaTimeMs, rect) // dt: 0.017f
        system.render(deltaTimeMs, rect) // dt: 0.034f
        Assert.assertEquals(1, system.getActiveParticleAmount())

        system.render(deltaTimeMs, rect) // dt: 0.051f
        system.render(deltaTimeMs, rect) // dt: 0.068f
        system.render(deltaTimeMs, rect) // dt: 0.085f
        system.render(deltaTimeMs, rect) // dt: 0.102f

        // All particles are created and one extra frame is executed to remove the last one
        system.render(deltaTimeMs, rect) // dt: 0.119f
        Assert.assertEquals(0, system.getActiveParticleAmount())
    }
}
