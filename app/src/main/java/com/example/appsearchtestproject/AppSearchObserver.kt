package com.example.appsearchtestproject

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appsearch.app.AppSearchSession
import androidx.appsearch.localstorage.LocalStorage
import androidx.appsearch.platformstorage.PlatformStorage
import androidx.core.os.BuildCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

private const val TAG = "AppSearchDemoActivity"
private const val DATABASE_NAME = "appsearchdemo"

class AppSearchObserver(private val context: Context) : LifecycleObserver {

    lateinit var sessionFuture: ListenableFuture<AppSearchSession>

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun setupAppSearch() {
        sessionFuture = if (Build.VERSION.SDK_INT >= 31) {
            PlatformStorage.createSearchSessionAsync(
                PlatformStorage.SearchContext.Builder(context, DATABASE_NAME)
                    .build()
            )
        } else {
            LocalStorage.createSearchSessionAsync(
                LocalStorage.SearchContext.Builder(context, DATABASE_NAME)
                    .build()
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("CheckResult")
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun teardownAppSearch() {
        /* val closeFuture = */ Futures.transform<AppSearchSession, Unit>(
            sessionFuture,
            { session ->
                session?.close()
                Unit
            }, context.mainExecutor
        )
    }
}
