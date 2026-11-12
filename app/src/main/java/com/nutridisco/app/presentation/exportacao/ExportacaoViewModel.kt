package com.nutridisco.app.presentation.exportacao

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutridisco.app.domain.export.CsvExportManager
import com.nutridisco.app.domain.export.PdfExportManager
import com.nutridisco.app.domain.repository.AvaliacaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FormatoExportacao(val rotulo: String, val extensao: String) {
    PDF("Documento PDF (Visualização e Impressão)", "pdf"),
    CSV("Planilha CSV (Excel e SISVAN)", "csv")
}

data class ExportacaoUiState(
    val exportando: Boolean = false,
    val formatoSelecionado: FormatoExportacao = FormatoExportacao.PDF,
    val mensagemSucesso: String? = null,
    val erro: String? = null
)

@HiltViewModel
class ExportacaoViewModel @Inject constructor(
    private val avaliacaoRepository: AvaliacaoRepository,
    private val csvExportManager: CsvExportManager,
    private val pdfExportManager: PdfExportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportacaoUiState())
    val uiState: StateFlow<ExportacaoUiState> = _uiState.asStateFlow()

    val totalAvaliacoes = avaliacaoRepository.listarTodasDetalhadasFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selecionarFormato(formato: FormatoExportacao) {
        _uiState.value = _uiState.value.copy(formatoSelecionado = formato, erro = null, mensagemSucesso = null)
    }

    fun exportar(formato: FormatoExportacao, context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(exportando = true, formatoSelecionado = formato, erro = null, mensagemSucesso = null)
            try {
                val lista = avaliacaoRepository.listarTodasParaExportacao()
                if (lista.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        exportando = false,
                        erro = "Não há avaliações registradas para exportar."
                    )
                    return@launch
                }

                when (formato) {
                    FormatoExportacao.PDF -> {
                        val arquivoPdf = pdfExportManager.gerarPdfAvaliacoes(lista)
                        val intent = pdfExportManager.criarIntentCompartilhamento(arquivoPdf)
                        context.startActivity(intent)
                        _uiState.value = _uiState.value.copy(
                            exportando = false,
                            mensagemSucesso = "Relatório em PDF gerado com sucesso! (${lista.size} registros)"
                        )
                    }
                    FormatoExportacao.CSV -> {
                        val arquivoCsv = csvExportManager.gerarCsvAvaliacoes(lista)
                        val intent = csvExportManager.criarIntentCompartilhamento(arquivoCsv)
                        context.startActivity(intent)
                        _uiState.value = _uiState.value.copy(
                            exportando = false,
                            mensagemSucesso = "Relatório CSV gerado com sucesso! (${lista.size} registros)"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    exportando = false,
                    erro = "Erro ao exportar ${formato.name}: ${e.localizedMessage ?: "Falha desconhecida"}"
                )
            }
        }
    }

    fun exportarECompartilhar(context: Context) {
        exportar(_uiState.value.formatoSelecionado, context)
    }
}
