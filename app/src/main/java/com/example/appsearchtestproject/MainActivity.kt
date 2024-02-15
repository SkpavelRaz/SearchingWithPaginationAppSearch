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
    private lateinit var button: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.float_add_button)

        button.setOnClickListener {
            startActivity(Intent(this, InsertDataForSearch::class.java))
        }

    }

}