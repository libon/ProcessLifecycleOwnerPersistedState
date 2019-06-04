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
    fun testLaunchActivity1() {
        testLaunchActivity()
    }

    @Test
    fun testLaunchActivity2() {
        testLaunchActivity()
    }

    private fun testLaunchActivity() {
        assertEquals(Lifecycle.State.CREATED, ProcessLifecycleOwner.get().lifecycle.currentState)
        val controller = Robolectric.buildActivity(MainActivity::class.java)
            .create().start().resume().visible()
        assertEquals(Lifecycle.State.RESUMED, ProcessLifecycleOwner.get().lifecycle.currentState)
        // Note: if we comment out the following 3 lines, and also comment out our tearDown(), testLaunchActivity1()
        // will pass, but testLaunchActivity2() will fail, because the ProcessLifecycleOwner's state at the
        // beginning of the second test will be RESUMED instead of CREATED (left-over from the end of the first test).
        controller.pause().stop().destroy()
        Robolectric.flushForegroundThreadScheduler()
        assertEquals(Lifecycle.State.CREATED, ProcessLifecycleOwner.get().lifecycle.currentState)
    }
}
