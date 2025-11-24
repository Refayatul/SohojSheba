package com.bonfire.shohojsheba.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// For the "config" object
data class Config(
    val catalog: CatalogInfo = CatalogInfo()
)

data class CatalogInfo(
    val version: Int = 0,
    val updatedAt: Long = 0L
)

// For the objects inside the "services" array (summary)
data class ServiceSummary(
    val id: String = "",
    val title: LocalizedString = LocalizedString(),
    val subtitle: LocalizedString = LocalizedString(),
    val iconName: String = "",
    val category: String = "",
    val versionAdded: Int = 0,
    val lastUpdated: Long = 0L,
    val searchKeywords: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val imageNames: List<String> = emptyList()
)

// For the objects inside the "service_details" array
data class ServiceDetails(
    val serviceId: String = "",
    val instructions: LocalizedString = LocalizedString(),
    val requiredDocuments: LocalizedString = LocalizedString(),
    val processingTime: LocalizedString = LocalizedString(),
    val contactInfo: LocalizedString = LocalizedString(),
    val youtubeLink: String? = null, // Nullable as it can be null
    val lastUpdated: Long = 0L,
    val imageNames: List<String> = emptyList(),
    val images: List<String> = emptyList()
)


object FirestoreApi {
    private val fs by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { com.google.firebase.auth.FirebaseAuth.getInstance() }

    suspend fun catalogVersion(): Int =
        fs.collection("config").document("catalog")
            .get().await().toObject(CatalogInfo::class.java)?.version ?: 0

    suspend fun allServices(): List<ServiceSummary> =
        fs.collection("services").get().await().documents.mapNotNull { it.toObject(ServiceSummary::class.java) }

    suspend fun allDetails(): List<ServiceDetails> =
        fs.collection("service_details").get().await().documents.mapNotNull { it.toObject(ServiceDetails::class.java) }

    suspend fun detailById(serviceId: String): ServiceDetails? =
        fs.collection("service_details").document(serviceId).get().await().toObject(ServiceDetails::class.java)

    // Remote search (array-contains-any on searchKeywords)
    suspend fun searchServicesRemote(query: String): List<ServiceSummary> {
        val tokens = query.lowercase().split(Regex("[^\\p{L}\\p{N}]+")).filter { it.length >= 2 }.take(10)
        if (tokens.isEmpty()) return emptyList()
        val snap = fs.collection("services").whereArrayContainsAny("searchKeywords", tokens).get().await()
        return snap.documents.mapNotNull { it.toObject(ServiceSummary::class.java) }
    }

    suspend fun saveService(service: ServiceSummary) {
        fs.collection("services").document(service.id).set(service).await()
    }

    suspend fun saveServiceDetails(details: ServiceDetails) {
        fs.collection("service_details").document(details.serviceId).set(details).await()
    }
    
    // User data sync methods
    suspend fun syncFavoriteToFirestore(serviceId: String) {
        try {
            val uid = auth.currentUser?.uid ?: return
            fs.collection("users").document(uid)
                .update("favoriteServiceIds", com.google.firebase.firestore.FieldValue.arrayUnion(serviceId))
                .await()
        } catch (e: Exception) {
            // If document doesn't exist, create it
            try {
                val uid = auth.currentUser?.uid ?: return
                fs.collection("users").document(uid)
                    .set(mapOf("favoriteServiceIds" to listOf(serviceId)), com.google.firebase.firestore.SetOptions.merge())
                    .await()
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }
    
    suspend fun removeFavoriteFromFirestore(serviceId: String) {
        try {
            val uid = auth.currentUser?.uid ?: return
            fs.collection("users").document(uid)
                .update("favoriteServiceIds", com.google.firebase.firestore.FieldValue.arrayRemove(serviceId))
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun syncHistoryToFirestore(serviceId: String) {
        try {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                android.util.Log.w("FirestoreApi", "syncHistoryToFirestore: User not authenticated")
                return
            }
            
            android.util.Log.d("FirestoreApi", "syncHistoryToFirestore: Syncing serviceId=$serviceId for user=$uid")
            
            // Store history as an array field, just like favorites
            fs.collection("users").document(uid)
                .update("historyServiceIds", com.google.firebase.firestore.FieldValue.arrayUnion(serviceId))
                .await()
                
            android.util.Log.d("FirestoreApi", "syncHistoryToFirestore: Successfully synced serviceId=$serviceId")
        } catch (e: Exception) {
            // If document doesn't exist or field doesn't exist, create it
            try {
                val uid = auth.currentUser?.uid ?: return
                fs.collection("users").document(uid)
                    .set(mapOf("historyServiceIds" to listOf(serviceId)), com.google.firebase.firestore.SetOptions.merge())
                    .await()
                android.util.Log.d("FirestoreApi", "syncHistoryToFirestore: Created history field and synced serviceId=$serviceId")
            } catch (e2: Exception) {
                android.util.Log.e("FirestoreApi", "syncHistoryToFirestore: Failed to sync history", e2)
                e2.printStackTrace()
            }
        }
    }
    
    // Fetch user data from Firestore (for syncing on app start)
    suspend fun getUserFavoritesFromFirestore(): List<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return emptyList()
            val snapshot = fs.collection("users").document(uid).get().await()
            snapshot.get("favoriteServiceIds") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreApi", "Failed to fetch favorites", e)
            emptyList()
        }
    }
    
    suspend fun getUserHistoryFromFirestore(): List<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return emptyList()
            val snapshot = fs.collection("users").document(uid).get().await()
            snapshot.get("historyServiceIds") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreApi", "Failed to fetch history", e)
            emptyList()
        }
    }
    suspend fun clearAllFavoritesFromFirestore() {
        try {
            val uid = auth.currentUser?.uid ?: return
            fs.collection("users").document(uid)
                .update("favoriteServiceIds", emptyList<String>())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun clearAllHistoryFromFirestore() {
        try {
            val uid = auth.currentUser?.uid ?: return
            fs.collection("users").document(uid)
                .update("historyServiceIds", emptyList<String>())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

