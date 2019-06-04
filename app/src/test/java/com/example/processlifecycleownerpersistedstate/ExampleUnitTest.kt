package com.example.processlifecycleownerpersistedstate

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ProcessLifecycleTrojan
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
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

    @Before
    fun setup() {
        ProcessLifecycleTrojan.setup()
    }

    @After
    fun tearDown() {
        ProcessLifecycleTrojan.tearDown()
    }

    @Test
    fun testLaunchActivity() {
        val controller = Robolectric.buildActivity(MainActivity::class.java)
            .create().start().resume().visible()
        assertEquals(Lifecycle.State.RESUMED, ProcessLifecycleOwner.get().lifecycle.currentState)
        controller.pause().stop().destroy()
    }
}
