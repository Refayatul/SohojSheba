package com.bonfire.shohojsheba.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bonfire.shohojsheba.data.repositories.Repository
import com.bonfire.shohojsheba.data.repositories.RepositoryProvider

class ServicesViewModelFactory(private val repository: Repository, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServicesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ServicesViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ServiceDetailViewModelFactory(
    private val serviceId: String,
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServiceDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ServiceDetailViewModel(serviceId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class UserDataViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserDataViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
