package cz.cuni.mff.hubinkok.totravel

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

fun addPointByName(name: String){
    //TODO
}

fun addPointByCoordinates(name: String, latitude: Double, longitude: Double,
                          latitudeDirection: LatitudeDirection,
                          longitudeDirection: LongitudeDirection) {
    val lat = if (latitudeDirection == LatitudeDirection.N) latitude else -latitude
    val lon = if (longitudeDirection == LongitudeDirection.E) longitude else -longitude
    Points.list.add(Point(lat, lon, name))
}

data class Point (
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val note: String = ""
)

object Points {
    private const val FILE_NAME = "data.json"
    var list: MutableList<Point> = mutableListOf()

    suspend fun loadPoints(context: Context) {
        withContext(Dispatchers.IO) {
            val data = readFile(context)
            list = if (data != null) getPointsFromString(data).toMutableList() else mutableListOf()
        }
    }

    fun savePoints(context: Context) {
        saveToFile(context, getStringFromPoints(list))
    }

    fun deletePoints() {
        list = mutableListOf()
    }

    private fun readFile(context: Context): String? {
        val file = File(context.filesDir, FILE_NAME)
        return if (file.exists()) file.readText() else null
    }

    private fun saveToFile(context: Context, data: String) {
        val file = File(context.filesDir, FILE_NAME)

        if (!file.exists()) {
            file.createNewFile()
        }

        file.writeText(data)
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

            val name = properties.optString("name", "")
            val note = properties.optString("note", "")

            points.add(Point(latitude, longitude, name, note))
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
            properties.put("name", point.name)
            properties.put("note", point.note)

            feature.put("geometry", geometry)
            feature.put("properties", properties)
            featuresArray.put(feature)
        }

        featureCollection.put("features", featuresArray)
        return featureCollection.toString(4) // Pretty print JSON with 4 spaces
    }
}