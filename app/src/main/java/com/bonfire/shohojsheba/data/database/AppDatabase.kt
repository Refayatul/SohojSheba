package com.bonfire.shohojsheba.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bonfire.shohojsheba.data.database.dao.MetadataDao
import com.bonfire.shohojsheba.data.database.dao.ServiceDao
import com.bonfire.shohojsheba.data.database.dao.UserDataDao
import com.bonfire.shohojsheba.data.database.entities.*
import com.bonfire.shohojsheba.workers.CatalogRefreshWorker

@Database(
    entities = [
        Service::class, ServiceDetail::class,
        UserFavorite::class, UserHistory::class,
        Metadata::class
    ],
    version = 5, // Incremented version to 5
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao
    abstract fun userDataDao(): UserDataDao
    abstract fun metadataDao(): MetadataDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // Destructive migration is enabled below, so specific migration paths are not needed for now

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shohoj_sheba_database"
                )
                .fallbackToDestructiveMigration() // This will solve any migration errors during development
                .addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        WorkManager.getInstance(context).enqueueUniqueWork(
                            "catalog_refresh_once",
                            ExistingWorkPolicy.KEEP,
                            OneTimeWorkRequestBuilder<CatalogRefreshWorker>().build()
                        )
                    }
                })
                .build().also { INSTANCE = it }
            }
    }
}