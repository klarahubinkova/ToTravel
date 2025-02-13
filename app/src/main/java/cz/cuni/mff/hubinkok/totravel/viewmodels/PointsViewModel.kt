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

class PointsViewModel : ViewModel() {
    private val _pointsList = MutableLiveData<List<Point>>(emptyList())
    val pointsList: LiveData<List<Point>> get() = _pointsList

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val repository = PointsRepository()

    fun getPointById(id: Int): Point? {
        return repository.getPointById(id)
    }

    fun deletePointById(id: Int) {
        val result = repository.deletePointById(id)
        if (result) {
            _pointsList.postValue(repository.getPoints())
        } else {
            setError("Point with ID $id does not exist")
        }
    }

    suspend fun loadPoints(context: Context) {
        repository.loadPoints(context)
        _pointsList.postValue(repository.getPoints())
    }

    suspend fun savePoints(context: Context) {
        repository.saveToFile(context)
    }

    suspend fun deletePoints(context: Context) {
        repository.deletePoints(context)
        _pointsList.postValue(emptyList())
    }

    suspend fun addPointByName(name: String) {
        withContext(Dispatchers.IO) {
            val result = repository.addPointByName(name)
            if (result) {
                _pointsList.postValue(repository.getPoints())
            } else {
                setError("Place not found")
            }
        }
    }

    fun addPointByCoordinates(
        name: String,
        latitude: Double,
        longitude: Double,
        latitudeDirection: LatitudeDirection,
        longitudeDirection: LongitudeDirection
    ) {
        repository.addPointByCoordinates(name, latitude, longitude, latitudeDirection, longitudeDirection)
        _pointsList.postValue(repository.getPoints())
    }

    private fun setError(message: String) {
        _errorMessage.postValue(message)
    }

    fun clearError() {
        _errorMessage.postValue(null)
    }
}

