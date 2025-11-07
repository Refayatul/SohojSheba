package com.bonfire.shohojsheba.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await



import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.database.entities.ServiceDetail
import com.bonfire.shohojsheba.data.database.entities.UserFavorite
import com.bonfire.shohojsheba.data.database.entities.UserHistory

class FirebaseRepository {

    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // --- Service Functions (Replaces ServiceDao) ---

    suspend fun getAllServices(): Result<List<Service>> {
        return try {
            val services = db.collection("services")
                .get()
                .await()
                .toObjects(Service::class.java)
            Result.success(services)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServicesByCategory(category: String): Result<List<Service>> {
        return try {
            val services = db.collection("services")
                .whereEqualTo("category", category)
                .get()
                .await()
                .toObjects(Service::class.java)
            Result.success(services)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServiceById(serviceId: String): Result<Service?> {
        return try {
            val service = db.collection("services").document(serviceId)
                .get()
                .await()
                .toObject(Service::class.java)
            Result.success(service)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServicesByIds(serviceIds: List<String>): Result<List<Service>> {
        if (serviceIds.isEmpty()) return Result.success(emptyList())
        return try {
            val services = db.collection("services")
                .whereIn("id", serviceIds)
                .get()
                .await()
                .toObjects(Service::class.java)
            Result.success(services)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServiceDetail(serviceId: String): Result<ServiceDetail?> {
        return try {
            val detail = db.collection("service_details").document(serviceId)
                .get()
                .await()
                .toObject(ServiceDetail::class.java)
            Result.success(detail)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- User Data Functions (Replaces UserDataDao) ---

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun addFavorite(serviceId: String): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return try {
            val favorite = UserFavorite(serviceId, System.currentTimeMillis())
            db.collection("users").document(userId)
                .collection("favorites").document(serviceId)
                .set(favorite).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFavorite(serviceId: String): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return try {
            db.collection("users").document(userId)
                .collection("favorites").document(serviceId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isFavorite(serviceId: String): Result<Boolean> {
        val userId = getCurrentUserId() ?: return Result.success(false)
        return try {
            val isFav = db.collection("users").document(userId)
                .collection("favorites").document(serviceId)
                .get().await().exists()
            Result.success(isFav)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavorites(): Result<List<UserFavorite>> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return try {
            val favorites = db.collection("users").document(userId)
                .collection("favorites")
                .orderBy("addedDate")
                .get().await().toObjects(UserFavorite::class.java)
            Result.success(favorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addHistory(serviceId: String): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return try {
            val historyItem = UserHistory(serviceId, System.currentTimeMillis())
            db.collection("users").document(userId)
                .collection("history").add(historyItem).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentHistory(limit: Int): Result<List<UserHistory>> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return try {
            val history = db.collection("users").document(userId)
                .collection("history")
                .orderBy("accessedDate")
                .limit(limit.toLong())
                .get().await().toObjects(UserHistory::class.java)
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}