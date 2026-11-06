package com.nutridisco.app.presentation.novaavaliacao

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutridisco.app.data.local.entity.AvaliacaoEntity
import com.nutridisco.app.data.local.model.CriancaComComunidade
import com.nutridisco.app.domain.model.ResultadoClassificacao
import com.nutridisco.app.domain.repository.AvaliacaoRepository
import com.nutridisco.app.domain.repository.ConfiguracaoRepository
import com.nutridisco.app.domain.repository.CriancaRepository
import com.nutridisco.app.domain.usecase.CalcularClassificacaoNutricionalUseCase
import com.nutridisco.app.domain.util.IdadeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NovaAvaliacaoUiState(
    val peso: String = "",
    val estatura: String = "",
    val observacoes: String = "",
    val resultado: ResultadoClassificacao? = null,
    val salvando: Boolean = false,
    val erro: String? = null
)

@HiltViewModel
class NovaAvaliacaoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val criancaRepository: CriancaRepository,
    private val configuracaoRepository: ConfiguracaoRepository,
    private val avaliacaoRepository: AvaliacaoRepository,
    private val calcularClassificacaoUseCase: CalcularClassificacaoNutricionalUseCase
) : ViewModel() {

    val criancaId: Long = checkNotNull(savedStateHandle["criancaId"])

    val crianca: StateFlow<CriancaComComunidade?> = criancaRepository
        .buscarCriancaPorIdFlow(criancaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(NovaAvaliacaoUiState())
    val uiState: StateFlow<NovaAvaliacaoUiState> = _uiState.asStateFlow()

    fun onPesoChange(peso: String) {
        val normalizado = peso.replace(',', '.')
        if (normalizado.isEmpty() || normalizado.matches(Regex("^\\d{0,3}(\\.\\d{0,3})?$"))) {
            _uiState.value = _uiState.value.copy(peso = normalizado, erro = null)
            recalcularSeValido()
        }
    }

    fun onEstaturaChange(estatura: String) {
        val normalizado = estatura.replace(',', '.')
        if (normalizado.isEmpty() || normalizado.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?$"))) {
            _uiState.value = _uiState.value.copy(estatura = normalizado, erro = null)
            recalcularSeValido()
        }
    }

    fun onObservacoesChange(obs: String) {
        _uiState.value = _uiState.value.copy(observacoes = obs)
    }

    private fun recalcularSeValido() {
        val c = crianca.value ?: return
        val peso = _uiState.value.peso.toDoubleOrNull()
        val estatura = _uiState.value.estatura.toDoubleOrNull()
        val idadeMeses = IdadeUtils.calcularIdadeEmMesesCompletos(c.dataNascimento)

        if (peso != null && peso > 0 && estatura != null && estatura > 0) {
            viewModelScope.launch {
                val res = calcularClassificacaoUseCase(
                    idadeMeses = idadeMeses,
                    sexo = c.sexo,
                    peso = peso,
                    estatura = estatura
                )
                _uiState.value = _uiState.value.copy(resultado = res, erro = null)
            }
        }
    }

    fun salvarAvaliacao(onSucesso: () -> Unit) {
        val c = crianca.value ?: return
        val state = _uiState.value
        val peso = state.peso.toDoubleOrNull()
        val estatura = state.estatura.toDoubleOrNull()
        val idadeMeses = IdadeUtils.calcularIdadeEmMesesCompletos(c.dataNascimento)

        if (peso == null || peso <= 0.0 || peso > 50.0) {
            _uiState.value = state.copy(erro = "Informe um peso válido")
            return
        }
        if (estatura == null || estatura <= 0.0 || estatura > 150.0) {
            _uiState.value = state.copy(erro = "Informe uma estatura válida")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(salvando = true, erro = null)

            val resultado = calcularClassificacaoUseCase(
                idadeMeses = idadeMeses,
                sexo = c.sexo,
                peso = peso,
                estatura = estatura
            )

            val config = configuracaoRepository.getConfiguracao()

            avaliacaoRepository.salvarAvaliacao(
                AvaliacaoEntity(
                    criancaId = c.id,
                    comunidadeAtendimentoId = c.comunidadeId,
                    dataAtendimento = System.currentTimeMillis(),
                    idadeMesesMomento = idadeMeses,
                    peso = peso,
                    estatura = estatura,
                    classificacaoEstatura = resultado.classificacaoEstatura.descricao,
                    classificacaoPeso = resultado.classificacaoPeso.descricao,
                    observacoes = state.observacoes.trim().ifEmpty { null },
                    nomeProfissional = config?.nomeProfissional ?: "Profissional Local",
                    registroCoren = config?.registroCoren ?: "Não informado"
                )
            )

            _uiState.value = state.copy(salvando = false)
            onSucesso()
        }
    }
}
