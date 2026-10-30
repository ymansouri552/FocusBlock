package com.focusblock.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.collection.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Thread-safe memory cache and asynchronous icon loader for installed apps.
 * Prevents main thread IPC jank and frame drops when scrolling large app lists.
 */
object AppIconCache {
    private const val MAX_CACHE_SIZE = 250
    private val memoryCache = LruCache<String, ImageBitmap>(MAX_CACHE_SIZE)

    fun get(packageName: String): ImageBitmap? = memoryCache.get(packageName)

    fun put(packageName: String, bitmap: ImageBitmap) {
        memoryCache.put(packageName, bitmap)
    }

    fun clear() {
        memoryCache.evictAll()
    }

    fun size(): Int = memoryCache.size()

    suspend fun loadIcon(
        context: Context,
        packageName: String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ImageBitmap? {
        get(packageName)?.let { return it }

        return withContext(dispatcher) {
            runCatching {
                val pm = context.packageManager
                val drawable = pm.getApplicationIcon(packageName)
                val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 96
                val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 96
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                val imageBitmap = bitmap.asImageBitmap()
                put(packageName, imageBitmap)
                imageBitmap
            }.getOrNull()
        }
    }
}
