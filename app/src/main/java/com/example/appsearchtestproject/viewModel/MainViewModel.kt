package com.example.appsearchtestproject.viewModel

import androidx.lifecycle.ViewModel
import com.example.appsearchtestproject.database.DatabaseManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KProperty

class MainViewModel(
    private val noteSearchManager:DatabaseManager
):ViewModel() {
    init {


    }
}
