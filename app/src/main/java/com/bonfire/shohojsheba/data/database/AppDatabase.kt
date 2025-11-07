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
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao
    abstract fun userDataDao(): UserDataDao
    abstract fun metadataDao(): MetadataDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS service_details")
                db.execSQL("DROP TABLE IF EXISTS services")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS services(
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        subtitle TEXT NOT NULL,
                        iconName TEXT NOT NULL,
                        category TEXT NOT NULL,
                        versionAdded INTEGER NOT NULL,
                        lastUpdated INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS service_details(
                        serviceId TEXT NOT NULL PRIMARY KEY,
                        instructions TEXT NOT NULL,
                        imageNames TEXT NOT NULL,
                        youtubeLink TEXT,
                        requiredDocuments TEXT NOT NULL,
                        processingTime TEXT NOT NULL,
                        contactInfo TEXT NOT NULL,
                        lastUpdated INTEGER NOT NULL,
                        FOREIGN KEY(serviceId) REFERENCES services(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS metadata(
                        key TEXT NOT NULL PRIMARY KEY,
                        value TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shohoj_sheba_database"
                )
                .addMigrations(MIGRATION_2_3)
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