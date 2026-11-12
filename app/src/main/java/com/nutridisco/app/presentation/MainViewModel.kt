package com.nutridisco.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutridisco.app.domain.repository.ConfiguracaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val configuracaoRepository: ConfiguracaoRepository
) : ViewModel() {

    /**
     * Emite true quando a identificação do profissional e o polo base padrão estão configurados;
     * false quando não configurados ou incompletos; null enquanto o Room carrega o estado inicial.
     */
    val estaConfigurado: StateFlow<Boolean?> = configuracaoRepository.getConfiguracaoFlow()
        .map { config ->
            config != null && config.nomeProfissional.isNotBlank() && config.poloBaseIdPadrao != null
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
