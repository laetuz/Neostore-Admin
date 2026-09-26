package id.neotica.neostore.admin.ui.feature.contributions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.neostore.admin.domain.remote.ContributorsRepository
import id.neotica.neostore.admin.platform.PlatformFile
import id.neotica.neostore.admin.platform.exportIconToDownloads
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContributionDetailViewModel(
    private val repo: ContributorsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContributionDetailUiState())
    val uiState = _uiState.asStateFlow()

    private var contributionId: String = ""

    fun load(id: String) {
        contributionId = id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "") }
            repo.getContribution(id)
                .onSuccess { item -> _uiState.update { it.copy(isLoading = false, contribution = item) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, statusMessage = "Failed to load: ${error.message}") }
                }
        }
    }

    fun addScreenshots(files: List<PlatformFile>) {
        if (files.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingScreenshots = true, statusMessage = "Uploading screenshots...") }
            repo.addContributionScreenshots(contributionId, files)
                .onSuccess {
                    _uiState.update { it.copy(isUploadingScreenshots = false, statusMessage = "Screenshots added.") }
                    load(contributionId)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isUploadingScreenshots = false, statusMessage = "Failed: ${error.message}") }
                }
        }
    }

    fun removeScreenshot(index: Int) {
        viewModelScope.launch {
            repo.removeContributionScreenshot(contributionId, index)
                .onSuccess {
                    _uiState.update { it.copy(statusMessage = "Screenshot removed.") }
                    load(contributionId)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(statusMessage = "Failed: ${error.message}") }
                }
        }
    }

    fun approve() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Approving...") }
            repo.approveContribution(contributionId)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, statusMessage = "Approved and published.") }
                    load(contributionId)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, statusMessage = "Failed: ${error.message}") }
                }
        }
    }

    fun requestDecline() = _uiState.update { it.copy(showDeclineDialog = true) }
    fun cancelDecline() = _uiState.update { it.copy(showDeclineDialog = false, declineReason = "") }
    fun setDeclineReason(reason: String) = _uiState.update { it.copy(declineReason = reason) }

    fun confirmDecline() {
        val reason = _uiState.value.declineReason.ifBlank { null }
        viewModelScope.launch {
            _uiState.update { it.copy(showDeclineDialog = false, isLoading = true, statusMessage = "Declining...") }
            repo.declineContribution(contributionId, reason)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, statusMessage = "Declined.") }
                    load(contributionId)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, statusMessage = "Failed: ${error.message}") }
                }
        }
    }

    fun purge() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Purging...") }
            repo.purgeContribution(contributionId)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, statusMessage = "Purged.") }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, statusMessage = "Failed: ${error.message}") }
                }
        }
    }

    fun download() {
        val item = _uiState.value.contribution ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(statusMessage = "Downloading APK...") }
            repo.downloadContribution(item.id)
                .onSuccess { bytes ->
                    val name = "${item.packageName}-${item.versionName.ifBlank { "apk" }}.apk"
                    withContext(Dispatchers.IO) { exportIconToDownloads(bytes, name) }
                    _uiState.update { it.copy(statusMessage = "Saved $name to Downloads.") }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(statusMessage = "Download failed: ${error.message}") }
                }
        }
    }
}