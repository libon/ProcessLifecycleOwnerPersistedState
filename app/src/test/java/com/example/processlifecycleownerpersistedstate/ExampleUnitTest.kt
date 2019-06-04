package com.example.processlifecycleownerpersistedstate

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {
    @Test
    fun testLaunchActivity() {
        val controller = Robolectric.buildActivity(MainActivity::class.java)
            .create().start().resume().visible()
        assertEquals(Lifecycle.State.RESUMED, ProcessLifecycleOwner.get().lifecycle.currentState)
        controller.pause().stop().destroy()
    }
}
