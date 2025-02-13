package cz.cuni.mff.hubinkok.totravel.repository

import android.content.Context
import cz.cuni.mff.hubinkok.totravel.data.LatitudeDirection
import cz.cuni.mff.hubinkok.totravel.data.LongitudeDirection
import cz.cuni.mff.hubinkok.totravel.data.Point
import cz.cuni.mff.hubinkok.totravel.data.PointTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

class PointsRepository {
    private val fileName = "data.json"
    private var list = mutableListOf<Point>()
    private var maxId = AtomicInteger(0)

    fun getPoints(): List<Point> = list

    private fun getNewId(): Int = maxId.incrementAndGet()

    fun getPointById(id: Int): Point? {
        return list.find { it.id == id }
    }

    fun deletePointById(id: Int): Boolean {
        val point = list.find { it.id == id }
        return if (point != null) {
            list.remove(point)
            true
        } else {
            false
        }
    }

    suspend fun loadPoints(context: Context) {
        withContext(Dispatchers.IO) {
            val data = readFile(context)
            list = if (data != null) getPointsFromString(data).toMutableList() else mutableListOf()
            maxId.set(list.lastOrNull()?.id ?: 0)
        }
    }

    private suspend fun readFile(context: Context): String? {
        return withContext(Dispatchers.IO) {
            val file = File(context.filesDir, fileName)
            if (file.exists()) file.readText() else null
        }
    }

    suspend fun saveToFile(context: Context) {
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, fileName)
            file.writeText(getStringFromPoints(list))
        }
    }

    suspend fun deletePoints(context: Context) {
        withContext(Dispatchers.IO) {
            list.clear()
            saveToFile(context)
        }
    }

    fun addPointByCoordinates(
        name: String,
        latitude: Double,
        longitude: Double,
        latitudeDirection: LatitudeDirection,
        longitudeDirection: LongitudeDirection
    ) {
        val lat = if (latitudeDirection == LatitudeDirection.N) latitude else -latitude
        val lon = if (longitudeDirection == LongitudeDirection.E) longitude else -longitude

        list.add(Point(getNewId(), lat, lon, name))
    }

    suspend fun addPointByName(name: String): Boolean {
        val point = searchPlace(name)
        return if (point != null) {
            list.add(point)
            true
        } else {
            false
        }
    }

    private fun getPointsFromString(data: String): List<Point> {
        val points = mutableListOf<Point>()
        val jsonArray = JSONArray(data)

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val id = item.getInt("id")
            val latitude = item.getDouble("latitude")
            val longitude = item.getDouble("longitude")
            val name = item.getString("name")
            val note = item.optString("note", "")
            val tagString = item.optString("tag", "VISIT")
            val tag = try {
                PointTag.valueOf(tagString)
            } catch (_: IllegalArgumentException) {
                PointTag.VISIT
            }

            points.add(Point(id, latitude, longitude, name, note, tag))
        }
        return points
    }

    private fun getStringFromPoints(points: List<Point>): String {
        val jsonArray = JSONArray()
        for (point in points) {
            val jsonObject = JSONObject().apply {
                put("id", point.id)
                put("latitude", point.latitude)
                put("longitude", point.longitude)
                put("name", point.name)
                put("note", point.note)
                put("tag", point.tag.name)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    private suspend fun searchPlace(query: String): Point? {
        val urlString = "https://nominatim.openstreetmap.org/search?format=json&q=$query"
        val url = URL(urlString)
        val connection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection

        return try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "ToTravel")

            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.readText()
            withContext(Dispatchers.IO) { reader.close() }

            getPoint(response)
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun getPoint(response: String): Point {
        val item = JSONArray(response).getJSONObject(0)

        val name = item.getString("name")
        val latitude = item.getString("lat").toDouble()
        val longitude = item.getString("lon").toDouble()

        return Point(getNewId(), latitude, longitude, name)
    }
}
