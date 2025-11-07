package com.bonfire.shohojsheba.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.data.database.entities.UserFavorite
import com.bonfire.shohojsheba.data.repositories.Repository
import kotlinx.coroutines.launch

class ServiceDetailViewModel(private val repository: Repository, private val serviceId: String) : ViewModel() {

    val service = repository.getServiceById(serviceId)
    val serviceDetail = repository.getServiceDetail(serviceId)
    val isFavorite = repository.isFavorite(serviceId)

    init {
        viewModelScope.launch {
            repository.ensureDetail(serviceId)
        }
    }

    fun addFavorite(favorite: UserFavorite) {
        viewModelScope.launch {
            repository.addFavorite(favorite)
        }
    }

    fun removeFavorite(serviceId: String) {
        viewModelScope.launch {
            repository.removeFavorite(serviceId)
        }
    }
}