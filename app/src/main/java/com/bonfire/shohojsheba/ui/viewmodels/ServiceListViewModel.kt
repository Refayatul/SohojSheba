package com.bonfire.shohojsheba.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.data.repositories.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServiceListViewModel(private val repository: Repository) : ViewModel() {

    val allServices = repository.getAllServices()
    val history = repository.getRecentHistory()
    val favorites = repository.getFavorites()

    private val _dataSource = MutableStateFlow("Cache")
    val dataSource = _dataSource.asStateFlow()

    init {
        viewModelScope.launch {
            _dataSource.value = repository.refreshIfNeeded()
        }
    }
}