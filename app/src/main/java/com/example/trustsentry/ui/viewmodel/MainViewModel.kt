package com.example.trustsentry.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trustsentry.crypto.CryptoEngine
import com.example.trustsentry.data.AppDatabase
import com.example.trustsentry.data.VerificationEntity
import com.example.trustsentry.data.VerificationRepository
import com.example.trustsentry.model.ArtifactScenario
import com.example.trustsentry.model.VerificationResult
import com.example.trustsentry.model.VerificationStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VerificationRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = VerificationRepository(db.verificationDao())
    }

    // Navigation state
    private val _currentRoute = MutableStateFlow("home")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    // Active Verification Result
    private val _activeResult = MutableStateFlow<VerificationResult?>(null)
    val activeResult: StateFlow<VerificationResult?> = _activeResult.asStateFlow()

    // History filter
    private val _historyFilter = MutableStateFlow("All")
    val historyFilter: StateFlow<String> = _historyFilter.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Security & Preferences
    val biometricsEnabled = MutableStateFlow(true)
    val offlineCacheEnabled = MutableStateFlow(true)
    val selectedEnvironment = MutableStateFlow("dev") // dev, staging, prod

    // Room DB streams
    val allVerifications: StateFlow<List<VerificationEntity>> = repository.getAllVerifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pinnedVerifications: StateFlow<List<VerificationEntity>> = repository.getPinnedVerifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined filtered verifications
    val filteredVerifications: StateFlow<List<VerificationEntity>> = combine(
        allVerifications,
        _historyFilter,
        _searchQuery
    ) { list, filter, query ->
        list.filter { entity ->
            val matchesFilter = when (filter) {
                "Trusted" -> entity.status == VerificationStatus.TRUSTED.name
                "Review" -> entity.status == VerificationStatus.REVIEW.name
                "Invalid" -> entity.status == VerificationStatus.INVALID.name || entity.status == VerificationStatus.SUSPICIOUS.name
                else -> true
            }

            val matchesQuery = if (query.isBlank()) true else {
                entity.artifactId.contains(query, ignoreCase = true) ||
                entity.issuer.contains(query, ignoreCase = true) ||
                entity.artifactType.contains(query, ignoreCase = true)
            }

            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(route: String) {
        _currentRoute.value = route
    }

    fun setHistoryFilter(filter: String) {
        _historyFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun verifyScannedPayload(payload: String) {
        viewModelScope.launch {
            val result = CryptoEngine.verifyPayload(payload)
            _activeResult.value = result
            repository.saveVerification(result)
        }
    }

    fun verifyDocument(filename: String, bytes: ByteArray, scenarioHint: String? = null) {
        viewModelScope.launch {
            val result = CryptoEngine.verifyDocumentBytes(filename, bytes, scenarioHint)
            _activeResult.value = result
            repository.saveVerification(result)
        }
    }

    fun loadScenario(scenario: ArtifactScenario) {
        verifyScannedPayload(scenario.samplePayload)
    }

    fun clearActiveResult() {
        _activeResult.value = null
    }

    fun togglePin(entity: VerificationEntity) {
        viewModelScope.launch {
            repository.togglePinned(entity.id, entity.isPinned)
        }
    }

    fun deleteVerification(entity: VerificationEntity) {
        viewModelScope.launch {
            repository.delete(entity)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
