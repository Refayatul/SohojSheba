package com.bonfire.shohojsheba.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.database.entities.ServiceDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<Service>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceDetails(serviceDetails: List<ServiceDetail>)

    @Query("SELECT * FROM services")
    fun getAllServices(): Flow<List<Service>>

    @Query("SELECT * FROM services WHERE category = :category")
    fun getServicesByCategory(category: String): Flow<List<Service>>

    @Query("SELECT * FROM services WHERE id = :serviceId")
    fun getServiceById(serviceId: String): Flow<Service?>

    @Query("SELECT * FROM service_details WHERE serviceId = :serviceId")
    fun getServiceDetail(serviceId: String): Flow<ServiceDetail?>

    @Query("SELECT * FROM service_details WHERE serviceId = :serviceId")
    suspend fun getServiceDetailOnce(serviceId: String): ServiceDetail?

    @Query("SELECT COUNT(*) FROM services")
    suspend fun getServiceCount(): Int

    @Query("""
    SELECT * FROM services
    WHERE title LIKE '%' || :q || '%' OR subtitle LIKE '%' || :q || '%'
    """)
    fun searchServices(q: String): Flow<List<Service>>

    @Query("SELECT * FROM services WHERE id IN (:serviceIds)")
    fun getServicesByIds(serviceIds: List<String>): Flow<List<Service>>
}
