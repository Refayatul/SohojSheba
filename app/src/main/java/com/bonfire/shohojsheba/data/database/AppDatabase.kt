package com.bonfire.shohojsheba.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.data.database.dao.ServiceDao
import com.bonfire.shohojsheba.data.database.dao.UserDataDao
import com.bonfire.shohojsheba.data.database.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Service::class, ServiceDetail::class, UserFavorite::class, UserHistory::class],
    version = 2, // Increment version when schema changes
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
                    .fallbackToDestructiveMigration(true) // drop all tables if version changes
                    .build()

                INSTANCE = instance

                // Always pre-populate the DB after creation
                CoroutineScope(Dispatchers.IO).launch {
                    prePopulateDatabase(context, instance.serviceDao())
                }

                instance
            }
        }
    }
}

// Pre-populate DB in coroutine
suspend fun prePopulateDatabase(context: Context, serviceDao: ServiceDao) {
    Log.d("AppDatabase", "prePopulateDatabase called")

    // Check if DB is already populated
    if (serviceDao.getServiceCount() > 0) {
        Log.d("AppDatabase", "Database already populated, skipping pre-population")
        return
    }

    val services = listOf(
        Service(
            "citizen_apply_nid",
            R.string.service_citizen_apply_nid_title,
            R.string.service_citizen_apply_nid_subtitle,
            R.drawable.ic_citizen_apply_nid,
            "citizen",
            1
        ),
        Service(
            "citizen_renew_passport",
            R.string.service_citizen_renew_passport_title,
            R.string.service_citizen_renew_passport_subtitle,
            R.drawable.ic_citizen_renew_passport,
            "citizen",
            1
        ),
        Service(
            "citizen_file_gd",
            R.string.service_citizen_file_gd_title,
            R.string.service_citizen_file_gd_subtitle,
            R.drawable.ic_citizen_file_gd,
            "citizen",
            1
        )
        // Add other services here
    )

    val serviceDetails = services.map { service ->
        val images = when (service.id) {
            "citizen_apply_nid" -> "nid_registration_1" // Only NID image
            "citizen_renew_passport" -> "img_step_placeholder_1,img_step_placeholder_2"
            "citizen_file_gd" -> "img_step_placeholder_1,img_step_placeholder_2"
            else -> "img_step_placeholder_1,img_step_placeholder_2"
        }

        ServiceDetail(
            serviceId = service.id,
            instructions = "Instructions for ${context.getString(service.titleRes)}",
            imageRes = images,
            youtubeLink = null,
            requiredDocuments = "- Document 1\n- Document 2",
            processingTime = "5-10 business days",
            contactInfo = "Contact the relevant office.",
            lastUpdated = 1
        ).also {
            Log.d("AppDatabase", "Service ID: ${service.id}, images = $images")
        }
    }

    // Insert services and details in DB
    serviceDao.insertServices(services)
    serviceDao.insertServiceDetails(serviceDetails)

    Log.d("AppDatabase", "Database pre-population completed")
}
