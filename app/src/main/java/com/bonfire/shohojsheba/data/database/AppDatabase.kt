package com.bonfire.shohojsheba.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.data.database.dao.ServiceDao
import com.bonfire.shohojsheba.data.database.dao.UserDataDao
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.database.entities.ServiceDetail
import com.bonfire.shohojsheba.data.database.entities.UserFavorite
import com.bonfire.shohojsheba.data.database.entities.UserHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Service::class, ServiceDetail::class, UserFavorite::class, UserHistory::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun serviceDao(): ServiceDao
    abstract fun userDataDao(): UserDataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shohoj_sheba_database"
                )
                    .addCallback(AppDatabaseCallback(context))
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

private class AppDatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            prePopulateDatabase(context, AppDatabase.getDatabase(context).serviceDao())
        }
    }
}

// Sample migration from version 1 to 2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example of adding a new column to the services table
        database.execSQL("ALTER TABLE services ADD COLUMN newColumn TEXT")

        // Drop and recreate static tables
        database.execSQL("DROP TABLE IF EXISTS services")
        database.execSQL("DROP TABLE IF EXISTS service_details")

        // Recreate tables with new schema
        // (Assuming Service and ServiceDetail tables have been updated in their respective entity classes)
        // This is a simplified example. A real migration would need to recreate the tables
        // with the new schema.
        // You would typically call the same create table statements that Room generates.
    }
}


suspend fun prePopulateDatabase(context: Context, serviceDao: ServiceDao) {
    val services = listOf(
        Service("nid", R.string.service_nid_title, R.string.service_nid_subtitle, R.drawable.ic_nid, "citizen", 1),
        Service("passport", R.string.service_passport_title, R.string.service_passport_subtitle, R.drawable.ic_passport, "citizen", 1),
        Service("birth_cert", R.string.service_birth_cert_title, R.string.service_birth_cert_subtitle, R.drawable.ic_birth_cert, "citizen", 1),
        Service("driving_license", R.string.service_driving_license_title, R.string.service_driving_license_subtitle, R.drawable.ic_driving_license, "citizen", 1),
        Service("land_reg", R.string.service_land_reg_title, R.string.service_land_reg_subtitle, R.drawable.ic_land_reg, "citizen", 1),
        Service("marriage_reg", R.string.service_marriage_reg_title, R.string.service_marriage_reg_subtitle, R.drawable.ic_marriage_reg, "citizen", 1),
        Service("voter_transfer", R.string.service_voter_transfer_title, R.string.service_voter_transfer_subtitle, R.drawable.ic_voter_transfer, "citizen", 1)
    )

    val serviceDetails = listOf(
        ServiceDetail(
            serviceId = "nid",
            instructions = "NID instructions...",
            imageRes = R.drawable.ic_launcher_background,
            youtubeLink = "https://www.youtube.com/watch?v=your_video_id",
            requiredDocuments = "- Birth Certificate\n- SSC Certificate",
            processingTime = "15-30 days",
            contactInfo = "Contact your local election office",
            lastUpdated = 1
        )
        // Add more service details here...
    )

    serviceDao.insertServices(services)
    serviceDao.insertServiceDetails(serviceDetails)
}
