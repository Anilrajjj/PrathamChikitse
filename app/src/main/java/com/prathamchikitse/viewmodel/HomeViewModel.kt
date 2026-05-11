package com.prathamchikitse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prathamchikitse.domain.model.Emergency
import com.prathamchikitse.domain.model.EmergencyCategory
import com.prathamchikitse.domain.repository.EmergencyRepository
import com.prathamchikitse.domain.usecase.GetUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HomeUiState(
    val emergencies: List<Emergency> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedCategory: EmergencyCategory? = null,
    val currentLanguage: String = "en",
    val error: String? = null,
    val recentlyViewed: List<Emergency> = emptyList(),
    val recentSearches: List<String> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val emergencyRepository: EmergencyRepository,
    getUserPreferences: GetUserPreferencesUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedCategory = MutableStateFlow<EmergencyCategory?>(null)

    private val _language = MutableStateFlow("en")

    private val _recentlyViewed = MutableStateFlow<List<Emergency>>(emptyList())
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())

    init {
        getUserPreferences().onEach { prefs ->
            _language.value = prefs.languageCode
        }.launchIn(viewModelScope)
    }

    private val emergenciesFlow = combine(
        _query.debounce(300),
        _selectedCategory
    ) { queryText, category ->
        queryText to category
    }.flatMapLatest { (queryText, category) ->
        val allFlow = if (queryText.isBlank()) {
            emergencyRepository.getAllEmergencies()
        } else {
            emergencyRepository.searchEmergencies(queryText.trim())
        }

        allFlow.map { list ->
            val filtered = if (category != null) {
                list.filter { it.category == category }
            } else {
                list
            }
            filtered
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(500),
        initialValue = emptyList<Emergency>()
    )

    val uiState: StateFlow<HomeUiState> = combine(
        emergenciesFlow,
        _query,
        _selectedCategory,
        _language
    ) { emergencies, query, category, language ->
        HomeUiState(
            emergencies = emergencies,
            isLoading = false,
            searchQuery = query,
            selectedCategory = category,
            currentLanguage = language,
            error = null,
            recentlyViewed = _recentlyViewed.value,
            recentSearches = _recentSearches.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(500),
        initialValue = HomeUiState()
    )

    fun onSearchQueryChanged(q: String) {
        _query.update { q }
        if (q.isNotBlank() && q.length >= 2) {
            addRecentSearch(q.trim())
        }
    }

    fun onCategorySelected(category: EmergencyCategory?) {
        _selectedCategory.update { if (it == category) null else category }
    }

    fun onEmergencyViewed(emergency: Emergency) {
        _recentlyViewed.update { current ->
            val updated = listOf(emergency) + current.filter { it.id != emergency.id }
            updated.take(5)
        }
    }

    fun clearRecentSearches() {
        _recentSearches.update { emptyList() }
    }

    private fun addRecentSearch(searchTerm: String) {
        _recentSearches.update { current ->
            val updated = listOf(searchTerm) + current.filter { it != searchTerm }
            updated.take(10)
        }
    }
}
