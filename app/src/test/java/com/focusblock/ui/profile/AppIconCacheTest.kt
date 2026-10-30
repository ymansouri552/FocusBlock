package com.focusblock.ui.profile

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AppIconCacheTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppIconCache.clear()
    }

    @After
    fun tearDown() {
        AppIconCache.clear()
    }

    @Test
    fun get_whenEmpty_returnsNull() {
        assertNull(AppIconCache.get("com.example.nonexistent"))
    }

    @Test
    fun putAndGet_storesAndRetrievesIcon() {
        val bitmap = ImageBitmap(32, 32)
        AppIconCache.put("com.example.test", bitmap)

        assertEquals(1, AppIconCache.size())
        assertEquals(bitmap, AppIconCache.get("com.example.test"))
    }

    @Test
    fun clear_evictsAllEntries() {
        AppIconCache.put("com.example.test1", ImageBitmap(16, 16))
        AppIconCache.put("com.example.test2", ImageBitmap(16, 16))
        assertEquals(2, AppIconCache.size())

        AppIconCache.clear()
        assertEquals(0, AppIconCache.size())
        assertNull(AppIconCache.get("com.example.test1"))
    }

    @Test
    fun loadIcon_whenPackageNotFound_returnsNullGracefully() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val result = AppIconCache.loadIcon(context, "com.nonexistent.fakeapp", testDispatcher)
        assertNull(result)
    }

    @Test
    fun loadIcon_whenCached_returnsFromCacheImmediately() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val bitmap = ImageBitmap(24, 24)
        AppIconCache.put("com.example.cached", bitmap)

        val result = AppIconCache.loadIcon(context, "com.example.cached", testDispatcher)
        assertEquals(bitmap, result)
    }
}
