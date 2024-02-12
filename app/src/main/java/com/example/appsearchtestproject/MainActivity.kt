package com.example.appsearchtestproject

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appsearch.app.AppSearchBatchResult
import androidx.appsearch.app.GenericDocument
import androidx.appsearch.app.PutDocumentsRequest
import androidx.appsearch.app.SearchResult
import androidx.appsearch.app.SearchResults
import androidx.appsearch.app.SearchSpec
import androidx.appsearch.app.SetSchemaRequest
import androidx.appsearch.exceptions.AppSearchException
import androidx.lifecycle.lifecycleScope
import com.example.appsearchtestproject.database.Note
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.gson.Gson
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.P)
class MainActivity : AppCompatActivity() {
    private val TAG = "AppSearchDemoActivity"
    private lateinit var appSearchObserver: AppSearchObserver
    private lateinit var button: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.float_add_button)

        button.setOnClickListener {
            startActivity(Intent(this, InsertDataForSearch::class.java))
        }
        appSearchObserver = AppSearchObserver(applicationContext)
        lifecycle.addObserver(appSearchObserver)
        lifecycleScope.launchWhenResumed {
            setSchema()
            addDocument()
            search()
            persist()
        }

    }


    @SuppressLint("CheckResult")
    private fun setSchema() {
        val setSchemaRequest =
            SetSchemaRequest.Builder().addDocumentClasses(Note::class.java)
                .build()
        Futures.transformAsync(
            appSearchObserver.sessionFuture,
            { session ->
                session?.setSchemaAsync(setSchemaRequest)
            }, mainExecutor
        )
    }

    private fun addDocument() {
        val doc = (1..100).map {
            Note(
                namespace = packageName,
                id = UUID.randomUUID().toString(),
                text = "Hello, this doc was created $it"
            )
        }
        val putRequest = PutDocumentsRequest.Builder().addDocuments(doc).build()
        val putFuture = Futures.transformAsync(
            appSearchObserver.sessionFuture,
            { session ->
                session?.putAsync(putRequest)
            }, mainExecutor
        )
        Futures.addCallback(
            putFuture,
            object : FutureCallback<AppSearchBatchResult<String, Void>?> {
                override fun onSuccess(result: AppSearchBatchResult<String, Void>?) {
                    Log.d(TAG, "successfulResults = ${Gson().toJson(result?.successes)}")
                    Log.d(TAG, "failedResults = ${result?.failures}")

                }

                override fun onFailure(t: Throwable) {
//                    Log.d(TAG,"Failed to put document(s).")
                    Log.e(TAG, "Failed to put document(s).", t)
                }
            },
            mainExecutor
        )
    }

    private fun search() {
        val searchSpec = SearchSpec.Builder()
            .addFilterNamespaces(packageName)
            .setResultCountPerPage(100)
            .build()
        val searchFuture = Futures.transform(
            appSearchObserver.sessionFuture,
            { session ->
                session?.search("hello 50", searchSpec)
            }, mainExecutor
        )
        Futures.addCallback(
            searchFuture,
            object : FutureCallback<SearchResults> {
                override fun onSuccess(searchResults: SearchResults?) {
                    searchResults?.let {
                        iterateSearchResults(searchResults)
                    }
                }

                override fun onFailure(t: Throwable) {
                    Log.e("TAG", "Failed to search in AppSearch.", t)
                }
            },
            mainExecutor
        )
    }

    @SuppressLint("CheckResult")
    private fun iterateSearchResults(searchResults: SearchResults) {
        Futures.transform(
            searchResults.nextPageAsync,
            { page: List<SearchResult>? ->
                page?.forEach { current ->
                    val genericDocument: GenericDocument = current.genericDocument
                    val schemaType = genericDocument.schemaType
                    val document: Note? = try {
                        if (schemaType == "Note") {
                            genericDocument.toDocumentClass(Note::class.java)
                        } else null
                    } catch (e: AppSearchException) {
                        Log.e(
                            TAG,
                            "Failed to convert GenericDocument to MyDocument",
                            e
                        )
                        null
                    }
                    Log.d(TAG, "Found ${document?.text}")
                }

            }, mainExecutor
        )
    }

    private fun persist() {
        val requestFlushFuture = Futures.transformAsync(
            appSearchObserver.sessionFuture,
            { session -> session?.requestFlushAsync() }, mainExecutor
        )
        Futures.addCallback(requestFlushFuture, object : FutureCallback<Void?> {
            override fun onSuccess(result: Void?) {
                // Success! Database updates have been persisted to disk.
                Log.d(TAG, "onSuccess: ${Gson().toJson(result)}")
            }

            override fun onFailure(t: Throwable) {
                Log.e(TAG, "Failed to flush database updates.", t)
            }
        }, mainExecutor)
    }
}