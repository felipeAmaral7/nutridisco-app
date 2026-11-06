package com.nutridisco.app.presentation.exportacao

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutridisco.app.domain.export.CsvExportManager
import com.nutridisco.app.domain.repository.AvaliacaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExportacaoUiState(
    val exportando: Boolean = false,
    val mensagemSucesso: String? = null,
    val erro: String? = null
)

@HiltViewModel
class ExportacaoViewModel @Inject constructor(
    private val avaliacaoRepository: AvaliacaoRepository,
    private val csvExportManager: CsvExportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportacaoUiState())
    val uiState: StateFlow<ExportacaoUiState> = _uiState.asStateFlow()

    val totalAvaliacoes = avaliacaoRepository.listarTodasDetalhadasFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun exportarECompartilhar(context: Context) {
        viewModelScope.launch {
            _uiState.value = ExportacaoUiState(exportando = true)
            try {
                val lista = avaliacaoRepository.listarTodasParaExportacao()
                if (lista.isEmpty()) {
                    _uiState.value = ExportacaoUiState(
                        exportando = false,
                        erro = "Não há avaliações registradas para exportar."
                    )
                    return@launch
                }

                val arquivoCsv = csvExportManager.gerarCsvAvaliacoes(lista)
                val intent = csvExportManager.criarIntentCompartilhamento(arquivoCsv)
                context.startActivity(intent)

                _uiState.value = ExportacaoUiState(
                    exportando = false,
                    mensagemSucesso = "Relatório CSV gerado com sucesso! (${lista.size} registros)"
                )
            } catch (e: Exception) {
                _uiState.value = ExportacaoUiState(
                    exportando = false,
                    erro = "Erro ao exportar CSV: ${e.localizedMessage ?: "Falha desconhecida"}"
                )
            }
        }
    }
}
