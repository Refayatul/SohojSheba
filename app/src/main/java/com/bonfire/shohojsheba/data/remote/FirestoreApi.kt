package com.bonfire.shohojsheba.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ServiceDTO(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val iconName: String = "",
    val category: String = "",
    val versionAdded: Int = 1,
    val lastUpdated: Long = 0L,
    val images: List<String> = emptyList(),
    val imageNames: List<String> = emptyList(),
    val searchKeywords: List<String> = emptyList()
)

data class ServiceDetailDTO(
    val serviceId: String = "",
    val instructions: String = "",
    val imageNames: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val youtubeLink: String? = null,
    val requiredDocuments: String = "",
    val processingTime: String = "",
    val contactInfo: String = "",
    val lastUpdated: Long = 0L
)

object FirestoreApi {
    private val fs by lazy { FirebaseFirestore.getInstance() }

    suspend fun catalogVersion(): Int =
        fs.collection("config").document("catalog")
            .get().await().getLong("version")?.toInt() ?: 0

    suspend fun allServices(): List<ServiceDTO> =
        fs.collection("services").get().await().documents.mapNotNull { it.toObject(ServiceDTO::class.java) }

    suspend fun allDetails(): List<ServiceDetailDTO> =
        fs.collection("service_details").get().await().documents.mapNotNull { it.toObject(ServiceDetailDTO::class.java) }

    suspend fun detailById(serviceId: String): ServiceDetailDTO? =
        fs.collection("service_details").document(serviceId).get().await().toObject(ServiceDetailDTO::class.java)

    // Remote search (array-contains-any on searchKeywords)
    suspend fun searchServicesRemote(query: String): List<ServiceDTO> {
        val tokens = query.lowercase().split(Regex("[^\\p{L}\\p{N}]+")).filter { it.length >= 2 }.take(10)
        if (tokens.isEmpty()) return emptyList()
        val snap = fs.collection("services").whereArrayContainsAny("searchKeywords", tokens).get().await()
        return snap.documents.mapNotNull { it.toObject(ServiceDTO::class.java) }
    }

    // Callback-based fetching as requested
    fun fetchServices(onComplete: (List<ServiceDTO>) -> Unit) {
        fs.collection("services")
            .get()
            .addOnSuccessListener { result ->
                val services = result.map { it.toObject(ServiceDTO::class.java) }
                onComplete(services)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onComplete(emptyList())
            }
    }

    fun fetchServiceDetails(onComplete: (List<ServiceDetailDTO>) -> Unit) {
        fs.collection("service_details")
            .get()
            .addOnSuccessListener { result ->
                val details = result.map { it.toObject(ServiceDetailDTO::class.java) }
                onComplete(details)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onComplete(emptyList())
            }
    }
}