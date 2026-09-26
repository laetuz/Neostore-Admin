package id.neotica.neostore.admin.ui.feature.session

import id.neotica.neostore.admin.domain.model.contributor.ContributorMe
import id.neotica.neostore.admin.domain.model.contributor.ContributorRole
import id.neotica.neostore.admin.domain.remote.ContributorsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionStore(
    private val repo: ContributorsRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _me = MutableStateFlow<ContributorMe?>(null)
    val me = _me.asStateFlow()

    private val _isResolving = MutableStateFlow(false)
    val isResolving = _isResolving.asStateFlow()

    fun refresh() {
        if (_isResolving.value) return
        scope.launch {
            _isResolving.value = true
            repo.getMyRole()
                .onSuccess { _me.value = it }
                .onFailure { _me.value = ContributorMe(role = ContributorRole.NONE) }
            _isResolving.value = false
        }
    }

    fun clear() {
        _me.value = null
    }
}