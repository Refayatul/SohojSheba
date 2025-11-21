package com.bonfire.shohojsheba.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bonfire.shohojsheba.LocaleSetupApplication
import com.bonfire.shohojsheba.data.repositories.AuthRepositoryImpl
import com.bonfire.shohojsheba.data.repositories.RepositoryProvider
import com.bonfire.shohojsheba.data.repositories.SearchRepository
import kotlinx.coroutines.CoroutineScope

class ViewModelFactory(
    private val context: Context,
    private val serviceId: String? = null,
    private val appScope: CoroutineScope? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = RepositoryProvider.getRepository(context.applicationContext)
        val application = context.applicationContext as LocaleSetupApplication

        return when {
            modelClass.isAssignableFrom(ServicesViewModel::class.java) -> {
                ServicesViewModel(repository, application.networkStatusTracker) as T
            }
            modelClass.isAssignableFrom(ServiceDetailViewModel::class.java) -> {
                requireNotNull(serviceId) { "serviceId must be provided for ServiceDetailViewModel" }
                ServiceDetailViewModel(repository, serviceId) as T
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                requireNotNull(appScope) { "appScope must be provided for SearchViewModel" }
                val searchRepository = SearchRepository(repository.serviceDao, appScope)
                SearchViewModel(searchRepository) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(AuthRepositoryImpl.getInstance()) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
