package com.bonfire.shohojsheba.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.data.database.dao.ServiceDao
import com.bonfire.shohojsheba.data.database.dao.UserDataDao
import com.bonfire.shohojsheba.data.database.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Service::class, ServiceDetail::class, UserFavorite::class, UserHistory::class],
    version = 2, // Incremented version
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
                    .fallbackToDestructiveMigration() // Recreate DB if version changes
                    .addCallback(object : Callback() {

                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    prePopulateDatabase(context, database.serviceDao())
                                }
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    prePopulateDatabase(context, database.serviceDao())
                                }
                            }
                        }

                    })
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}

// Pre-populate services and service details
suspend fun prePopulateDatabase(context: Context, serviceDao: ServiceDao) {
    Log.d("AppDatabase", "prePopulateDatabase called")

    val count = serviceDao.getServiceCount()
    if (count > 0) {
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
    )

    val serviceDetails = services.map { service ->
        val (images, instructions) = when (service.id) {
            "citizen_apply_nid" -> {
                // Multiple images and their corresponding step instructions
                "nid_registration_1,nid_registration_2" to
                        "Step 1: Fill out the NID application form completely.\n\n" +
                        "Step 2: Upload your photo and supporting documents as required."
            }
            "citizen_renew_passport" -> {
                "img_step_placeholder_1,img_step_placeholder_2" to
                        "Step 1: Complete the passport renewal form.\n\n" +
                        "Step 2: Submit required documents and pay the applicable fees."
            }
            "citizen_file_gd" -> {
                "img_step_placeholder_1,img_step_placeholder_2" to
                        "Step 1: Visit your nearest police station.\n\n" +
                        "Step 2: File the GD with accurate details and get a receipt."
            }
            else -> {
                "img_step_placeholder_1,img_step_placeholder_2" to
                        "Step 1: Follow the instructions provided.\n\n" +
                        "Step 2: Complete the process as per guidelines."
            }
        }

        ServiceDetail(
            serviceId = service.id,
            instructions = instructions,
            imageRes = images,
            youtubeLink = null,
            requiredDocuments = "- Document 1\n- Document 2",
            processingTime = "5-10 business days",
            contactInfo = "Contact the relevant office.",
            lastUpdated = 1
        ).also { Log.d("AppDatabase", "Service ID: ${service.id}, images = $images, instructions = $instructions") }
    }

    serviceDao.insertServices(services)
    serviceDao.insertServiceDetails(serviceDetails)

    Log.d("AppDatabase", "Database pre-population completed")
}
