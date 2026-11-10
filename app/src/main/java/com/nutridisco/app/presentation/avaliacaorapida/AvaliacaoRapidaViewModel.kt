package com.nutridisco.app.presentation.avaliacaorapida

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutridisco.app.domain.model.ResultadoClassificacao
import com.nutridisco.app.domain.usecase.CalcularClassificacaoNutricionalUseCase
import com.nutridisco.app.domain.usecase.IdadeLimiteExcedidaException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AvaliacaoRapidaUiState(
    val sexo: String = "M",
    /** Começa no meio da faixa (2 anos e 6 meses) para reduzir toques nos steppers. */
    val anos: Int = AvaliacaoRapidaViewModel.ANOS_INICIAL,
    val meses: Int = AvaliacaoRapidaViewModel.MESES_INICIAL,
    /** Texto exibido no campo; o separador decimal é sempre vírgula (ponto digitado vira vírgula). */
    val peso: String = "",
    val estatura: String = "",
    val calculando: Boolean = false,
    val resultado: ResultadoClassificacao? = null,
    val erro: String? = null
) {
    val idadeMeses: Int get() = anos * 12 + meses
    val pesoValor: Double? get() = peso.toDecimalOrNull()
    val estaturaValor: Double? get() = estatura.toDecimalOrNull()

    val podeCalcular: Boolean
        get() = !calculando &&
            (pesoValor ?: 0.0) > 0.0 &&
            (estaturaValor ?: 0.0) > 0.0
}

private fun String.toDecimalOrNull(): Double? = replace(',', '.').toDoubleOrNull()

/** Padrão brasileiro: o separador decimal exibido é sempre vírgula, mesmo se o teclado só tiver ponto. */
private fun String.comVirgulaDecimal(): String = replace('.', ',')

@HiltViewModel
class AvaliacaoRapidaViewModel @Inject constructor(
    private val calcularClassificacaoUseCase: CalcularClassificacaoNutricionalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvaliacaoRapidaUiState())
    val uiState: StateFlow<AvaliacaoRapidaUiState> = _uiState.asStateFlow()

    fun onSexoChange(sexo: String) {
        _uiState.update { it.copy(sexo = sexo, resultado = null, erro = null) }
    }

    fun onAnosChange(anos: Int) {
        _uiState.update { it.copy(anos = anos.coerceIn(0, ANOS_MAX), resultado = null, erro = null) }
    }

    fun onMesesChange(meses: Int) {
        _uiState.update { it.copy(meses = meses.coerceIn(0, MESES_MAX), resultado = null, erro = null) }
    }

    fun onPesoChange(peso: String) {
        val normalizado = peso.comVirgulaDecimal()
        if (normalizado.isEmpty() || normalizado.matches(REGEX_PESO)) {
            _uiState.update { it.copy(peso = normalizado, resultado = null, erro = null) }
        }
    }

    fun onEstaturaChange(estatura: String) {
        val normalizado = estatura.comVirgulaDecimal()
        if (normalizado.isEmpty() || normalizado.matches(REGEX_ESTATURA)) {
            _uiState.update { it.copy(estatura = normalizado, resultado = null, erro = null) }
        }
    }

    fun calcular() {
        val state = _uiState.value
        val idade = state.idadeMeses
        val peso = state.pesoValor
        val estatura = state.estaturaValor

        if (peso == null || peso <= 0.0 || peso > PESO_MAX_KG) {
            _uiState.update { it.copy(erro = "Informe um peso válido (ex: 12,5 kg).") }
            return
        }
        if (estatura == null || estatura <= 0.0 || estatura > ESTATURA_MAX_CM) {
            _uiState.update { it.copy(erro = "Informe uma estatura válida (ex: 85,0 cm).") }
            return
        }

        _uiState.update { it.copy(calculando = true, erro = null) }
        viewModelScope.launch {
            try {
                val res = calcularClassificacaoUseCase(
                    idadeMeses = idade,
                    sexo = state.sexo,
                    peso = peso,
                    estatura = estatura
                )
                _uiState.update { it.copy(calculando = false, resultado = res) }
            } catch (e: IdadeLimiteExcedidaException) {
                _uiState.update {
                    it.copy(
                        calculando = false,
                        resultado = null,
                        erro = "Idade não permitida (${e.idadeMeses} meses). O NutriDisco atende " +
                            "apenas crianças de 0 a 59 meses completos (menores de 5 anos)."
                    )
                }
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(calculando = false, resultado = null, erro = e.message) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        calculando = false,
                        resultado = null,
                        erro = ERRO_INESPERADO
                    )
                }
            }
        }
    }

    fun fecharResultado() {
        _uiState.update { it.copy(resultado = null) }
    }

    fun limpar() {
        _uiState.update { AvaliacaoRapidaUiState(sexo = it.sexo) }
    }

    companion object {
        /** 4 anos e 11 meses = 59 meses, limite superior do disco da OMS. */
        const val ANOS_MAX = 4
        const val MESES_MAX = 11
        /** Valores iniciais no meio da faixa: 2 anos e 6 meses (30 meses). */
        const val ANOS_INICIAL = 2
        const val MESES_INICIAL = 6
        const val PESO_MAX_KG = 50.0
        const val ESTATURA_MAX_CM = 150.0
        const val ERRO_INESPERADO =
            "Erro ao calcular a classificação nutricional. Tente novamente."

        private val REGEX_PESO = Regex("^\\d{0,3}(,\\d{0,3})?$")
        private val REGEX_ESTATURA = Regex("^\\d{0,3}(,\\d{0,2})?$")
    }
}
