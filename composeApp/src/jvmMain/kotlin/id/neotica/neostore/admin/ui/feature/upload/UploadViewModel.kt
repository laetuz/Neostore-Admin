package id.neotica.neostore.admin.ui.feature.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.neostore.admin.domain.model.RegisterAppRequest
import id.neotica.neostore.admin.domain.remote.FileRepository
import id.neotica.neostore.admin.utils.Constants.BASE_URL_BUCKET
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import java.io.File

class UploadViewModel(
    private val repository: FileRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState = _uiState.asStateFlow()

    fun clear(type: ClearState) {
        when (type) {
            ClearState.UPLOAD -> _uiState.update { it.copy(filePath = "", statusMessage =  "", uploadProgress =  0f) }
            ClearState.ALL -> _uiState.update { UploadUiState() }
        }
    }

    fun setPath(path: String) {
        _uiState.update {
            it.copy(filePath = path, statusMessage = "Analyzing APK...", uploadProgress = 0f)
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(path)

                if (file.extension.equals("apk", true)) {
                    val apkFile = ApkFile(file)
                    val apkMeta = apkFile.apkMeta

                    val iconData: ByteArray? = try { apkFile.iconFile?.data } catch (e: Exception) { null }

                    _uiState.update {
                        it.copy(
                            apkFileFolder = apkMeta.packageName ?: it.apkFileFolder,
                            versionName = apkMeta.versionName ?: it.versionName,
                            versionCode = apkMeta.versionCode?.toString() ?: it.versionCode,
                            minSdk = apkMeta.minSdkVersion ?: it.minSdk,
                            maxSdk = apkMeta.targetSdkVersion ?: it.maxSdk,
                            title = apkMeta.label ?: it.title,
                            description = apkMeta.label ?: it.description,
                            iconByteArray = iconData,
                            statusMessage = "APK analyzed successfully. Ready to upload."
                        )
                    }

                    apkFile.close()
                } else {
                    _uiState.update { it.copy(statusMessage = "File selected. Ready to upload.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "Failed to parse APK: ${e.message}") }
            }
        }
    }

    fun setApkFileFolder(folder: String) =_uiState.update { it.copy(apkFileFolder = folder) }
    fun setVersionName(name: String) = _uiState.update { it.copy(versionName = name) }
    fun setVersionCode(code: String) = _uiState.update { it.copy(versionCode = code.filter { char -> char.isDigit() }) } // Force numbers only
    fun setChangelog(log: String) = _uiState.update { it.copy(changelog = log) }
    fun setMinSdk(minSdk: String) = _uiState.update { it.copy(minSdk = minSdk.filter { char -> char.isDigit() }) }

    fun setMaxSdk(maxSdk: String) = _uiState.update { it.copy(maxSdk = maxSdk.filter { char -> char.isDigit() }) }

    fun checkLatestVersion() = viewModelScope.launch {
        val result = repository.checkLatestVersion(_uiState.value.apkFileFolder)

        result.onSuccess { data ->
            _uiState.update { it.copy(
                versionName = data.versionName,
                minSdk = data.minSdk.toString(),
                maxSdk = data.maxSdk.toString(),
                versionCode = data.versionCode.toString(),
                changelog = data.changelog
            ) }
        }
    }

    fun upload() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.filePath.isBlank()) return
        if (currentState.apkFileFolder.isBlank() || currentState.versionName.isBlank() || currentState.versionCode.isBlank()) {
            _uiState.update { it.copy(statusMessage = "Please fill in all version details!") }
            return
        }

        val file = File(currentState.filePath)
        if (!file.exists()) {
            _uiState.update { it.copy(statusMessage = "File does not exist!") }
            return
        }

        _uiState.update {
            it.copy(isLoading = true, statusMessage = "Starting upload...", uploadProgress = 0f)
        }

        val bucketUrl = "$BASE_URL_BUCKET/neostore"

        viewModelScope.launch {
            val uploadResult = repository.uploadFile(
                file = file,
                s3Path = bucketUrl,
                apkPath = currentState.apkFileFolder,
                versionCode = currentState.versionCode.toInt()
            ) { progress ->
                _uiState.update { it.copy(uploadProgress = progress) }
            }

            uploadResult.onSuccess {
                val fileUrl = "/buckets/neostore/${currentState.apkFileFolder}/${currentState.versionCode}.apk"

                _uiState.update {
                    it.copy(statusMessage = "Upload Success! ✅", uploadProgress = 1f, isLoading = false)
                }

                val registerResult = repository.publishApkVersion(
                    packageName = currentState.apkFileFolder,
                    versionName = currentState.versionName,
                    versionCode = currentState.versionCode.toInt(),
                    fileUrl = fileUrl,
                    changelog = currentState.changelog,
                    minSdk = currentState.minSdk.toInt(),
                    maxSdk = currentState.maxSdk.toInt(),
                )

                registerResult
                    .onSuccess { _uiState.update { it.copy(statusMessage = "Version published successfully! ✅", isLoading = false) } }
                    .onFailure { error ->
                        when (error.message) {
                            "Package not found"-> {
                                registerApp()
                            }
                            "Failed to publish app version" -> registerApp()
                            else -> {
                                _uiState.update { it.copy(statusMessage = "File uploaded, but registration failed: ${error.message} ❌", isLoading = false) }
                            }
                        }
                    }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(statusMessage = "Error: ${e.message} ❌", isLoading = false)
                }
            }
        }
    }

    fun registerApp() {
        val currentState = _uiState.value
        _uiState.update { it.copy(statusMessage = "Package not found. Auto-registering root app...") }

        viewModelScope.launch {
            val bucketUrl = "$BASE_URL_BUCKET/neostore"

            val registerRequest = RegisterAppRequest(
                packageName = currentState.apkFileFolder,
                title = currentState.title,
                description = currentState.description,
                category = currentState.category,
            )

            val registerResult = repository.registerApp(registerRequest)

            registerResult.onSuccess {
                _uiState.update { it.copy(statusMessage = "Database record created. Uploading icon...") }

                // Upload the icon if we have one
                if (currentState.iconByteArray != null) {
                    try {
                        val tempIcon = File.createTempFile("app_icon", ".png")
                        tempIcon.writeBytes(currentState.iconByteArray)

                        repository.uploadIcon(
                            file = tempIcon,
                            s3Path = bucketUrl,
                            apkPath = currentState.apkFileFolder
                        )

                        tempIcon.deleteOnExit()
                    } catch (e: Exception) {
                        println("Warning: Icon upload failed during auto-registration - ${e.message}")
                    }
                }

                _uiState.update { it.copy(statusMessage = "App Auto-Registered! Re-publishing version...") }

                // RE-ATTEMPT VERSION PUBLISH
                // We know the APK file is already in the bucket from the first step in upload()
                val fileUrl = "/buckets/neostore/${currentState.apkFileFolder}/${currentState.versionCode}.apk"
                val publishResult = repository.publishApkVersion(
                    packageName = currentState.apkFileFolder,
                    versionName = currentState.versionName,
                    versionCode = currentState.versionCode.toInt(),
                    fileUrl = fileUrl,
                    changelog = currentState.changelog,
                    minSdk = currentState.minSdk.toInt(),
                    maxSdk = currentState.maxSdk.toInt(),
                )

                publishResult
                    .onSuccess { _uiState.update { it.copy(statusMessage = "Auto-Registration & Publish complete! ✅", isLoading = false) } }
                    .onFailure { error -> _uiState.update { it.copy(statusMessage = "Registered app, but publish failed: ${error.message} ❌", isLoading = false) } }

            }.onFailure { e ->
                _uiState.update { it.copy(statusMessage = "Auto-registration failed: ${e.message} ❌", isLoading = false) }
            }
        }
    }
}

enum class TargetUpload(val label: String) {
    NEOSTORE("neostore")
}