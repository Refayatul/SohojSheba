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
    version = 2,
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
                    .fallbackToDestructiveMigration()
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
        val instructions = when (service.id) {
            "citizen_apply_nid" -> """
                Step 1: Fill out the NID application form completely.
                
                Step 2: Upload your photo and supporting documents as required.
                
                Step 3: Dummy Step
                
            """.trimIndent()

            "citizen_renew_passport" -> """
                Step 1 — Search on Google
                ● Type: passport bd
                ● Click the first result: “E-Passport Online Registration Portal”.

                Step 2 — Homepage, click “APPLY ONLINE”
                ● Click the green APPLY ONLINE button on the left.

                Step 3 — Region/Office Availability
                ● Question: “Are you applying from Bangladesh?” → Yes.
                ● Select your district (example: Tangail).
                ● Select your nearest police station (example: Ghatail).
                ● Note the Regional Passport Office shown.
                ● Click Continue.

                Step 4 — Enter Email
                ● Enter your email address.
                ● Check “I am human” (reCAPTCHA).
                ● Click Continue.

                Step 5 — Email Verification Screen
                ● Confirm email is correct.
                ● Click Continue.

                Step 6 — Account Information
                ● Enter Password (min 6 characters: uppercase, lowercase, numbers).
                ● Enter Full Name (as per NID/BRC).
                ● Optional: Given Name.
                ● Surname: last/family name.

                Step 7 — Mobile Number
                ● Select country code: +880.
                ● Enter mobile number.
                ● Check “I am human”.

                Step 8 — Create Account
                ● Click Create Account button.
                ● Note: Max 6 applications per account.

                Step 9 — Account Activation
                ● Click the activation link sent to your email.
                ● Resend if not received.

                Step 10 — Sign In
                ● Enter Email and Password.
                ● Check “I am human”.
                ● Use Forgot Password? if needed.

                Step 11 — Sign In Confirm
                ● Click Sign In.

                Step 12 — Dashboard
                ● Click “Apply for a new e-Passport”.
                ● Optional: Review profile under Account.

                Step 13 — Passport Type
                ● Ordinary Passport for normal users.
                ● Official Passport only for government officials.
                ● Click Save and Continue.

                Step 14 — Personal Information
                ● Select “I apply for myself”.
                ● Select gender.
                ● Enter Full Name / Given Name / Surname exactly.
                ● Ensure spelling matches previous documents.

                Step 15 — Profession
                ● Select profession (PRIVATE SERVICE / STUDENT / BUSINESS etc.).
                ● Enter correct information for verification.

                Step 16 — Religion & Country Code
                ● Select religion.
                ● Country code +880.
                ● Verify mobile number and other fields.

                Step 17 — Birth Information Part 1
                ● Select country of birth.
                ● Verify personal info matches documents.

                Step 18 — Birth Information Part 2 & Citizenship
                ● Select district of birth.
                ● Select date of birth (match NID/passport).
                ● Citizenship type: By Birth.
                ● Click Save and Continue.

                Step 19 — Permanent Address
                ● Select district, City/Village/House, Road/Block/Sector (optional), Post office, Postal code, Police station.
                ● If changed, police verification may be required.

                Step 20 — Present Address
                ● Check “Same as Permanent” if applicable.
                ● Otherwise, fill manually.
                ● Regional Passport Office will appear.

                Step 21 — Regional Passport Office (RPO)
                ● Select appropriate RPO.
                ● Click Save and Continue.

                Step 22 — National ID Verification
                ● Enter NID number.
                ● Verify in NID system.
                ● Indicate if you have previous passports (MRP/ePP/None).

                Step 23 — Reissue Reason & Old Passport Info
                ● Select reason: EXPIRED, PAGES EXHAUSTED, DAMAGED, etc.
                ● Enter previous passport number.
                ● Enter issue and expiry dates.

                Step 24 — Other-country Passport (if any)
                ● Indicate yes/no for other country passports.
                ● Provide info if yes.

                Step 25 — Parental Information
                ● Father: Name, Profession, Nationality, NID (optional).
                ● Mother: Name, Profession, Nationality, NID (optional).
                ● Legal Guardian info only if applicable.

                Step 26 — Spouse Information
                ● Select marital status.
                ● Enter spouse’s Name, Profession, Nationality, NID (optional).

                Step 27 — Emergency Contact
                ● Select relationship.
                ● Enter Name.
                ● Fill Address fields: Country, District, City/Village/House, Road/Block/Sector (optional), Post office, Postal code, Police station.
                ● Click Save and Continue.

                Step 28 — Passport Options
                ● Select number of pages (48/64).
                ● Select validity (5/10 years).
                ● Review price.
                ● Click Save and Continue.

                Step 29 — Delivery Options & Appointment
                ● Choose Regular / Express / Super Express delivery.
                ● Price updates automatically.
                ● Click Save and Continue.

                Step 30 — Declaration & Payment
                ● Check declaration box.
                ● Click Confirm and proceed to payment.
                ● Pay via EkPay/Card/bKash/Nagad/Rocket/Upay.
                ● Complete OTP/PIN verification.
                ● After payment, select appointment date/time.
                ● Download summary/receipt.

                Step 31 — Confirm Submission
                ● Review status (Submitted).
                ● Download application form for printing.

                Step 32 — Review All Information
                ● Double-check all entries.
                ● Ensure names/dates match official documents.

                Step 33 — Passport Photos
                ● Ensure photos match e-passport requirements.

                Step 34 — Supporting Documents
                ● Upload all required documents.
                ● Verify file format and size.

                Step 35 — Payment Confirmation
                ● Confirm successful payment.
                ● Retain transaction ID.

                Step 36 — Appointment Confirmation
                ● Confirm selected date/time for passport collection.

                Step 37 — Track Application
                ● Use portal to track status.

                Step 38 — Regional Passport Office Visit
                ● Visit RPO as per appointment.

                Step 39 — Verification
                ● Officials will verify all submitted info.

                Step 40 — Biometric Capture
                ● Fingerprints and photo will be taken.

                Step 41 — Final Submission at RPO
                ● Officials confirm all details.
                ● Sign where necessary.

                Step 42 — Receive Acknowledgment
                ● Obtain receipt of application submission.

                Step 43 — Passport Processing
                ● Wait for processing (portal will update status).

                Step 44 — Payment Status Check
                ● Verify transaction completed on portal.

                Step 45 — Passport Printed
                ● Portal will notify when passport is printed.

                Step 46 — Passport Delivery
                ● Passport will be delivered as per chosen method.

                Step 47 — Confirmation of Receipt
                ● Confirm receipt of passport.
                ● Sign acknowledgment.

                Step 48 — End of Process
                ● Keep all documents for future reference.
                ● Process complete.
            """.trimIndent()

            "citizen_file_gd" -> """
                Step 1: Visit your nearest police station.
                Step 2: File the GD with accurate details and get a receipt.
            """.trimIndent()

            else -> "Step 1: Follow the instructions provided.\nStep 2: Complete the process as per guidelines."
        }

        ServiceDetail(
            serviceId = service.id,
            instructions = instructions,
            imageRes = "", // images will be loaded dynamically
            youtubeLink = null,
            requiredDocuments = "- Document 1\n- Document 2",
            processingTime = "5-10 business days",
            contactInfo = "Contact the relevant office.",
            lastUpdated = 1
        )
    }

    serviceDao.insertServices(services)
    serviceDao.insertServiceDetails(serviceDetails)

    Log.d("AppDatabase", "Database pre-population completed")
}
