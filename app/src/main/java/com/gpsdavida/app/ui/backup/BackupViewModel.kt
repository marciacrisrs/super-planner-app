package com.gpsdavida.app.ui.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpsdavida.app.data.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backup: BackupManager,
) : ViewModel() {
    var error: String? = null
        private set

    fun export(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { backup.exportTo(uri) }.onFailure { error = it.message ?: "falha ao exportar" }
        }
    }

    fun restore(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { backup.restoreFrom(uri) }.onFailure { error = it.message ?: "falha ao restaurar" }
        }
    }
}
