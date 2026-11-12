package com.nutridisco.app.presentation.avaliacoessalvas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutridisco.app.data.local.model.AvaliacaoDetalhada
import com.nutridisco.app.domain.repository.AvaliacaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class TipoOrdenacao(val rotulo: String) {
    RECENTES("Mais recentes"),
    ALFABETICA("Ordem alfabética (A–Z)")
}

@HiltViewModel
class AvaliacoesSalvasViewModel @Inject constructor(
    private val avaliacaoRepository: AvaliacaoRepository
) : ViewModel() {

    private val _termoBusca = MutableStateFlow("")
    val termoBusca: StateFlow<String> = _termoBusca.asStateFlow()

    private val _ordenacao = MutableStateFlow(TipoOrdenacao.RECENTES)
    val ordenacao: StateFlow<TipoOrdenacao> = _ordenacao.asStateFlow()

    private val _todasAvaliacoes = avaliacaoRepository.listarTodasDetalhadasFlow()

    val avaliacoes: StateFlow<List<AvaliacaoDetalhada>> = combine(
        _todasAvaliacoes,
        _termoBusca,
        _ordenacao
    ) { lista, termo, ord ->
        val filtrada = if (termo.isBlank()) {
            lista
        } else {
            lista.filter { item ->
                item.criancaNome.contains(termo, ignoreCase = true) ||
                item.comunidadeNome.contains(termo, ignoreCase = true) ||
                item.poloBaseNome.contains(termo, ignoreCase = true)
            }
        }
        when (ord) {
            TipoOrdenacao.RECENTES -> filtrada.sortedByDescending { it.dataAtendimento }
            TipoOrdenacao.ALFABETICA -> filtrada.sortedWith(
                compareBy<AvaliacaoDetalhada, String>(String.CASE_INSENSITIVE_ORDER) { it.criancaNome }
                    .thenByDescending { it.dataAtendimento }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalGeral: StateFlow<Int> = _todasAvaliacoes
        .combine(MutableStateFlow(Unit)) { lista, _ -> lista.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun onTermoBuscaChange(novoTermo: String) {
        _termoBusca.value = novoTermo
    }

    fun limparTermoBusca() {
        _termoBusca.value = ""
    }

    fun alternarOrdenacao() {
        _ordenacao.value = if (_ordenacao.value == TipoOrdenacao.RECENTES) {
            TipoOrdenacao.ALFABETICA
        } else {
            TipoOrdenacao.RECENTES
        }
    }

    fun definirOrdenacao(tipo: TipoOrdenacao) {
        _ordenacao.value = tipo
    }
}
