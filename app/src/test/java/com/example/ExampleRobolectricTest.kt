package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.CardinalDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Precision Compass", appName)
    }

    @Test
    fun `cardinal directions resolution is accurate`() {
        assertEquals(CardinalDirection.NORTH, CardinalDirection.fromDegrees(0f))
        assertEquals(CardinalDirection.NORTH, CardinalDirection.fromDegrees(359f))
        assertEquals(CardinalDirection.EAST, CardinalDirection.fromDegrees(90f))
        assertEquals(CardinalDirection.SOUTH, CardinalDirection.fromDegrees(180f))
        assertEquals(CardinalDirection.WEST, CardinalDirection.fromDegrees(270f))
        assertEquals(CardinalDirection.NORTH_EAST, CardinalDirection.fromDegrees(45f))
        assertEquals(CardinalDirection.SOUTH_WEST, CardinalDirection.fromDegrees(225f))
    }

    @Test
    fun `shortest angular difference avoids 360 degree spin glitch`() {
        // Transition from 359° to 2°: shortest delta should be +3°, not -357°
        val current = 359f
        val target = 2f
        var delta = (target - (current % 360f)) % 360f
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        assertEquals(3f, delta, 0.01f)

        // Transition from 1° to 358°: shortest delta should be -3°, not +357°
        val current2 = 1f
        val target2 = 358f
        var delta2 = (target2 - (current2 % 360f)) % 360f
        if (delta2 > 180f) delta2 -= 360f
        if (delta2 < -180f) delta2 += 360f
        assertEquals(-3f, delta2, 0.01f)
    }

    @Test
    fun `primary cardinal crossing detects North and East`() {
        assertTrue(CardinalDirection.isPrimaryCardinal(0.5f, tolerance = 1.0f))
        assertTrue(CardinalDirection.isPrimaryCardinal(359.5f, tolerance = 1.0f))
        assertTrue(CardinalDirection.isPrimaryCardinal(89.8f, tolerance = 1.0f))
    }

    @Test
    fun `haptic manager safely executes without throwing`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val hapticManager = com.example.domain.haptics.CompassHapticManager(context)
        hapticManager.performCardinalHaptic(CardinalDirection.NORTH)
        hapticManager.performCardinalHaptic(CardinalDirection.EAST)
        hapticManager.performCardinalHaptic(CardinalDirection.SOUTH)
        hapticManager.performCardinalHaptic(CardinalDirection.WEST)
    }

    @Test
    fun `launch MainActivity with Robolectric`() {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        org.junit.Assert.assertNotNull(activity)
    }
}
