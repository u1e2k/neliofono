package com.app.neliofono.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.Color
import com.app.neliofono.model.TrackInfo
import com.app.neliofono.model.VinylPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioScanner(private val context: Context) {

    private val presetPalettes = listOf(
        VinylPalette(
            dominant = Color(0xFF8B1E3F),
            vibrant = Color(0xFFD94F70),
            darkVibrant = Color(0xFF3F0A1D),
            lightMuted = Color(0xFFE89BA7)
        ),
        VinylPalette(
            dominant = Color(0xFF1E4D6B),
            vibrant = Color(0xFF3897C5),
            darkVibrant = Color(0xFF0D2535),
            lightMuted = Color(0xFF8EC5E0)
        ),
        VinylPalette(
            dominant = Color(0xFFB86B1B),
            vibrant = Color(0xFFE89A3C),
            darkVibrant = Color(0xFF5E3206),
            lightMuted = Color(0xFFFFD580)
        ),
        VinylPalette(
            dominant = Color(0xFF1A5E42),
            vibrant = Color(0xFF2EB886),
            darkVibrant = Color(0xFF0B2E20),
            lightMuted = Color(0xFFA3E5CB)
        ),
        VinylPalette(
            dominant = Color(0xFF5B2C6F),
            vibrant = Color(0xFF8E44AD),
            darkVibrant = Color(0xFF2C133B),
            lightMuted = Color(0xFFD7BDE2)
        )
    )

    suspend fun scanAudioFiles(): List<TrackInfo> = withContext(Dispatchers.IO) {
        val trackList = mutableListOf<TrackInfo>()
        val contentResolver: ContentResolver = context.contentResolver

        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )

        // Only query valid music files with duration > 5 seconds
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 5000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                var index = 0
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Title"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    // Album Art Uri
                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()

                    val palette = presetPalettes[index % presetPalettes.size]

                    trackList.add(
                        TrackInfo(
                            id = id.toString(),
                            title = title,
                            artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                            album = if (album == "<unknown>") "Unknown Album" else album,
                            durationMs = duration,
                            coverUrl = albumArtUri,
                            mediaUri = contentUri,
                            defaultPalette = palette
                        )
                    )
                    index++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        trackList
    }
}
