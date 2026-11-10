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
import com.nutridisco.app.domain.usecase.ReferenciaNaoEncontradaException
import com.nutridisco.app.domain.util.DataUtils
import com.nutridisco.app.domain.util.IdadeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CadastroCriancaUiState(
    val nome: String = "",
    val sexo: String = "M",
    val dataNascimentoTexto: String = "", // Formato dd/MM/yyyy
    val nomeResponsavel: String = "",
    val numeroCns: String = "",
    val poloBaseId: Long? = null,
    val comunidadeNome: String = "",
    /** true quando sexo e data de nascimento vieram da Avaliação Rápida e não podem ser alterados aqui. */
    val dadosDaAvaliacao: Boolean = false,
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

    /**
     * Usa o sexo e a data de nascimento informados na Avaliação Rápida, para que a classificação
     * gravada seja a mesma exibida na tela de Resultado (mesma idade em meses na consulta à OMS).
     */
    fun preencherDadosDaAvaliacao(sexo: String, dataNascimentoMillis: Long) {
        _uiState.value = _uiState.value.copy(
            sexo = sexo,
            dataNascimentoTexto = DataUtils.formatar(dataNascimentoMillis),
            dadosDaAvaliacao = true
        )
    }

    fun onNomeChange(nome: String) {
        _uiState.value = _uiState.value.copy(nome = nome, erro = null)
    }

    fun onSexoChange(sexo: String) {
        if (_uiState.value.dadosDaAvaliacao) return
        _uiState.value = _uiState.value.copy(sexo = sexo, erro = null)
    }

    fun onDataNascimentoChange(data: String) {
        if (_uiState.value.dadosDaAvaliacao) return
        // Formata máscara dd/MM/yyyy automaticamente
        _uiState.value = _uiState.value.copy(dataNascimentoTexto = DataUtils.aplicarMascara(data), erro = null)
    }

    fun onNomeResponsavelChange(nome: String) {
        _uiState.value = _uiState.value.copy(nomeResponsavel = nome, erro = null)
    }

    fun onNumeroCnsChange(cns: String) {
        // CNS tem 15 dígitos; descarta espaços/pontuação colados para não burlar a checagem de duplicidade
        val somenteDigitos = cns.filter(Char::isDigit).take(CNS_TAMANHO)
        _uiState.value = _uiState.value.copy(numeroCns = somenteDigitos, erro = null)
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

        val dataNascTimestamp = DataUtils.parse(state.dataNascimentoTexto)
        if (dataNascTimestamp != null && dataNascTimestamp > System.currentTimeMillis()) {
            _uiState.value = state.copy(erro = "Data de nascimento não pode ser futura")
            return
        }

        if (dataNascTimestamp == null) {
            _uiState.value = state.copy(erro = "Informe uma data de nascimento válida (DD/MM/AAAA)")
            return
        }

        if (state.numeroCns.isNotEmpty() && state.numeroCns.length != CNS_TAMANHO) {
            _uiState.value = state.copy(erro = "O CNS deve ter $CNS_TAMANHO dígitos")
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
            try {
                salvarDados(state, state.poloBaseId, dataNascTimestamp, dadosAvaliacaoPendente, onSucesso)
            } catch (e: CancellationException) {
                throw e
            } catch (e: ReferenciaNaoEncontradaException) {
                _uiState.value = _uiState.value.copy(
                    salvando = false,
                    erro = ReferenciaNaoEncontradaException.MENSAGEM_USUARIO
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(salvando = false, erro = ERRO_AO_SALVAR)
            }
        }
    }

    private suspend fun salvarDados(
        state: CadastroCriancaUiState,
        poloBaseId: Long,
        dataNascTimestamp: Long,
        dadosAvaliacaoPendente: Pair<Double, Double>?,
        onSucesso: (Long) -> Unit
    ) {
        val cns = state.numeroCns.ifEmpty { null }
        if (cns != null) {
            criancaRepository.buscarCriancaPorCns(cns)?.let { existente ->
                _uiState.value = _uiState.value.copy(
                    salvando = false,
                    erro = "Este CNS já está cadastrado para ${existente.nome}."
                )
                return
            }
        }

        // Classifica a avaliação pendente ANTES de gravar qualquer coisa: se faltar referência
        // da OMS, nada é salvo (evita criança gravada sem a avaliação e cadastro duplicado).
        val idadeMeses = IdadeUtils.calcularIdadeEmMesesCompletos(dataNascTimestamp)
        val resultado = dadosAvaliacaoPendente?.let { (peso, estatura) ->
            calcularClassificacaoUseCase(
                idadeMeses = idadeMeses,
                sexo = state.sexo,
                peso = peso,
                estatura = estatura
            )
        }

        // 1. Obter ou criar comunidade on-the-fly
        val comunidadeId = territorioRepository.obterOuCriarComunidade(
            poloBaseId = poloBaseId,
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
                numeroCns = cns
            )
        )

        // 3. Se houver avaliação pendente (originada da Avaliação Rápida), salvar agora
        if (resultado != null) {
            val config = configuracaoRepository.getConfiguracao()

            avaliacaoRepository.salvarAvaliacao(
                AvaliacaoEntity(
                    criancaId = criancaId,
                    comunidadeAtendimentoId = comunidadeId,
                    dataAtendimento = System.currentTimeMillis(),
                    idadeMesesMomento = idadeMeses,
                    peso = resultado.peso,
                    estatura = resultado.estatura,
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

    private companion object {
        const val CNS_TAMANHO = 15
        const val ERRO_AO_SALVAR = "Não foi possível salvar o cadastro. Tente novamente."
    }
}
