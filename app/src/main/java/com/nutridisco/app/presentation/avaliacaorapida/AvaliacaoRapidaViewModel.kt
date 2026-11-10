package com.nutridisco.app.presentation.avaliacaorapida

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutridisco.app.domain.model.ResultadoClassificacao
import com.nutridisco.app.domain.usecase.CalcularClassificacaoNutricionalUseCase
import com.nutridisco.app.domain.usecase.IdadeLimiteExcedidaException
import com.nutridisco.app.domain.usecase.ReferenciaNaoEncontradaException
import com.nutridisco.app.domain.util.DataUtils
import com.nutridisco.app.domain.util.IdadeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

/** Situação da data de nascimento digitada, já interpretada em relação à data de hoje. */
sealed interface DataNascimentoStatus {
    /** Campo vazio ou ainda incompleto (menos de 8 dígitos). */
    data object Incompleta : DataNascimentoStatus
    data object Invalida : DataNascimentoStatus
    data object Futura : DataNascimentoStatus
    data class ForaDaFaixa(val idadeMeses: Int) : DataNascimentoStatus
    data class Valida(
        val dataNascimentoMillis: Long,
        val idadeMeses: Int,
        /** Ex.: "1 ano e 9 meses". */
        val idadeExtenso: String,
    ) : DataNascimentoStatus
}

data class AvaliacaoRapidaUiState(
    val sexo: String = "M",
    /** Só os dígitos (ddMMaaaa); as barras são exibidas pela máscara do campo. */
    val dataNascimento: String = "",
    val dataNascimentoStatus: DataNascimentoStatus = DataNascimentoStatus.Incompleta,
    /** Texto exibido no campo; o separador decimal é sempre vírgula (ponto digitado vira vírgula). */
    val peso: String = "",
    val estatura: String = "",
    val calculando: Boolean = false,
    val resultado: ResultadoClassificacao? = null,
    /** Data de nascimento usada no [resultado]; segue para o cadastro ao salvar o registro. */
    val dataNascimentoResultado: Long? = null,
    /** Evento único: true logo após um cálculo bem-sucedido, até a tela navegar para o Resultado. */
    val navegarParaResultado: Boolean = false,
    val erro: String? = null
) {
    val pesoValor: Double? get() = peso.toDecimalOrNull()
    val estaturaValor: Double? get() = estatura.toDecimalOrNull()

    val podeCalcular: Boolean
        get() = !calculando &&
            dataNascimentoStatus is DataNascimentoStatus.Valida &&
            (pesoValor ?: 0.0) > 0.0 &&
            (estaturaValor ?: 0.0) > 0.0
}

private fun String.toDecimalOrNull(): Double? = replace(',', '.').toDoubleOrNull()

/** Padrão brasileiro: o separador decimal exibido é sempre vírgula, mesmo se o teclado só tiver ponto. */
private fun String.comVirgulaDecimal(): String = replace('.', ',')

@HiltViewModel
class AvaliacaoRapidaViewModel @Inject constructor(
    private val calcularClassificacaoUseCase: CalcularClassificacaoNutricionalUseCase,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvaliacaoRapidaUiState())
    val uiState: StateFlow<AvaliacaoRapidaUiState> = _uiState.asStateFlow()

    fun onSexoChange(sexo: String) {
        _uiState.update { it.copy(sexo = sexo, resultado = null, erro = null) }
    }

    fun onDataNascimentoChange(texto: String) {
        val digitos = DataUtils.somenteDigitos(texto)
        _uiState.update {
            it.copy(
                dataNascimento = digitos,
                dataNascimentoStatus = interpretarData(digitos),
                resultado = null,
                erro = null
            )
        }
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
        // Reinterpreta no momento do cálculo: a idade pode ter mudado se a tela ficou aberta de um dia para o outro.
        val status = interpretarData(state.dataNascimento)
        val peso = state.pesoValor
        val estatura = state.estaturaValor

        if (status !is DataNascimentoStatus.Valida) {
            _uiState.update {
                it.copy(dataNascimentoStatus = status, erro = "Informe uma data de nascimento válida (dd/mm/aaaa).")
            }
            return
        }
        if (peso == null || peso <= 0.0 || peso > PESO_MAX_KG) {
            _uiState.update { it.copy(erro = "Informe um peso válido (ex: 12,5 kg).") }
            return
        }
        if (estatura == null || estatura <= 0.0 || estatura > ESTATURA_MAX_CM) {
            _uiState.update { it.copy(erro = "Informe uma estatura válida (ex: 85,0 cm).") }
            return
        }

        _uiState.update { it.copy(dataNascimentoStatus = status, calculando = true, erro = null) }
        viewModelScope.launch {
            try {
                val res = calcularClassificacaoUseCase(
                    idadeMeses = status.idadeMeses,
                    sexo = state.sexo,
                    peso = peso,
                    estatura = estatura
                )
                _uiState.update {
                    it.copy(
                        calculando = false,
                        resultado = res,
                        dataNascimentoResultado = status.dataNascimentoMillis,
                        navegarParaResultado = true
                    )
                }
            } catch (e: IdadeLimiteExcedidaException) {
                _uiState.update {
                    it.copy(calculando = false, resultado = null, erro = mensagemForaDaFaixa(e.idadeMeses))
                }
            } catch (e: ReferenciaNaoEncontradaException) {
                _uiState.update {
                    it.copy(
                        calculando = false,
                        resultado = null,
                        erro = ERRO_REFERENCIA_INDISPONIVEL
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

    /** Consome o evento de navegação; o resultado continua disponível para a tela de Resultado. */
    fun onNavegouParaResultado() {
        _uiState.update { it.copy(navegarParaResultado = false) }
    }

    fun limpar() {
        _uiState.update { AvaliacaoRapidaUiState(sexo = it.sexo) }
    }

    private fun interpretarData(digitos: String): DataNascimentoStatus {
        if (digitos.length < DataUtils.TOTAL_DIGITOS) return DataNascimentoStatus.Incompleta
        val nascimento = DataUtils.parse(digitos, clock.zone) ?: return DataNascimentoStatus.Invalida
        val agora = clock.millis()
        if (nascimento > agora) return DataNascimentoStatus.Futura

        val idadeMeses = IdadeUtils.calcularIdadeEmMesesCompletos(nascimento, agora, clock.zone)
        if (idadeMeses > IDADE_MAX_MESES) return DataNascimentoStatus.ForaDaFaixa(idadeMeses)

        return DataNascimentoStatus.Valida(
            dataNascimentoMillis = nascimento,
            idadeMeses = idadeMeses,
            idadeExtenso = IdadeUtils.formatarIdadeExtenso(nascimento, agora, clock.zone)
        )
    }

    companion object {
        /** 59 meses completos (menor de 5 anos): limite superior do disco da OMS. */
        const val IDADE_MAX_MESES = 59
        const val PESO_MAX_KG = 50.0
        const val ESTATURA_MAX_CM = 150.0
        const val ERRO_INESPERADO =
            "Erro ao calcular a classificação nutricional. Tente novamente."
        const val ERRO_REFERENCIA_INDISPONIVEL = ReferenciaNaoEncontradaException.MENSAGEM_USUARIO

        fun mensagemForaDaFaixa(idadeMeses: Int): String =
            "Idade não permitida ($idadeMeses meses). O NutriDisco atende " +
                "apenas crianças de 0 a 59 meses completos (menores de 5 anos)."

        private val REGEX_PESO = Regex("^\\d{0,3}(,\\d{0,3})?$")
        private val REGEX_ESTATURA = Regex("^\\d{0,3}(,\\d{0,2})?$")
    }
}
