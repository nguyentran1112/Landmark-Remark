package tcnguyen.app.landmarkremark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import tcnguyen.app.landmarkremark.data.model.LocationNote
import tcnguyen.app.landmarkremark.data.repository.LocationNoteRepository
import tcnguyen.app.landmarkremark.ui.navigations.LandmarkRemarkDestinations

class RootViewModel() : ViewModel() {
    private val repository = LocationNoteRepository()
    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(LandmarkRemarkHomeUIState(loading = true))
    val uiState: StateFlow<LandmarkRemarkHomeUIState> = _uiState

    private val _currentLocationNote = MutableStateFlow(LocationNote())
    val currentLocationNote: StateFlow<LocationNote> = _currentLocationNote

    private val _locationNotes = MutableStateFlow<MutableList<LocationNote>>(mutableListOf())
    val locationNotes: StateFlow<List<LocationNote>> = _locationNotes

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    val locationNotesQuery = searchText
        .debounce(500L)
        .onEach { _isSearching.update { true } }
        .combine(_locationNotes) {
            text, locationNotes ->
            if (text.isNullOrBlank()) {
                locationNotes
            } else {
                locationNotes.filter {
                    it.matchSearchQuery(text)
                }
            }
        }
        .onEach { _isSearching.update { false } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _locationNotes.value
        )
    fun setCurrentLocation(value: LocationNote) {
        _currentLocationNote.value = value
    }
    fun loadLocationNotes() {
        repository.loadLocationNotes(_locationNotes)
    }
    fun addALocation(value: LocationNote) {
        _locationNotes.update {
            _locationNotes.value.toMutableList().apply { this.add(value) }
        }
    }
    fun addLocationNote(value: LocationNote) {
        repository.addLocationNode(value)
    }

    fun switchTab(value: String) {
        _uiState.value = LandmarkRemarkHomeUIState(tab = value)
    }

    fun onSearchTextChange(value: String) {
        _searchText.value = value
    }
}

data class LandmarkRemarkHomeUIState(
    val loading: Boolean = false,
    val error: String? = null,
    val tab: String = LandmarkRemarkDestinations.HOME
)
