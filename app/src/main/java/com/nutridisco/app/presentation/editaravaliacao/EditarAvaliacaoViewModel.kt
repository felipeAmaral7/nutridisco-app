package com.nutridisco.app.presentation.editaravaliacao

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutridisco.app.domain.model.ClassificacaoEstatura
import com.nutridisco.app.domain.model.ClassificacaoPeso
import com.nutridisco.app.domain.repository.AvaliacaoRepository
import com.nutridisco.app.domain.repository.CriancaRepository
import com.nutridisco.app.domain.usecase.CalcularClassificacaoNutricionalUseCase
import com.nutridisco.app.domain.usecase.ReferenciaNaoEncontradaException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditarAvaliacaoUiState(
    val carregando: Boolean = true,
    val criancaNome: String = "",
    val criancaSexo: String = "M",
    val idadeMesesMomento: Int = 0,
    val dataAtendimento: Long = 0L,
    val nomeProfissional: String = "",
    val registroCoren: String = "",
    val peso: String = "",
    val estatura: String = "",
    val observacoes: String = "",
    val classificacaoEstatura: ClassificacaoEstatura? = null,
    val classificacaoPeso: ClassificacaoPeso? = null,
    val salvando: Boolean = false,
    val erro: String? = null
)

@HiltViewModel
class EditarAvaliacaoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val avaliacaoRepository: AvaliacaoRepository,
    private val criancaRepository: CriancaRepository,
    private val calcularClassificacaoUseCase: CalcularClassificacaoNutricionalUseCase
) : ViewModel() {

    val avaliacaoId: Long = checkNotNull(savedStateHandle["avaliacaoId"])

    private val _uiState = MutableStateFlow(EditarAvaliacaoUiState())
    val uiState: StateFlow<EditarAvaliacaoUiState> = _uiState.asStateFlow()

    init {
        carregarDados()
    }

    private fun carregarDados() {
        viewModelScope.launch {
            val avaliacao = avaliacaoRepository.buscarPorId(avaliacaoId)
            if (avaliacao != null) {
                val crianca = criancaRepository.buscarCriancaPorId(avaliacao.criancaId)
                _uiState.value = EditarAvaliacaoUiState(
                    carregando = false,
                    criancaNome = crianca?.nome ?: "Criança",
                    criancaSexo = crianca?.sexo ?: "M",
                    idadeMesesMomento = avaliacao.idadeMesesMomento,
                    dataAtendimento = avaliacao.dataAtendimento,
                    nomeProfissional = avaliacao.nomeProfissional,
                    registroCoren = avaliacao.registroCoren,
                    peso = avaliacao.peso.toString().replace('.', ','),
                    estatura = avaliacao.estatura.toString().replace('.', ','),
                    observacoes = avaliacao.observacoes ?: "",
                    classificacaoEstatura = avaliacao.classificacaoEstatura,
                    classificacaoPeso = avaliacao.classificacaoPeso
                )
            } else {
                _uiState.value = _uiState.value.copy(carregando = false, erro = "Avaliação não encontrada")
            }
        }
    }

    fun onPesoChange(novoPeso: String) {
        val normalizado = novoPeso.replace('.', ',')
        if (normalizado.isEmpty() || normalizado.matches(REGEX_DECIMAL)) {
            _uiState.update { it.copy(peso = normalizado, erro = null) }
        }
    }

    fun onEstaturaChange(novaEstatura: String) {
        val normalizado = novaEstatura.replace('.', ',')
        if (normalizado.isEmpty() || normalizado.matches(REGEX_DECIMAL)) {
            _uiState.update { it.copy(estatura = normalizado, erro = null) }
        }
    }

    fun onObservacoesChange(novasObs: String) {
        _uiState.update { it.copy(observacoes = novasObs.take(500), erro = null) }
    }

    fun salvar(onSucesso: () -> Unit) {
        val state = _uiState.value
        val pesoValor = state.peso.replace(',', '.').toDoubleOrNull()
        val estaturaValor = state.estatura.replace(',', '.').toDoubleOrNull()

        if (pesoValor == null || pesoValor <= 0.0 || pesoValor > 50.0) {
            _uiState.update { it.copy(erro = "Informe um peso válido (ex: 10,5 kg)") }
            return
        }

        if (estaturaValor == null || estaturaValor <= 0.0 || estaturaValor > 150.0) {
            _uiState.update { it.copy(erro = "Informe uma estatura válida (ex: 80,0 cm)") }
            return
        }

        _uiState.update { it.copy(salvando = true, erro = null) }

        viewModelScope.launch {
            val avaliacaoAtual = avaliacaoRepository.buscarPorId(avaliacaoId)
            if (avaliacaoAtual == null) {
                _uiState.update { it.copy(salvando = false, erro = "Avaliação não encontrada") }
                return@launch
            }

            try {
                val resultado = calcularClassificacaoUseCase(
                    sexo = state.criancaSexo,
                    idadeMeses = avaliacaoAtual.idadeMesesMomento,
                    peso = pesoValor,
                    estatura = estaturaValor
                )

                val avaliacaoAtualizada = avaliacaoAtual.copy(
                    peso = pesoValor,
                    estatura = estaturaValor,
                    classificacaoEstatura = resultado.classificacaoEstatura,
                    classificacaoPeso = resultado.classificacaoPeso,
                    observacoes = state.observacoes.trim().ifEmpty { null }
                )

                avaliacaoRepository.atualizarAvaliacao(avaliacaoAtualizada)
                _uiState.update { it.copy(salvando = false) }
                onSucesso()
            } catch (e: ReferenciaNaoEncontradaException) {
                _uiState.update {
                    it.copy(
                        salvando = false,
                        erro = "Referência OMS indisponível para esta idade/sexo no dispositivo."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(salvando = false, erro = "Não foi possível salvar as alterações. Tente novamente.")
                }
            }
        }
    }

    private companion object {
        val REGEX_DECIMAL = Regex("""^\d{0,3}([,\.]\d{0,2})?$""")
    }
}
