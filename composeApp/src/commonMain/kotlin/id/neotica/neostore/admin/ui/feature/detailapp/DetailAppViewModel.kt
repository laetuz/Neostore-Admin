package id.neotica.neostore.admin.ui.feature.detailapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.neostore.admin.domain.model.UpdateAppRequest
import id.neotica.neostore.admin.domain.remote.CategoriesRepository
import id.neotica.neostore.admin.domain.remote.FileRepository
import id.neotica.neostore.admin.platform.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class DetailAppViewModel(
    private val repo: FileRepository,
    private val categoriesRepo: CategoriesRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(DetailAppUiState())
    val uiState = _uiState.asStateFlow()

    private val _openCategoryTrigger = MutableStateFlow(0)
    val openCategoryTrigger = _openCategoryTrigger.asStateFlow()

    fun requestOpenCategory() { _openCategoryTrigger.update { it + 1 } }

    init { loadCategories() }

    fun loadCategories() = viewModelScope.launch {
        categoriesRepo.getCategories().onSuccess { cats ->
            _uiState.update { it.copy(categories = cats) }
        }
    }

    fun setPackageName(packageName: String) = _uiState.update { it.copy(packageName = packageName) }
    fun setTitle(title: String) = _uiState.update { it.copy(title = title) }
    fun setDescription(description: String) = _uiState.update { it.copy(description = description) }
    fun setCategorySlug(slug: String?) = _uiState.update { it.copy(categorySlug = slug) }
    fun setIconUrl(iconUrl: String) = _uiState.update { it.copy(iconUrl = iconUrl) }
    fun setGithubRepo(githubRepo: String) = _uiState.update { it.copy(githubRepo = githubRepo) }

    fun clear() {
        _uiState.update { DetailAppUiState() }
        loadCategories()
    }

    fun getAppDetail() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        delay(100.milliseconds)
        val response = repo.getAppDetail(_uiState.value.packageName)

        response.onSuccess { data ->
            val categories = _uiState.value.categories
            val matching = categories.firstOrNull { it.slug.equals(data.category, ignoreCase = true) }
            _uiState.update { it.copy(
                isLoading = false,
                title = data.title,
                description = data.description,
                categorySlug = matching?.slug ?: data.category,
                iconUrl = data.iconUrl ?: "",
                githubRepo = data.githubRepo ?: "",
                lastGithubTag = data.lastGithubTag ?: "",
                versions = data.versions
            ) }
        }
            .onFailure { error ->
                _uiState.update { it.copy(isLoading = false, statusMessage = "Failed: $error") }
            }
    }

    fun updateApp() {
        val currentState = _uiState.value

        if (currentState.isLoading || currentState.packageName.isBlank() || currentState.title.isBlank()) {
            _uiState.update { it.copy(statusMessage = "Please fill in Package Name and Title!") }
            return
        }

        _uiState.update { it.copy(statusMessage = "Updating app...") }

        viewModelScope.launch {
            val updateAppRequest = UpdateAppRequest(
                title = currentState.title,
                description = currentState.description,
                category = currentState.categorySlug ?: "",
                iconUrl = currentState.iconUrl,
                githubRepo = currentState.githubRepo.ifBlank { null }
            )

            val updateResult = repo.updateApp(currentState.packageName, updateAppRequest)

            updateResult.onSuccess {
                _uiState.update { it.copy(isLoading = false, statusMessage = "Updated.") }
            }.onFailure { error -> _uiState.update { it.copy(isLoading = false, statusMessage = "Failed updating app: $error") } }
        }
    }

    fun uploadIcon(file: PlatformFile) {
        val packageName = _uiState.value.packageName
        if (packageName.isBlank()) {
            _uiState.update { it.copy(statusMessage = "No package name loaded.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingIcon = true, statusMessage = "Uploading icon...") }
            val uploadResult = withContext(Dispatchers.IO) {
                repo.uploadIcon(file, packageName)
            }
            uploadResult.onSuccess {
                val iconUrl = "/buckets/neostore/$packageName/icon.jpg"
                _uiState.update { it.copy(iconUrl = iconUrl, isUploadingIcon = false, statusMessage = "Icon uploaded. Updating app...") }
                val updateRequest = UpdateAppRequest(
                    title = _uiState.value.title,
                    description = _uiState.value.description,
                    category = _uiState.value.categorySlug ?: "",
                    iconUrl = iconUrl,
                    githubRepo = _uiState.value.githubRepo.ifBlank { null }
                )
                repo.updateApp(packageName, updateRequest).onSuccess {
                    _uiState.update { it.copy(isLoading = false, statusMessage = "Icon updated successfully.") }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, statusMessage = "Icon uploaded but failed to update app: $error") }
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isUploadingIcon = false, statusMessage = "Icon upload failed: $error") }
            }
        }
    }

    fun requestUnregister() = _uiState.update { it.copy(showUnregisterConfirm = true) }

    fun cancelUnregister() = _uiState.update { it.copy(showUnregisterConfirm = false) }

    fun unregisterApp(onSuccess: () -> Unit) {
        val packageName = _uiState.value.packageName
        if (packageName.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Unregistering...") }
            repo.unregisterApp(packageName).onSuccess {
                _uiState.update { it.copy(isLoading = false, statusMessage = "App unregistered.") }
                onSuccess()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, showUnregisterConfirm = false, statusMessage = "Failed: $error") }
            }
        }
    }

    fun resetGithubTag() {
        val packageName = _uiState.value.packageName
        if (packageName.isBlank()) {
            _uiState.update { it.copy(statusMessage = "No package name loaded.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Resetting tag...") }
            val result = repo.resetGithubTag(packageName)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, statusMessage = "Tag reset successfully.") }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, statusMessage = "Failed resetting tag: $error") }
            }
        }
    }

    fun deleteVersion(versionId: String) {
        val packageName = _uiState.value.packageName
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Deleting version...") }
            val result = repo.deleteVersion(packageName, versionId)
            result.onSuccess {
                val updated = _uiState.value.versions.filter { it.id != versionId }
                _uiState.update { it.copy(isLoading = false, statusMessage = "Version deleted.", versions = updated) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, statusMessage = "Failed deleting version: $error") }
            }
        }
    }
}