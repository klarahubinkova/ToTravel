package cz.cuni.mff.hubinkok.totravel

import android.content.Context
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

suspend fun searchPlace(query: String): Point? {
    val urlString = "https://nominatim.openstreetmap.org/search?format=json&q=$query"
    val url = URL(urlString)
    val connection = withContext(Dispatchers.IO) {
        url.openConnection()
    } as HttpURLConnection

    try {
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "ToTravel")

        val responseCode = connection.responseCode

        if (responseCode != HttpURLConnection.HTTP_OK) {
            return null
        }

        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = reader.readText()
        withContext(Dispatchers.IO) {
            reader.close()
        }

        return getPoint(response)
    } catch (_: Exception) {
        return null
    } finally {
        connection.disconnect()
    }
}

fun getPoint(response: String): Point {
    val item = JSONArray(response).getJSONObject(0)

    val name = item.getString("name")
    val latitude = item.getString("lat").toDouble()
    val longitude = item.getString("lon").toDouble()

    return Point(Points.getNewId(), latitude, longitude, name)
}

suspend fun addPointByName(name: String){
    withContext(Dispatchers.IO) {
        val point = searchPlace(name)

        if (point == null) {
            throw NoSuchElementException()
        }
        else {
            Points.list.add(point)
        }
    }
}

fun addPointByCoordinates(name: String, latitude: Double, longitude: Double,
                          latitudeDirection: LatitudeDirection,
                          longitudeDirection: LongitudeDirection) {
    val lat = if (latitudeDirection == LatitudeDirection.N) latitude else -latitude
    val lon = if (longitudeDirection == LongitudeDirection.E) longitude else -longitude
    Points.list.add(Point(Points.getNewId(), lat, lon, name))
}

data class Point (
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    var name: String,
    var note: String = "",
    var tag: PointTag = PointTag.VISIT
)

object Points {
    private const val FILE_NAME = "data.json"
    private val maxId = AtomicInteger(0)
    var list: MutableList<Point> = mutableListOf()

    suspend fun loadPoints(context: Context) {
        withContext(Dispatchers.IO) {
            val data = readFile(context)
            list = if (data != null) getPointsFromString(data).toMutableList() else mutableListOf()
            maxId.set(list.lastOrNull()?.id ?: 0)
        }
    }

    suspend fun savePoints(context: Context) {
        withContext(Dispatchers.IO) {
            saveToFile(context, getStringFromPoints(list))
        }
    }

    suspend fun deletePoints(context: Context) {
        withContext(Dispatchers.IO) {
            list = mutableListOf()
            savePoints(context)
        }
    }

    fun getNewId(): Int {
        return maxId.incrementAndGet()
    }

    fun getPointById(id: Int): Point? {
        return list.find { it.id == id }
    }

    fun deletePointById(id: Int) {
        list.removeIf { it.id == id }
    }

    private suspend fun readFile(context: Context): String? {
        return withContext(Dispatchers.IO) {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) file.readText() else null
        }
    }

    private suspend fun saveToFile(context: Context, data: String) {
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, FILE_NAME)
            file.outputStream().use { it.write(data.toByteArray()) }
        }
    }

    private fun getPointsFromString(data: String): List<Point> {
        val points = mutableListOf<Point>()
        val jsonObject = JSONObject(data)

        val featuresArray = jsonObject.getJSONArray("features")
        for (i in 0 until featuresArray.length()) {
            val feature = featuresArray.getJSONObject(i)
            val geometry = feature.getJSONObject("geometry")
            val properties = feature.getJSONObject("properties")

            val coordinates = geometry.getJSONArray("coordinates")
            val longitude = coordinates.getDouble(0)
            val latitude = coordinates.getDouble(1)

            val id = properties.getInt("id")
            val name = properties.optString("name", "")
            val note = properties.optString("note", "")
            val tagString = properties.optString("tag", "VISIT")
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
        val featureCollection = JSONObject()
        featureCollection.put("type", "FeatureCollection")

        val featuresArray = JSONArray()
        for (point in points) {
            val feature = JSONObject()
            feature.put("type", "Feature")

            val geometry = JSONObject()
            geometry.put("type", "Point")
            geometry.put("coordinates", JSONArray(listOf(point.longitude, point.latitude)))

            val properties = JSONObject()
            properties.put("id", point.id)
            properties.put("name", point.name)
            properties.put("note", point.note)
            properties.put("tag", point.tag.name)

            feature.put("geometry", geometry)
            feature.put("properties", properties)
            featuresArray.put(feature)
        }

        featureCollection.put("features", featuresArray)
        return featureCollection.toString(4) // Pretty print JSON with 4 spaces
    }
}