package com.bonfire.shohojsheba.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase // <-- This import was missing
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
    version = 7, // Incremented version to 7
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao
    abstract fun userDataDao(): UserDataDao
    abstract fun metadataDao(): MetadataDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shohoj_sheba_database"
                )
                .fallbackToDestructiveMigration() // WARNING: This deletes data if you change the database schema. Good for dev, bad for prod.
                .addCallback(object : Callback() {
                    // This callback runs when the database is opened.
                    // We use it to trigger a background worker (CatalogRefreshWorker) to update data from the cloud.
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