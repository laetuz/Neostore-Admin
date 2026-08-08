package id.neotica.neostore.admin.ui.feature.registerapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.neostore.admin.domain.model.RegisterAppRequest
import id.neotica.neostore.admin.domain.remote.CategoriesRepository
import id.neotica.neostore.admin.domain.remote.FileRepository
import id.neotica.neostore.admin.platform.PlatformFile
import id.neotica.neostore.admin.platform.exportIconToDownloads
import id.neotica.neostore.admin.platform.platformFileFromBytes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ByteArrayApkFile

class RegisterAppViewModel(
    private val repo: FileRepository,
    private val categoriesRepo: CategoriesRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(RegisterAppUiState())
    val uiState = _uiState.asStateFlow()

    private var currentFile: PlatformFile? = null

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
    fun addSecondaryCategory(slug: String?) {
        if (slug == null) return
        _uiState.update { it.copy(secondaryCategorySlugs = (it.secondaryCategorySlugs + slug).distinct()) }
    }
    fun removeSecondaryCategory(slug: String) = _uiState.update {
        it.copy(secondaryCategorySlugs = it.secondaryCategorySlugs - slug)
    }

    fun clear() {
        _uiState.update { RegisterAppUiState() }
        loadCategories()
    }

    fun setPath(file: PlatformFile) {
        currentFile = file
        _uiState.update { it.copy(filePath = file.path, statusMessage = "Analyzing APK for Registration...") }

        droppedFile()
    }

    fun droppedFile() = viewModelScope.launch {
        val file = currentFile ?: return@launch
        try {
            if (file.extension.equals("apk", true)) {
                val apkFile = ByteArrayApkFile(file.readBytes())
                val apkMeta = apkFile.apkMeta

                val iconData: ByteArray? = try { apkFile.iconFile?.data } catch (e: Exception) { null }

                _uiState.update {
                    it.copy(
                        packageName = apkMeta.packageName ?: it.packageName,
                        title = apkMeta.label ?: it.title,
                        description = apkMeta.label,
                        iconByteArray = iconData,
                        statusMessage = "APK Analyzed. Icon saved to Downloads."
                    )
                }

                apkFile.close()
            } else {
                _uiState.update { it.copy(statusMessage = "Please drop a valid .apk file.") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(statusMessage = "Analysis failed: ${e.message}") }
        }
    }

    fun exportIcon() = viewModelScope.launch {
        val file = currentFile ?: return@launch
        try {
            if (file.extension.equals("apk", true)) {
                val apkFile = ByteArrayApkFile(file.readBytes())
                val apkMeta = apkFile.apkMeta

                val iconData: ByteArray? = try { apkFile.iconFile?.data } catch (e: Exception) { null }

                if (iconData != null) {
                    exportIconToDownloads(iconData, "${apkMeta.packageName}_icon.png")
                    println("Debug: Icon successfully extracted for ${apkMeta.packageName}")
                }

                _uiState.update {
                    it.copy(
                        packageName = apkMeta.packageName ?: it.packageName,
                        title = apkMeta.label ?: it.title,
                        iconByteArray = iconData,
                        statusMessage = "APK Analyzed. Icon saved to Downloads."
                    )
                }

                apkFile.close()
            } else {
                _uiState.update { it.copy(statusMessage = "Please drop a valid .apk file.") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(statusMessage = "Analysis failed: ${e.message}") }
        }
    }

    fun register() {
        val currentState = _uiState.value

        // 1. Validation
        if (currentState.isLoading || currentState.packageName.isBlank() || currentState.title.isBlank()) {
            _uiState.update { it.copy(statusMessage = "Please fill in Package Name and Title!") }
            return
        }

        _uiState.update {
            it.copy(isLoading = true, statusMessage = "Registering package with backend...")
        }

        viewModelScope.launch {
            // Register the app first
            val registerRequest = RegisterAppRequest(
                packageName = currentState.packageName,
                title = currentState.title,
                description = currentState.description,
                category = currentState.categorySlug ?: "",
                categories = currentState.secondaryCategorySlugs.takeIf { it.isNotEmpty() },
            )

            val registerResult = repo.registerApp(registerRequest)

            registerResult.onSuccess {
                _uiState.update { it.copy(statusMessage = "Database record created. Uploading icon...") }

                // Then upload icon
                if (currentState.iconByteArray != null) {
                    try {
                        // Call our new dedicated image upload function
                        repo.uploadIcon(
                            file = platformFileFromBytes("app_icon.png", currentState.iconByteArray),
                            apkPath = currentState.packageName
                        )

                        _uiState.update { it.copy(isLoading = false, statusMessage = "App Registered & Icon Uploaded! ✅") }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isLoading = false, statusMessage = "Registered, but icon upload failed: ${e.message} ⚠️") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, statusMessage = "App Registered Successfully (No Icon) ✅") }
                }

            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, statusMessage = "Registration failed: ${e.message} ❌") }
            }
        }
    }

    fun checkFile() {
        val currentState = _uiState.value
        if (currentFile == null) {
            _uiState.update { it.copy(statusMessage = "File does not exist!") }
        }
    }
}