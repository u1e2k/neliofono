package com.app.neliofono.data

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.app.neliofono.model.TrackInfo
import com.app.neliofono.model.VinylPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class PlaylistRepository(private val context: Context) {

    private val currentQueueFile = File(context.filesDir, "current_queue.json")
    private val namedPlaylistsDir = File(context.filesDir, "playlists").apply {
        if (!exists()) mkdirs()
    }

    suspend fun saveCurrentQueue(tracks: List<TrackInfo>, currentIndex: Int) = withContext(Dispatchers.IO) {
        try {
            val jsonObject = JSONObject().apply {
                put("currentIndex", currentIndex)
                put("tracks", tracksToJsonArray(tracks))
            }
            currentQueueFile.writeText(jsonObject.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadCurrentQueue(): Pair<List<TrackInfo>, Int>? = withContext(Dispatchers.IO) {
        if (!currentQueueFile.exists()) return@withContext null
        try {
            val content = currentQueueFile.readText()
            if (content.isBlank()) return@withContext null
            val jsonObject = JSONObject(content)
            val currentIndex = jsonObject.optInt("currentIndex", 0)
            val tracksArray = jsonObject.optJSONArray("tracks") ?: return@withContext null
            val tracks = jsonArrayToTracks(tracksArray)
            if (tracks.isEmpty()) null else tracks to currentIndex
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveNamedPlaylist(name: String, tracks: List<TrackInfo>) = withContext(Dispatchers.IO) {
        try {
            val safeName = name.replace(Regex("[^a-zA-Z0-9_\\-\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FFF]"), "_")
            val file = File(namedPlaylistsDir, "$safeName.json")
            val jsonObject = JSONObject().apply {
                put("name", name)
                put("tracks", tracksToJsonArray(tracks))
            }
            file.writeText(jsonObject.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadNamedPlaylist(name: String): List<TrackInfo>? = withContext(Dispatchers.IO) {
        try {
            val safeName = name.replace(Regex("[^a-zA-Z0-9_\\-\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FFF]"), "_")
            val file = File(namedPlaylistsDir, "$safeName.json")
            if (!file.exists()) return@withContext null
            val content = file.readText()
            val jsonObject = JSONObject(content)
            val tracksArray = jsonObject.optJSONArray("tracks") ?: return@withContext null
            jsonArrayToTracks(tracksArray)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getSavedPlaylistNames(): List<String> = withContext(Dispatchers.IO) {
        try {
            namedPlaylistsDir.listFiles { f -> f.extension == "json" }
                ?.map { it.nameWithoutExtension }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteNamedPlaylist(name: String) = withContext(Dispatchers.IO) {
        try {
            val safeName = name.replace(Regex("[^a-zA-Z0-9_\\-\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FFF]"), "_")
            val file = File(namedPlaylistsDir, "$safeName.json")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun tracksToJsonArray(tracks: List<TrackInfo>): JSONArray {
        val array = JSONArray()
        for (track in tracks) {
            val obj = JSONObject().apply {
                put("id", track.id)
                put("title", track.title)
                put("artist", track.artist)
                put("album", track.album)
                put("durationMs", track.durationMs)
                put("coverUrl", track.coverUrl ?: "")
                put("mediaUri", track.mediaUri?.toString() ?: "")
                put("dominantColor", track.defaultPalette.dominant.value.toLong())
                put("vibrantColor", track.defaultPalette.vibrant.value.toLong())
                put("darkVibrantColor", track.defaultPalette.darkVibrant.value.toLong())
                put("lightMutedColor", track.defaultPalette.lightMuted.value.toLong())
            }
            array.put(obj)
        }
        return array
    }

    private fun jsonArrayToTracks(array: JSONArray): List<TrackInfo> {
        val list = mutableListOf<TrackInfo>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val mediaUriStr = obj.optString("mediaUri")
            val coverUrlStr = obj.optString("coverUrl")

            val palette = VinylPalette(
                dominant = Color(obj.optLong("dominantColor", 0xFF8B1E3FL).toULong()),
                vibrant = Color(obj.optLong("vibrantColor", 0xFFD94F70L).toULong()),
                darkVibrant = Color(obj.optLong("darkVibrantColor", 0xFF3F0A1DL).toULong()),
                lightMuted = Color(obj.optLong("lightMutedColor", 0xFFE89BA7L).toULong())
            )

            list.add(
                TrackInfo(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    artist = obj.getString("artist"),
                    album = obj.getString("album"),
                    durationMs = obj.getLong("durationMs"),
                    coverUrl = if (coverUrlStr.isNullOrBlank()) null else coverUrlStr,
                    mediaUri = if (mediaUriStr.isNullOrBlank()) null else Uri.parse(mediaUriStr),
                    defaultPalette = palette
                )
            )
        }
        return list
    }
}
