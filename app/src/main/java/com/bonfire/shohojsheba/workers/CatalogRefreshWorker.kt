package com.bonfire.shohojsheba.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bonfire.shohojsheba.data.database.AppDatabase
import com.bonfire.shohojsheba.data.repositories.Repository

class CatalogRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        val db = AppDatabase.getDatabase(applicationContext)
        val repo = Repository(applicationContext, db.serviceDao(), db.userDataDao(), db.metadataDao())
        repo.refreshIfNeeded()
        Result.success()
    } catch (t: Throwable) {
        Result.retry()
    }
}
