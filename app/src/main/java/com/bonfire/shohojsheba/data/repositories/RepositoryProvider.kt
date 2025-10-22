package com.bonfire.shohojsheba.data.repositories

import android.content.Context
import com.bonfire.shohojsheba.data.database.AppDatabase

object RepositoryProvider {

    @Volatile
    private var repository: Repository? = null

    fun getRepository(context: Context): Repository {
        return repository ?: synchronized(this) {
            val database = AppDatabase.getDatabase(context)
            val instance = Repository(database.serviceDao(), database.userDataDao())
            repository = instance
            instance
        }
    }
}
