package com.example.sensingapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensingapp.OrientationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)

    // Expose allOrientationData as LiveData
    private val _allOrientationData = MutableLiveData<List<OrientationData>>()
    val allOrientationData: LiveData<List<OrientationData>> = _allOrientationData

    init {
        // Load initial data when ViewModel is initialized
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = database.orientationDataDao().getAllData()
            _allOrientationData.postValue(data)
        }
    }

    fun insertOrientationData(orientationData: OrientationData) {
        viewModelScope.launch(Dispatchers.IO) {
            database.orientationDataDao().insert(orientationData)
            // Reload data after insertion
            loadData()
        }
    }
}
