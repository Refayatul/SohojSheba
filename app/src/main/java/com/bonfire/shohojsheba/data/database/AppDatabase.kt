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
                    .fallbackToDestructiveMigration() // Added for development, should be removed for production
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
        // Example migration logic. For now, we just clear and re-create.
        database.execSQL("DROP TABLE IF EXISTS services")
        database.execSQL("DROP TABLE IF EXISTS service_details")
        // onCreate will be called again to pre-populate
    }
}


suspend fun prePopulateDatabase(context: Context, serviceDao: ServiceDao) {
    val services = listOf(
        // Citizen Services
        Service("citizen_apply_nid", R.string.service_citizen_apply_nid_title, R.string.service_citizen_apply_nid_subtitle, R.drawable.ic_citizen_apply_nid, "citizen", 1),
        Service("citizen_renew_passport", R.string.service_citizen_renew_passport_title, R.string.service_citizen_renew_passport_subtitle, R.drawable.ic_citizen_renew_passport, "citizen", 1),
        Service("citizen_file_gd", R.string.service_citizen_file_gd_title, R.string.service_citizen_file_gd_subtitle, R.drawable.ic_citizen_file_gd, "citizen", 1),
        Service("citizen_driving_license", R.string.service_citizen_driving_license_title, R.string.service_citizen_driving_license_subtitle, R.drawable.ic_citizen_driving_license, "citizen", 1),
        Service("citizen_register_birth", R.string.service_citizen_register_birth_title, R.string.service_citizen_register_birth_subtitle, R.drawable.ic_citizen_register_birth, "citizen", 1),
        Service("citizen_old_age_allowance", R.string.service_citizen_old_age_allowance_title, R.string.service_citizen_old_age_allowance_subtitle, R.drawable.ic_citizen_old_age_allowance, "citizen", 1),
        Service("citizen_check_voter_status", R.string.service_citizen_check_voter_status_title, R.string.service_citizen_check_voter_status_subtitle, R.drawable.ic_citizen_check_voter_status, "citizen", 1),
        Service("citizen_police_clearance", R.string.service_citizen_police_clearance_title, R.string.service_citizen_police_clearance_subtitle, R.drawable.ic_citizen_police_clearance, "citizen", 1),
        Service("citizen_new_electricity", R.string.service_citizen_new_electricity_title, R.string.service_citizen_new_electricity_subtitle, R.drawable.ic_citizen_new_electricity, "citizen", 1),
        Service("citizen_traffic_fines", R.string.service_citizen_traffic_fines_title, R.string.service_citizen_traffic_fines_subtitle, R.drawable.ic_citizen_traffic_fines, "citizen", 1),
        Service("citizen_register_marriage", R.string.service_citizen_register_marriage_title, R.string.service_citizen_register_marriage_subtitle, R.drawable.ic_citizen_register_marriage, "citizen", 1),

        // Farmer Services
        Service("farmer_soil_advice", R.string.service_farmer_soil_advice_title, R.string.service_farmer_soil_advice_subtitle, R.drawable.ic_farmer_soil_advice, "farmer", 1),
        Service("farmer_agri_loan", R.string.service_farmer_agri_loan_title, R.string.service_farmer_agri_loan_subtitle, R.drawable.ic_farmer_agri_loan, "farmer", 1),
        Service("farmer_crop_disease", R.string.service_farmer_crop_disease_title, R.string.service_farmer_crop_disease_subtitle, R.drawable.ic_farmer_crop_disease, "farmer", 1),
        Service("farmer_market_prices", R.string.service_farmer_market_prices_title, R.string.service_farmer_market_prices_subtitle, R.drawable.ic_farmer_market_prices, "farmer", 1),
        Service("farmer_id_card", R.string.service_farmer_id_card_title, R.string.service_farmer_id_card_subtitle, R.drawable.ic_farmer_id_card, "farmer", 1),
        Service("farmer_certified_seeds", R.string.service_farmer_certified_seeds_title, R.string.service_farmer_certified_seeds_subtitle, R.drawable.ic_farmer_certified_seeds, "farmer", 1),
        Service("farmer_irrigation_guide", R.string.service_farmer_irrigation_guide_title, R.string.service_farmer_irrigation_guide_subtitle, R.drawable.ic_farmer_irrigation_guide, "farmer", 1),
        Service("farmer_machinery_subsidy", R.string.service_farmer_machinery_subsidy_title, R.string.service_farmer_machinery_subsidy_subtitle, R.drawable.ic_farmer_machinery_subsidy, "farmer", 1),
        Service("farmer_rice_farming", R.string.service_farmer_rice_farming_title, R.string.service_farmer_rice_farming_subtitle, R.drawable.ic_farmer_rice_farming, "farmer", 1),
        Service("farmer_crop_insurance", R.string.service_farmer_crop_insurance_title, R.string.service_farmer_crop_insurance_subtitle, R.drawable.ic_farmer_crop_insurance, "farmer", 1),
        Service("farmer_poultry_farming", R.string.service_farmer_poultry_farming_title, R.string.service_farmer_poultry_farming_subtitle, R.drawable.ic_farmer_poultry_farming, "farmer", 1),

        // Entrepreneur Services
        Service("entrepreneur_trade_license", R.string.service_entrepreneur_trade_license_title, R.string.service_entrepreneur_trade_license_subtitle, R.drawable.ic_entrepreneur_trade_license, "entrepreneur", 1),
        Service("entrepreneur_ebin_register", R.string.service_entrepreneur_ebin_register_title, R.string.service_entrepreneur_ebin_register_subtitle, R.drawable.ic_entrepreneur_ebin_register, "entrepreneur", 1),
        Service("entrepreneur_register_company", R.string.service_entrepreneur_register_company_title, R.string.service_entrepreneur_register_company_subtitle, R.drawable.ic_entrepreneur_register_company, "entrepreneur", 1),
        Service("entrepreneur_irc_apply", R.string.service_entrepreneur_irc_apply_title, R.string.service_entrepreneur_irc_apply_subtitle, R.drawable.ic_entrepreneur_irc_apply, "entrepreneur", 1),
        Service("entrepreneur_erc_apply", R.string.service_entrepreneur_erc_apply_title, R.string.service_entrepreneur_erc_apply_subtitle, R.drawable.ic_entrepreneur_erc_apply, "entrepreneur", 1),
        Service("entrepreneur_vat_return", R.string.service_entrepreneur_vat_return_title, R.string.service_entrepreneur_vat_return_subtitle, R.drawable.ic_entrepreneur_vat_return, "entrepreneur", 1),
        Service("entrepreneur_fire_license", R.string.service_entrepreneur_fire_license_title, R.string.service_entrepreneur_fire_license_subtitle, R.drawable.ic_entrepreneur_fire_license, "entrepreneur", 1),
        Service("entrepreneur_tin_cert", R.string.service_entrepreneur_tin_cert_title, R.string.service_entrepreneur_tin_cert_subtitle, R.drawable.ic_entrepreneur_tin_cert, "entrepreneur", 1),
        Service("entrepreneur_trademark_reg", R.string.service_entrepreneur_trademark_reg_title, R.string.service_entrepreneur_trademark_reg_subtitle, R.drawable.ic_entrepreneur_trademark_reg, "entrepreneur", 1),
        Service("entrepreneur_egp_bid", R.string.service_entrepreneur_egp_bid_title, R.string.service_entrepreneur_egp_bid_subtitle, R.drawable.ic_entrepreneur_egp_bid, "entrepreneur", 1),
        Service("entrepreneur_sme_loan", R.string.service_entrepreneur_sme_loan_title, R.string.service_entrepreneur_sme_loan_subtitle, R.drawable.ic_entrepreneur_sme_loan, "entrepreneur", 1),

        // Government Office Services
        Service("govt_egp_use", R.string.service_govt_egp_use_title, R.string.service_govt_egp_use_subtitle, R.drawable.ic_govt_egp_use, "govt_office", 1),
        Service("govt_brta_services", R.string.service_govt_brta_services_title, R.string.service_govt_brta_services_subtitle, R.drawable.ic_govt_brta_services, "govt_office", 1),
        Service("govt_board_results", R.string.service_govt_board_results_title, R.string.service_govt_board_results_subtitle, R.drawable.ic_govt_board_results, "govt_office", 1),
        Service("govt_nid_server", R.string.service_govt_nid_server_title, R.string.service_govt_nid_server_subtitle, R.drawable.ic_govt_nid_server, "govt_office", 1),
        Service("govt_health_services", R.string.service_govt_health_services_title, R.string.service_govt_health_services_subtitle, R.drawable.ic_govt_health_services, "govt_office", 1),
        Service("govt_local_forms", R.string.service_govt_local_forms_title, R.string.service_govt_local_forms_subtitle, R.drawable.ic_govt_local_forms, "govt_office", 1),
        Service("govt_btrc_report", R.string.service_govt_btrc_report_title, R.string.service_govt_btrc_report_subtitle, R.drawable.ic_govt_btrc_report, "govt_office", 1),
        Service("govt_env_clearance", R.string.service_govt_env_clearance_title, R.string.service_govt_env_clearance_subtitle, R.drawable.ic_govt_env_clearance, "govt_office", 1),
        Service("govt_mygov_track", R.string.service_govt_mygov_track_title, R.string.service_govt_mygov_track_subtitle, R.drawable.ic_govt_mygov_track, "govt_office", 1),
        Service("govt_power_outage", R.string.service_govt_power_outage_title, R.string.service_govt_power_outage_subtitle, R.drawable.ic_govt_power_outage, "govt_office", 1),
        Service("govt_nbr_portal", R.string.service_govt_nbr_portal_title, R.string.service_govt_nbr_portal_subtitle, R.drawable.ic_govt_nbr_portal, "govt_office", 1)
    )

    val serviceDetails = services.map { service ->
        ServiceDetail(
            serviceId = service.id,
            instructions = "Dummy instructions for ${context.getString(service.titleRes)}...",
            imageRes = R.drawable.ic_launcher_background, // Placeholder image
            youtubeLink = "https://www.youtube.com/watch?v=dQw4w9WgXcQ", // Placeholder link
            requiredDocuments = "- Dummy Document 1\n- Dummy Document 2",
            processingTime = "5-10 business days",
            contactInfo = "Contact the relevant government office.",
            lastUpdated = 1
        )
    }

    serviceDao.insertServices(services)
    serviceDao.insertServiceDetails(serviceDetails)
}
