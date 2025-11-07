package com.bonfire.shohojsheba.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.data.repositories.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class SearchViewModel(private val searchRepository: SearchRepository) : ViewModel() {

    private val query = MutableStateFlow("")

    val searchResults = query.flatMapLatest { q ->
        searchRepository.searchServicesSmart(q)
    }

    fun onSearchQueryChanged(newQuery: String) {
        query.value = newQuery
    }
}