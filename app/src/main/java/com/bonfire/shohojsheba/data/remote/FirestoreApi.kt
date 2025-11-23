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
}
