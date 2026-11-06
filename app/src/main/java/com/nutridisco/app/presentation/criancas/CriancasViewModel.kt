package com.nutridisco.app.presentation.criancas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutridisco.app.data.local.model.CriancaComComunidade
import com.nutridisco.app.domain.repository.CriancaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CriancasViewModel @Inject constructor(
    private val criancaRepository: CriancaRepository
) : ViewModel() {

    private val _pesquisaTermo = MutableStateFlow("")
    val pesquisaTermo: StateFlow<String> = _pesquisaTermo.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val criancas: StateFlow<List<CriancaComComunidade>> = _pesquisaTermo
        .flatMapLatest { termo ->
            if (termo.isBlank()) {
                criancaRepository.listarCriancas()
            } else {
                criancaRepository.pesquisarCriancas(termo)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onPesquisaChange(termo: String) {
        _pesquisaTermo.value = termo
    }
}
