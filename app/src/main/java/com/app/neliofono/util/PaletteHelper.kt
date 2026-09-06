package com.app.neliofono.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.app.neliofono.model.VinylPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PaletteHelper {

    suspend fun extractPaletteFromUrl(context: Context, imageUrl: String?): VinylPalette? {
        if (imageUrl.isNullOrBlank()) return null

        return withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val req = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .allowHardware(false)
                    .build()

                val result = loader.execute(req)
                if (result is SuccessResult) {
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    bitmap?.let { extractFromBitmap(it) }
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    fun extractFromBitmap(bitmap: Bitmap): VinylPalette {
        val palette = Palette.from(bitmap).generate()

        val dominant = palette.dominantSwatch?.rgb?.let { Color(it) } ?: Color(0xFF8B263E)
        val vibrant = palette.vibrantSwatch?.rgb?.let { Color(it) } ?: Color(0xFFE27D60)
        val darkVibrant = palette.darkVibrantSwatch?.rgb?.let { Color(it) } ?: Color(0xFF41121F)
        val lightMuted = palette.lightMutedSwatch?.rgb?.let { Color(it) } ?: Color(0xFFE8A87C)

        return VinylPalette(
            dominant = dominant,
            vibrant = vibrant,
            darkVibrant = darkVibrant,
            lightMuted = lightMuted
        )
    }
}
