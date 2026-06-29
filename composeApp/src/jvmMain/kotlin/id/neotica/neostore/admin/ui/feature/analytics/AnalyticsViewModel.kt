package id.neotica.neostore.admin.ui.feature.analytics

import id.neotica.neostore.admin.domain.remote.AnalyticsRepository
import id.neotica.neostore.admin.utils.Constants.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class AnalyticsViewModel(
    private val analyticsRepository: AnalyticsRepository,
    private val httpClient: HttpClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        verifyAndLoad()
    }

    fun refresh() {
        verifyAndLoad()
    }

    fun selectDate(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
        fetchEventsForDate(date, page = 1)
    }

    fun backToDashboard() {
        _uiState.update {
            it.copy(selectedDate = null, events = emptyList(), isLoadingEvents = false, errorEvents = null)
        }
    }

    fun loadNextPage() {
        val s = _uiState.value
        val date = s.selectedDate ?: return
        if (s.isLoadingEvents || s.eventsPage >= s.eventsTotalPages) return
        fetchEventsForDate(date, page = s.eventsPage + 1, append = true)
    }

    private fun fetchEventsForDate(date: String, page: Int, append: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingEvents = true, errorEvents = null) }

            val result = analyticsRepository.getEvents(
                from = date, to = date, page = page, limit = 20
            )

            result.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isLoadingEvents = false,
                        events = if (append) it.events + response.data else response.data,
                        eventsPage = response.page,
                        eventsTotalPages = response.total_pages
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoadingEvents = false, errorEvents = error.message ?: "Failed to load events.")
                }
            }
        }
    }

    private fun verifyAndLoad() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val usernameResult = kotlin.runCatching {
                httpClient.get("$BASE_URL/auth/user/username").body<String>()
            }

            val username = usernameResult.getOrNull()

            if (username != "laetuz") {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        accessGranted = false,
                        errorMessage = "Access denied. User '$username' is not authorized."
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(accessGranted = true) }

            val countsResult = analyticsRepository.getCounts()
            val trendingResult = analyticsRepository.getTrending(limit = 10)

            val counts = countsResult.getOrNull()
            val trending = trendingResult.getOrNull()
            val error = countsResult.exceptionOrNull()?.message
                ?: trendingResult.exceptionOrNull()?.message

            if (error != null && counts == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        counts = counts,
                        trending = trending ?: emptyList()
                    )
                }
            }
        }
    }
}
