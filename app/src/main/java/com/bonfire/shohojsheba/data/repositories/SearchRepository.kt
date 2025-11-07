package com.bonfire.shohojsheba.data.repositories

import com.bonfire.shohojsheba.data.database.dao.ServiceDao
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.mappers.toEntity
import com.bonfire.shohojsheba.data.remote.FirestoreApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchRepository(
    private val serviceDao: ServiceDao,
    private val appScope: CoroutineScope
) {
    fun searchServicesSmart(query: String): Flow<List<Service>> {
        val local = serviceDao.searchServices(query)
        return local.onEach { hits ->
            if (hits.isEmpty()) {
                appScope.launch(Dispatchers.IO) {
                    val remote = FirestoreApi.searchServicesRemote(query).map { it.toEntity() }
                    if (remote.isNotEmpty()) serviceDao.insertServices(remote)
                    // else: plug Gemini here if you want a fallback
                }
            }
        }
    }
}