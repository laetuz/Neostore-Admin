package id.neotica.neostore.admin.ui.feature.registerapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.neostore.admin.domain.model.RegisterAppRequest
import id.neotica.neostore.admin.domain.remote.FileRepository
import id.neotica.neostore.admin.utils.Constants.BASE_URL_BUCKET
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import java.io.File

class RegisterAppViewModel(
    private val repo: FileRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(RegisterAppUiState())
    val uiState = _uiState.asStateFlow()

    fun setPackageName(packageName: String) = _uiState.update { it.copy(packageName = packageName) }
    fun setTitle(title: String) = _uiState.update { it.copy(title = title) }
    fun setDescription(description: String) = _uiState.update { it.copy(description = description) }
    fun setCategory(category: String) = _uiState.update { it.copy(category = category) }

    fun clear() = _uiState.update { RegisterAppUiState() }
    fun setPath(path: String) {
        _uiState.update { it.copy(filePath = path, statusMessage = "Analyzing APK for Registration...") }

        droppedFile()
    }

    fun droppedFile() = viewModelScope.launch {
        val path = _uiState.value.filePath
        try {
            val file = File(path)
            if (file.extension.equals("apk", true)) {
                val apkFile = ApkFile(file)
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
        val path = _uiState.value.filePath
        try {
            val file = File(path)
            if (file.extension.equals("apk", true)) {
                val apkFile = ApkFile(file)
                val apkMeta = apkFile.apkMeta

                val iconData: ByteArray? = try { apkFile.iconFile?.data } catch (e: Exception) { null }

                if (iconData != null) {
                    val downloadsDir = File(System.getProperty("user.home"), "Downloads")
                    val iconFile = File(downloadsDir, "${apkMeta.packageName}_icon.png")
                    iconFile.writeBytes(iconData)
                    println("Debug: Icon successfully extracted to ${iconFile.absolutePath}")
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

    // todo: add function for upload, clear, and apk analyzer. Also icon extractor from the analyzer. Icon uploader too.
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
            val bucketUrl = "$BASE_URL_BUCKET/neostore"

            // Register the app first
            val registerRequest = RegisterAppRequest(
                packageName = currentState.packageName,
                title = currentState.title,
                description = currentState.description,
                category = currentState.category,
            )

            val registerResult = repo.registerApp(registerRequest)

            registerResult.onSuccess {
                _uiState.update { it.copy(statusMessage = "Database record created. Uploading icon...") }

                // Then upload icon
                if (currentState.iconByteArray != null) {
                    try {
                        val tempIcon = File.createTempFile("app_icon", ".png")
                        tempIcon.writeBytes(currentState.iconByteArray)

                        // Call our new dedicated image upload function
                        repo.uploadIcon(
                            file = tempIcon,
                            s3Path = bucketUrl,
                            apkPath = currentState.packageName
                        )

                        tempIcon.deleteOnExit()
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
        val file = File(currentState.filePath)
        if (!file.exists()) {
            _uiState.update { it.copy(statusMessage = "File does not exist!") }
        }
    }
}