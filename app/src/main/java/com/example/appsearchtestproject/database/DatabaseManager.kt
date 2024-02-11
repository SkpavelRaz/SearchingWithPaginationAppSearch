package com.example.appsearchtestproject.database

import android.content.Context
import androidx.appsearch.app.AppSearchSession
import androidx.appsearch.localstorage.LocalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.appsearch.app.SetSchemaRequest


class DatabaseManager(
    private val context: Context
) {
    private var session: AppSearchSession?=null

    suspend fun init(){
        withContext(Dispatchers.IO){
            val sessionFuture=LocalStorage.createSearchSessionAsync(
                LocalStorage.SearchContext.Builder(
                    context,"Note"
                ).build()
            )
            val setSchemaRequest = SetSchemaRequest.Builder().addDocumentClasses(Note::class.java)
                .build()

            session=sessionFuture.get()

            session?.setSchemaAsync(setSchemaRequest)
        }
    }


}