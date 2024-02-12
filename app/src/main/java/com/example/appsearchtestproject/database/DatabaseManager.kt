package com.example.appsearchtestproject.database

import android.content.Context
import androidx.appsearch.app.AppSearchSession
import androidx.appsearch.app.PutDocumentsRequest
import androidx.appsearch.app.SearchSpec
import androidx.appsearch.localstorage.LocalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.appsearch.app.SetSchemaRequest


class DatabaseManager(
    private val context: Context
) {
    private var session: AppSearchSession? = null

    suspend fun init() {
        withContext(Dispatchers.IO) {
            val sessionFuture = LocalStorage.createSearchSessionAsync(
                LocalStorage.SearchContext.Builder(
                    context, "Note"
                ).build()
            )
            val setSchemaRequest = SetSchemaRequest.Builder().addDocumentClasses(Note::class.java)
                .build()

            session = sessionFuture.get()

            session?.setSchemaAsync(setSchemaRequest)
        }
    }

    suspend fun putTodos(notes: List<Note>): Boolean {
        return withContext(Dispatchers.IO) {
            session?.putAsync(
                PutDocumentsRequest.Builder()
                    .addDocuments(notes).build()
            )?.get()?.isSuccess == true
        }
    }

    suspend fun searchNotes(query: String): List<Note> {
        return withContext(Dispatchers.IO) {
            val searchSpec = SearchSpec.Builder()
                .setSnippetCount(10)
                .addFilterNamespaces("My Notes")
                .setRankingStrategy(SearchSpec.RANKING_STRATEGY_USAGE_COUNT)
                .build()
            val result = session?.search(
                query,
                searchSpec
            ) ?: return@withContext emptyList()

            val page = result.nextPageAsync.get()

            page.mapNotNull {
                if (it.genericDocument.schemaType == Note::class.java.simpleName) {
                    it.getDocument(Note::class.java)
                } else null
            }
        }
    }

    fun closeSession(){
        session?.close()
        session= null
    }

}