package com.example.arquitecturamvvmhiltfirebase_albertogv.viewmodel



import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arquitecturamvvmhiltfirebase_albertogv.Datos.Incidencia
import com.example.arquitecturamvvmhiltfirebase_albertogv.repositorio.IncidenciaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncidenciaViewModel @Inject constructor(
    private val repository: IncidenciaRepository
) : ViewModel() {

    // Lista de incidencias (actualización en tiempo real)
    private val _incidencias = MutableStateFlow<List<Incidencia>>(emptyList())
    val incidencias: StateFlow<List<Incidencia>> = _incidencias

    // Estado de carga / errores para la UI
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    init {
        cargarIncidencias()
    }

    private fun cargarIncidencias() {
        viewModelScope.launch {
            repository.getIncidencias()
                .catch { e -> _uiState.value = UiState.Error(e.message ?: "Error desconocido") }
                .collect { lista -> _incidencias.value = lista }
        }
    }

    fun agregarIncidencia(incidencia: Incidencia, fotoUri: Uri? = null) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val urlFoto = if (fotoUri != null) repository.uploadFoto(fotoUri) else incidencia.fotoUrl
                val nuevaIncidencia = incidencia.copy(fotoUrl = urlFoto)
                repository.addIncidencia(nuevaIncidencia)
                _uiState.value = UiState.Success("Incidencia creada correctamente")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al crear")
            }
        }
    }

    fun actualizarIncidencia(incidencia: Incidencia, fotoUri: Uri? = null) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val urlFoto = if (fotoUri != null) repository.uploadFoto(fotoUri) else incidencia.fotoUrl
                val actualizada = incidencia.copy(fotoUrl = urlFoto)
                repository.updateIncidencia(actualizada)
                _uiState.value = UiState.Success("Incidencia actualizada")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al actualizar")
            }
        }
    }

    fun eliminarIncidencia(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteIncidencia(id)
                _uiState.value = UiState.Success("Incidencia eliminada")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al eliminar")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
}