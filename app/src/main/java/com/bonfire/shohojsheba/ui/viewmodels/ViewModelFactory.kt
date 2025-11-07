package com.bonfire.shohojsheba.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
// Make sure to import the NEW repository
import com.bonfire.shohojsheba.data.repositories.FirebaseRepository

class ServicesViewModelFactory(
    private val repository: FirebaseRepository // <-- THIS IS THE FIX
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServicesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // The factory now passes the correct repository type to the ViewModel
            return ServicesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ServiceDetailViewModelFactory(
    private val serviceId: String,
    private val repository: FirebaseRepository // <-- THIS IS THE FIX
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServiceDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ServiceDetailViewModel(serviceId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class UserDataViewModelFactory(
    private val repository: FirebaseRepository // <-- THIS IS THE FIX
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserDataViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
