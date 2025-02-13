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

/**
 * Repository class for managing points.
 * Handles CRUD operations and data persistence for points.
 */
class PointsRepository {
    private val fileName = "data.json"
    private var list = mutableListOf<Point>()
    private var maxId = AtomicInteger(0)

    /**
     * Retrieves the list of points.
     *
     * @return List of points currently stored.
     */
    fun getPoints(): List<Point> = list

    /**
     * Generates a new unique ID for a point.
     *
     * @return New unique ID.
     */
    private fun getNewId(): Int = maxId.incrementAndGet()

    /**
     * Finds a point by its ID.
     *
     * @param id The ID of the point.
     * @return The matching point or null if not found.
     */
    fun getPointById(id: Int): Point? {
        return list.find { it.id == id }
    }

    /**
     * Deletes a point by its ID.
     *
     * @param id The ID of the point to be deleted.
     * @return True if the point was found and deleted, false otherwise.
     */
    fun deletePointById(id: Int): Boolean {
        val point = list.find { it.id == id }
        return if (point != null) {
            list.remove(point)
            true
        } else {
            false
        }
    }

    /**
     * Loads points from the stored JSON file asynchronously.
     *
     * @param context The application context used to access the file.
     */
    suspend fun loadPoints(context: Context) {
        withContext(Dispatchers.IO) {
            val data = readFile(context)
            list = if (data != null) getPointsFromString(data).toMutableList() else mutableListOf()
            maxId.set(list.lastOrNull()?.id ?: 0)
        }
    }

    /**
     * Reads the stored JSON file.
     *
     * @param context The application context used to access the file.
     * @return The file content as a string, or null if the file does not exist.
     */
    private suspend fun readFile(context: Context): String? {
        return withContext(Dispatchers.IO) {
            val file = File(context.filesDir, fileName)
            if (file.exists()) file.readText() else null
        }
    }

    /**
     * Saves the current points list to the JSON file asynchronously.
     *
     * @param context The application context used to access the file.
     */
    suspend fun saveToFile(context: Context) {
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, fileName)
            file.writeText(getStringFromPoints(list))
        }
    }

    /**
     * Deletes all stored points and clears the file asynchronously.
     *
     * @param context The application context used to access the file.
     */
    suspend fun deletePoints(context: Context) {
        withContext(Dispatchers.IO) {
            list.clear()
            saveToFile(context)
        }
    }

    /**
     * Adds a new point using coordinates.
     *
     * @param name The name of the point.
     * @param latitude The latitude value.
     * @param longitude The longitude value.
     * @param latitudeDirection The latitude direction (N/S).
     * @param longitudeDirection The longitude direction (E/W).
     */
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

    /**
     * Adds a point by searching for a place name asynchronously.
     *
     * @param name The name of the place.
     * @return True if the place was found and added, false otherwise.
     */
    suspend fun addPointByName(name: String): Boolean {
        val point = searchPlace(name)
        return if (point != null) {
            list.add(point)
            true
        } else {
            false
        }
    }

    /**
     * Converts a JSON string into a list of points.
     *
     * @param data The JSON data representing points.
     * @return A list of points.
     */
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

    /**
     * Converts a list of points into a JSON string.
     *
     * @param points The list of points to convert.
     * @return JSON string representation of the points list.
     */
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

    /**
     * Searches for a location using a Nominatim API.
     *
     * @param query The location name.
     * @return A Point object if the place is found, or null otherwise.
     */
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

    /**
     * Extracts a single point from a JSON response.
     *
     * @param response The JSON response from the API.
     * @return A Point object.
     */
    private fun getPoint(response: String): Point {
        val item = JSONArray(response).getJSONObject(0)

        val name = item.getString("name")
        val latitude = item.getString("lat").toDouble()
        val longitude = item.getString("lon").toDouble()

        return Point(getNewId(), latitude, longitude, name)
    }
}
