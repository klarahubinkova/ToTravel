package cz.cuni.mff.hubinkok.totravel.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cz.cuni.mff.hubinkok.totravel.data.LatitudeDirection
import cz.cuni.mff.hubinkok.totravel.data.LongitudeDirection
import cz.cuni.mff.hubinkok.totravel.data.Point
import cz.cuni.mff.hubinkok.totravel.repository.PointsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ViewModel for managing points data and interacting with the repository.
 */
class PointsViewModel : ViewModel() {
    private val _pointsList = MutableLiveData<List<Point>>(emptyList())
    val pointsList: LiveData<List<Point>> get() = _pointsList

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Repository responsible for handling data operations.
    private val repository = PointsRepository()

    /**
     * Updates the points list by fetching the latest data from the repository.
     */
    fun update() {
        _pointsList.postValue(repository.getPoints())
    }

    /**
     * Retrieves a point by its ID.
     * @param id The unique identifier of the point.
     * @return The corresponding [Point] object, or null if not found.
     */
    fun getPointById(id: Int): Point? {
        return repository.getPointById(id)
    }

    /**
     * Deletes a point by its ID and updates the points list if successful.
     * If the point does not exist, sets an error message.
     * @param id The ID of the point to delete.
     */
    fun deletePointById(id: Int) {
        val result = repository.deletePointById(id)
        if (result) {
            update()
        } else {
            setError("Point with ID $id does not exist")
        }
    }

    /**
     * Loads points from a file into the repository and updates the list.
     * @param context The application context required for file operations.
     */
    suspend fun loadPoints(context: Context) {
        repository.loadPoints(context)
        update()
    }

    /**
     * Saves the current points to a file.
     * @param context The application context required for file operations.
     */
    suspend fun savePoints(context: Context) {
        repository.saveToFile(context)
    }

    /**
     * Deletes all points from the repository and clears the list.
     * @param context The application context required for file operations.
     */
    suspend fun deletePoints(context: Context) {
        repository.deletePoints(context)
        _pointsList.postValue(emptyList())
    }

    /**
     * Searches for a place by name and adds it to the list if found.
     * Updates the list if successful, otherwise sets an error message.
     * @param name The name of the place to search for.
     */
    suspend fun addPointByName(name: String) {
        withContext(Dispatchers.IO) {
            val result = repository.addPointByName(name)
            if (result) {
                update()
            } else {
                setError("Place not found")
            }
        }
    }

    /**
     * Adds a point with specified coordinates and updates the list.
     * @param name The name of the point.
     * @param latitude The latitude of the point.
     * @param longitude The longitude of the point.
     * @param latitudeDirection The direction of the latitude (NORTH or SOUTH).
     * @param longitudeDirection The direction of the longitude (EAST or WEST).
     */
    fun addPointByCoordinates(
        name: String,
        latitude: Double,
        longitude: Double,
        latitudeDirection: LatitudeDirection,
        longitudeDirection: LongitudeDirection
    ) {
        repository.addPointByCoordinates(name, latitude, longitude, latitudeDirection, longitudeDirection)
        update()
    }

    /**
     * Sets an error message that can be observed by the UI.
     * @param message The error message to display.
     */
    private fun setError(message: String) {
        _errorMessage.postValue(message)
    }

    /**
     * Clears any existing error messages.
     */
    fun clearError() {
        _errorMessage.postValue(null)
    }
}

