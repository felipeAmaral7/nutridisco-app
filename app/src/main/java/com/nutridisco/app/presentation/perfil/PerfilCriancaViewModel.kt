package com.nutridisco.app.presentation.perfil

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutridisco.app.data.local.entity.AvaliacaoEntity
import com.nutridisco.app.data.local.model.CriancaComComunidade
import com.nutridisco.app.domain.repository.AvaliacaoRepository
import com.nutridisco.app.domain.repository.CriancaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PerfilCriancaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val criancaRepository: CriancaRepository,
    private val avaliacaoRepository: AvaliacaoRepository
) : ViewModel() {

    val criancaId: Long = checkNotNull(savedStateHandle["criancaId"])

    val crianca: StateFlow<CriancaComComunidade?> = criancaRepository
        .buscarCriancaPorIdFlow(criancaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val avaliacoes: StateFlow<List<AvaliacaoEntity>> = avaliacaoRepository
        .listarPorCrianca(criancaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun excluirAvaliacao(avaliacao: AvaliacaoEntity) {
        viewModelScope.launch {
            avaliacaoRepository.deletarAvaliacao(avaliacao)
        }
    }
}
