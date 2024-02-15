package com.example.appsearchtestproject

import android.content.Context
import android.os.Build
import androidx.appsearch.app.AppSearchBatchResult
import androidx.appsearch.app.AppSearchSession
import androidx.appsearch.app.PutDocumentsRequest
import androidx.appsearch.app.RemoveByDocumentIdRequest
import androidx.appsearch.app.SearchResult
import androidx.appsearch.app.SearchSpec
import androidx.appsearch.app.SetSchemaRequest
import androidx.appsearch.localstorage.LocalStorage
import androidx.appsearch.platformstorage.PlatformStorage
import androidx.concurrent.futures.await
import com.example.appsearchtestproject.database.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppSearchManager(context: Context,coroutineScope: CoroutineScope) {
    private val isInitialized: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private lateinit var appSearchSession: AppSearchSession

    init {
        coroutineScope.launch {
            appSearchSession = if (Build.VERSION.SDK_INT >= 31) {
                PlatformStorage.createSearchSessionAsync(
                    PlatformStorage.SearchContext.Builder(context, DATABASE_NAME).build()
                ).await()
            } else {
                LocalStorage.createSearchSessionAsync(
                    LocalStorage.SearchContext.Builder(context, DATABASE_NAME).build()
                ).await()
            }
        }

        try {
            val setSchemaRequest =
                SetSchemaRequest.Builder().addDocumentClasses(Note::class.java).build()

            appSearchSession.setSchemaAsync(setSchemaRequest)
        } finally {
            appSearchSession.close()
        }
    }

    /**
     * Adds a [Note] document to the AppSearch database.
     */

    suspend fun addNote(note: Note): AppSearchBatchResult<String, Void> {
        awaitInitialization()
        val request = PutDocumentsRequest.Builder().addDocuments(note).build()
        return appSearchSession.putAsync(request).await()
    }

    /**
     * Queries the AppSearch database for matching [Note] documents.
     *
     * @return a list of [SearchResult] objects. This returns SearchResults in the order
     * they were created (with most recent first). This returns a maximum of 10
     * SearchResults that match the query, per AppSearch default page size.
     * Snippets are returned for the first 10 results.
     */

    suspend fun queryLatestNotes(query:String):List<SearchResult>{
        awaitInitialization()
        val searchSpec=SearchSpec.Builder()
            .setRankingStrategy(SearchSpec.RANKING_STRATEGY_CREATION_TIMESTAMP)
            .setSnippetCount(10).build()

        val searchResult=appSearchSession.search(query,searchSpec)
        return searchResult.nextPageAsync.await()
    }

    /**
     * Removes [Note] document from the AppSearch database by namespace and
     * id.
     */

    suspend fun removeNote(nameSpace:String,id:String):AppSearchBatchResult<String,Void>{
        awaitInitialization()
        val request=RemoveByDocumentIdRequest.Builder(nameSpace).addIds(id).build()
        return appSearchSession.removeAsync(request).await()
    }

    private suspend fun awaitInitialization() {
        if (!isInitialized.value){
            isInitialized.first(){it}
        }
    }

    companion object {
        private const val DATABASE_NAME = "notesDatabase"
    }
}

