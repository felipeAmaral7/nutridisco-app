package com.nutridisco.app.presentation.cadastro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutridisco.app.data.local.entity.AvaliacaoEntity
import com.nutridisco.app.data.local.entity.ComunidadeEntity
import com.nutridisco.app.data.local.entity.CriancaEntity
import com.nutridisco.app.data.local.entity.PoloBaseEntity
import com.nutridisco.app.domain.repository.AvaliacaoRepository
import com.nutridisco.app.domain.repository.ConfiguracaoRepository
import com.nutridisco.app.domain.repository.CriancaRepository
import com.nutridisco.app.domain.repository.TerritorioRepository
import com.nutridisco.app.domain.usecase.CalcularClassificacaoNutricionalUseCase
import com.nutridisco.app.domain.util.IdadeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class CadastroCriancaUiState(
    val nome: String = "",
    val sexo: String = "M",
    val dataNascimentoTexto: String = "", // Formato dd/MM/yyyy
    val nomeResponsavel: String = "",
    val numeroCns: String = "",
    val poloBaseId: Long? = null,
    val comunidadeNome: String = "",
    val salvando: Boolean = false,
    val erro: String? = null
)

@HiltViewModel
class CadastroCriancaViewModel @Inject constructor(
    private val criancaRepository: CriancaRepository,
    private val territorioRepository: TerritorioRepository,
    private val configuracaoRepository: ConfiguracaoRepository,
    private val avaliacaoRepository: AvaliacaoRepository,
    private val calcularClassificacaoUseCase: CalcularClassificacaoNutricionalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CadastroCriancaUiState())
    val uiState: StateFlow<CadastroCriancaUiState> = _uiState.asStateFlow()

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        isLenient = false
    }

    val polosBase: StateFlow<List<PoloBaseEntity>> = territorioRepository.listarPolosBase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val comunidadesDoPolo: StateFlow<List<ComunidadeEntity>> = _uiState
        .flatMapLatest { state ->
            val poloId = state.poloBaseId
            if (poloId != null) {
                territorioRepository.listarComunidadesPorPolo(poloId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            configuracaoRepository.getConfiguracao()?.let { config ->
                if (_uiState.value.poloBaseId == null) {
                    _uiState.value = _uiState.value.copy(poloBaseId = config.poloBaseIdPadrao)
                }
            }
        }
    }

    fun preencherDadosIniciais(sexo: String, idadeMeses: Int) {
        _uiState.value = _uiState.value.copy(
            sexo = sexo
        )
    }

    fun onNomeChange(nome: String) {
        _uiState.value = _uiState.value.copy(nome = nome, erro = null)
    }

    fun onSexoChange(sexo: String) {
        _uiState.value = _uiState.value.copy(sexo = sexo, erro = null)
    }

    fun onDataNascimentoChange(data: String) {
        // Formata máscara dd/MM/yyyy automaticamente
        val digits = data.filter { it.isDigit() }
        val formatted = buildString {
            for (i in digits.indices) {
                if (i == 2 || i == 4) append("/")
                if (i < 8) append(digits[i])
            }
        }
        _uiState.value = _uiState.value.copy(dataNascimentoTexto = formatted, erro = null)
    }

    fun onNomeResponsavelChange(nome: String) {
        _uiState.value = _uiState.value.copy(nomeResponsavel = nome, erro = null)
    }

    fun onNumeroCnsChange(cns: String) {
        _uiState.value = _uiState.value.copy(numeroCns = cns, erro = null)
    }

    fun onPoloBaseSelecionado(poloId: Long) {
        _uiState.value = _uiState.value.copy(poloBaseId = poloId, erro = null)
    }

    fun onComunidadeNomeChange(comunidade: String) {
        _uiState.value = _uiState.value.copy(comunidadeNome = comunidade, erro = null)
    }

    fun salvar(
        dadosAvaliacaoPendente: Pair<Double, Double>? = null, // peso e estatura se veio da Avaliação Rápida
        onSucesso: (Long) -> Unit
    ) {
        val state = _uiState.value

        if (state.nome.isBlank()) {
            _uiState.value = state.copy(erro = "Informe o nome da criança")
            return
        }
        if (state.nomeResponsavel.isBlank()) {
            _uiState.value = state.copy(erro = "Informe o nome do responsável")
            return
        }
        if (state.poloBaseId == null) {
            _uiState.value = state.copy(erro = "Selecione o Polo Base")
            return
        }
        if (state.comunidadeNome.isBlank()) {
            _uiState.value = state.copy(erro = "Informe ou selecione a Comunidade")
            return
        }

        val dataNascTimestamp = try {
            val parsedDate = dateFormatter.parse(state.dataNascimentoTexto)
            if (parsedDate != null && parsedDate.after(Date())) {
                _uiState.value = state.copy(erro = "Data de nascimento não pode ser futura")
                return
            }
            parsedDate?.time
        } catch (e: Exception) {
            null
        }

        if (dataNascTimestamp == null) {
            _uiState.value = state.copy(erro = "Informe uma data de nascimento válida (DD/MM/AAAA)")
            return
        }

        val idadeCalculadaMeses = IdadeUtils.calcularIdadeEmMesesCompletos(dataNascTimestamp)
        if (dadosAvaliacaoPendente != null && idadeCalculadaMeses >= 60) {
            _uiState.value = state.copy(
                erro = "A data informada corresponde a uma idade de $idadeCalculadaMeses meses (≥ 60 meses). O NutriDisco avalia exclusivamente crianças de 0 a 59 meses."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(salvando = true, erro = null)

            // 1. Obter ou criar comunidade on-the-fly
            val comunidadeId = territorioRepository.obterOuCriarComunidade(
                poloBaseId = state.poloBaseId,
                nomeComunidade = state.comunidadeNome
            )

            // 2. Salvar Criança
            val criancaId = criancaRepository.salvarCrianca(
                CriancaEntity(
                    comunidadeId = comunidadeId,
                    nome = state.nome.trim(),
                    sexo = state.sexo,
                    dataNascimento = dataNascTimestamp,
                    nomeResponsavel = state.nomeResponsavel.trim(),
                    numeroCns = state.numeroCns.trim().ifEmpty { null }
                )
            )

            // 3. Se houver avaliação pendente (originada da Avaliação Rápida), salvar agora
            if (dadosAvaliacaoPendente != null) {
                val config = configuracaoRepository.getConfiguracao()
                val peso = dadosAvaliacaoPendente.first
                val estatura = dadosAvaliacaoPendente.second
                val idadeMeses = IdadeUtils.calcularIdadeEmMesesCompletos(dataNascTimestamp)

                val resultado = calcularClassificacaoUseCase(
                    idadeMeses = idadeMeses,
                    sexo = state.sexo,
                    peso = peso,
                    estatura = estatura
                )

                avaliacaoRepository.salvarAvaliacao(
                    AvaliacaoEntity(
                        criancaId = criancaId,
                        comunidadeAtendimentoId = comunidadeId,
                        dataAtendimento = System.currentTimeMillis(),
                        idadeMesesMomento = idadeMeses,
                        peso = peso,
                        estatura = estatura,
                        classificacaoEstatura = resultado.classificacaoEstatura,
                        classificacaoPeso = resultado.classificacaoPeso,
                        observacoes = "Criada a partir da Avaliação Rápida",
                        nomeProfissional = config?.nomeProfissional ?: "Profissional Local",
                        registroCoren = config?.registroCoren ?: "Não informado"
                    )
                )
            }

            _uiState.value = _uiState.value.copy(salvando = false)
            onSucesso(criancaId)
        }
    }
}
