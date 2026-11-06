package com.nutridisco.app.presentation.configuracao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutridisco.app.data.local.entity.ConfiguracaoDispositivoEntity
import com.nutridisco.app.data.local.entity.PoloBaseEntity
import com.nutridisco.app.domain.repository.ConfiguracaoRepository
import com.nutridisco.app.domain.repository.TerritorioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfiguracaoUiState(
    val nomeProfissional: String = "",
    val registroCoren: String = "",
    val poloBaseIdPadrao: Long? = null,
    val salvoComSucesso: Boolean = false,
    val erro: String? = null
)

@HiltViewModel
class ConfiguracaoViewModel @Inject constructor(
    private val configuracaoRepository: ConfiguracaoRepository,
    private val territorioRepository: TerritorioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfiguracaoUiState())
    val uiState: StateFlow<ConfiguracaoUiState> = _uiState.asStateFlow()

    val polosBase: StateFlow<List<PoloBaseEntity>> = territorioRepository.listarPolosBase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val configuracaoExistente = configuracaoRepository.getConfiguracaoFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            configuracaoRepository.getConfiguracao()?.let { config ->
                _uiState.value = _uiState.value.copy(
                    nomeProfissional = config.nomeProfissional,
                    registroCoren = config.registroCoren,
                    poloBaseIdPadrao = config.poloBaseIdPadrao
                )
            }
        }
    }

    fun onNomeProfissionalChange(nome: String) {
        _uiState.value = _uiState.value.copy(nomeProfissional = nome, erro = null)
    }

    fun onRegistroCorenChange(coren: String) {
        _uiState.value = _uiState.value.copy(registroCoren = coren, erro = null)
    }

    fun onPoloBaseSelecionado(poloId: Long) {
        _uiState.value = _uiState.value.copy(poloBaseIdPadrao = poloId, erro = null)
    }

    fun salvarConfiguracao(onSucesso: () -> Unit) {
        val state = _uiState.value
        if (state.nomeProfissional.isBlank()) {
            _uiState.value = state.copy(erro = "Informe o nome do profissional")
            return
        }
        if (state.registroCoren.isBlank()) {
            _uiState.value = state.copy(erro = "Informe o registro COREN")
            return
        }

        viewModelScope.launch {
            configuracaoRepository.salvarConfiguracao(
                ConfiguracaoDispositivoEntity(
                    id = 1L,
                    nomeProfissional = state.nomeProfissional.trim(),
                    registroCoren = state.registroCoren.trim(),
                    poloBaseIdPadrao = state.poloBaseIdPadrao
                )
            )
            _uiState.value = state.copy(salvoComSucesso = true)
            onSucesso()
        }
    }
}
