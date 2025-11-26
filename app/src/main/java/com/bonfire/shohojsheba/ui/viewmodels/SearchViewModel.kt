package com.bonfire.shohojsheba.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.data.repositories.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class SearchViewModel(private val searchRepository: SearchRepository) : ViewModel() {

    // --- State Management ---
    // Holds the current search text typed by the user.
    // 'MutableStateFlow' is a reactive state holder that emits updates to collectors.
    private val query = MutableStateFlow("")

    // --- Reactive Search Stream ---
    // 'flatMapLatest' is a powerful operator for search scenarios:
    // 1. It observes changes to the 'query' flow.
    // 2. Whenever the query changes, it executes the block { q -> ... }.
    // 3. IMPORTANT: If the user types a new character before the previous search completes,
    //    it automatically CANCELS the previous search and starts a new one with the latest query.
    //    This prevents "race conditions" (where old results overwrite new ones) and saves resources.
    val searchResults = query.flatMapLatest { q ->
        // Delegate the actual search logic to the repository
        searchRepository.searchServicesSmart(q)
    }

    // --- User Interaction ---
    // Called from the UI (SearchBar) when the user types text
    fun onSearchQueryChanged(newQuery: String) {
        query.value = newQuery // Updating this triggers the flatMapLatest flow above
    }
}