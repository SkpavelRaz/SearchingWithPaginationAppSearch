package com.example.appsearchtestproject.viewModel

import android.app.Application
import androidx.appsearch.app.SearchResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsearchtestproject.AppSearchManager
import com.example.appsearchtestproject.database.DatabaseManager
import com.example.appsearchtestproject.database.Note
import kotlinx.coroutines.launch
import java.util.UUID

class NoteViewModel(application: Application):AndroidViewModel(application) {

    private val _errorMessageLiveData =MutableLiveData<String?>()
    val errorMessageLiveData:LiveData<String?> = _errorMessageLiveData

    private val _noteLiveData: MutableLiveData<List<SearchResult>> = MutableLiveData(mutableListOf())
    private val noteLiveData:LiveData<List<SearchResult>> = _noteLiveData

    private val noteAppSearchManager:AppSearchManager=
        AppSearchManager(getApplication(),viewModelScope)

    /** add text to create note document*/
    fun addNote(text:String){
        val id=UUID.randomUUID().toString()
        val note=Note(namespace = "user", id = id, text=text)

        viewModelScope.launch {
            val result=noteAppSearchManager.addNote(note)
            if (!result.isSuccess){
                _errorMessageLiveData.postValue("Faild to add note with id:$id and text: $text")
            }

            queryNotes()
        }
    }

    /** remove [Note] document from the AppSearch database*/
    fun removeNote(namespace:String,id:String){
        viewModelScope.launch {
            val result=noteAppSearchManager.removeNote(namespace,id)
            if (!result.isSuccess){
                _errorMessageLiveData.postValue("Faild to add note with id:$id and namespace: $namespace")
            }
            queryNotes()
        }
    }



    private fun queryNotes() {

    }

}
