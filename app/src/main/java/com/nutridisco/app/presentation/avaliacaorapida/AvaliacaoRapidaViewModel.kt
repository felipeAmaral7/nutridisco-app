package com.nutridisco.app.presentation.avaliacaorapida

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutridisco.app.domain.model.ResultadoClassificacao
import com.nutridisco.app.domain.usecase.CalcularClassificacaoNutricionalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AvaliacaoRapidaUiState(
    val sexo: String = "M",
    val idadeMeses: String = "",
    val peso: String = "",
    val estatura: String = "",
    val calculando: Boolean = false,
    val resultado: ResultadoClassificacao? = null,
    val erro: String? = null
)

@HiltViewModel
class AvaliacaoRapidaViewModel @Inject constructor(
    private val calcularClassificacaoUseCase: CalcularClassificacaoNutricionalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvaliacaoRapidaUiState())
    val uiState: StateFlow<AvaliacaoRapidaUiState> = _uiState.asStateFlow()

    fun onSexoChange(sexo: String) {
        _uiState.value = _uiState.value.copy(sexo = sexo, erro = null)
        recalcularSeValido()
    }

    fun onIdadeChange(idade: String) {
        if (idade.all { it.isDigit() } && idade.length <= 2) {
            _uiState.value = _uiState.value.copy(idadeMeses = idade, erro = null)
            recalcularSeValido()
        }
    }

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

    fun calcular() {
        val state = _uiState.value
        val idade = state.idadeMeses.toIntOrNull()
        val peso = state.peso.toDoubleOrNull()
        val estatura = state.estatura.toDoubleOrNull()

        if (idade == null || idade < 0 || idade > 60) {
            _uiState.value = state.copy(erro = "Informe uma idade válida entre 0 e 60 meses")
            return
        }
        if (peso == null || peso <= 0.0 || peso > 50.0) {
            _uiState.value = state.copy(erro = "Informe um peso válido (ex: 12.5 kg)")
            return
        }
        if (estatura == null || estatura <= 0.0 || estatura > 150.0) {
            _uiState.value = state.copy(erro = "Informe uma estatura válida (ex: 85.0 cm)")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(calculando = true, erro = null)
            val res = calcularClassificacaoUseCase(
                idadeMeses = idade,
                sexo = state.sexo,
                peso = peso,
                estatura = estatura
            )
            _uiState.value = _uiState.value.copy(
                calculando = false,
                resultado = res
            )
        }
    }

    private fun recalcularSeValido() {
        val state = _uiState.value
        val idade = state.idadeMeses.toIntOrNull()
        val peso = state.peso.toDoubleOrNull()
        val estatura = state.estatura.toDoubleOrNull()

        if (idade != null && idade in 0..60 && peso != null && peso > 0 && estatura != null && estatura > 0) {
            calcular()
        }
    }

    fun limpar() {
        _uiState.value = AvaliacaoRapidaUiState()
    }
}
