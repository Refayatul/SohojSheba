package com.bonfire.shohojsheba.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.data.repositories.Repository
import kotlinx.coroutines.launch

class ServiceListViewModel(private val repository: Repository) : ViewModel() {

    val allServices = repository.getAllServices()
    val history = repository.getRecentHistory()
    val favorites = repository.getFavorites()

    init {
        viewModelScope.launch {
            repository.refreshIfNeeded()
        }
    }
}