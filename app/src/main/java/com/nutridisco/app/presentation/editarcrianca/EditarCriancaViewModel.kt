package com.nutridisco.app.presentation.editarcrianca

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutridisco.app.data.local.entity.ComunidadeEntity
import com.nutridisco.app.data.local.entity.PoloBaseEntity
import com.nutridisco.app.domain.repository.CriancaRepository
import com.nutridisco.app.domain.repository.TerritorioRepository
import com.nutridisco.app.domain.util.DataUtils
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
import javax.inject.Inject

data class EditarCriancaUiState(
    val carregando: Boolean = true,
    val nome: String = "",
    val sexo: String = "M",
    val dataNascimentoTexto: String = "",
    val nomeResponsavel: String = "",
    val numeroCns: String = "",
    val poloBaseId: Long? = null,
    val comunidadeNome: String = "",
    val salvando: Boolean = false,
    val erro: String? = null
)

@HiltViewModel
class EditarCriancaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val criancaRepository: CriancaRepository,
    private val territorioRepository: TerritorioRepository
) : ViewModel() {

    val criancaId: Long = checkNotNull(savedStateHandle["criancaId"])

    private val _uiState = MutableStateFlow(EditarCriancaUiState())
    val uiState: StateFlow<EditarCriancaUiState> = _uiState.asStateFlow()

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
        carregarDados()
    }

    private fun carregarDados() {
        viewModelScope.launch {
            val crianca = criancaRepository.buscarCriancaPorId(criancaId)
            if (crianca != null) {
                val comunidade = territorioRepository.buscarComunidadePorId(crianca.comunidadeId)
                _uiState.value = EditarCriancaUiState(
                    carregando = false,
                    nome = crianca.nome,
                    sexo = crianca.sexo,
                    dataNascimentoTexto = DataUtils.formatar(crianca.dataNascimento),
                    nomeResponsavel = crianca.nomeResponsavel,
                    numeroCns = crianca.numeroCns ?: "",
                    poloBaseId = comunidade?.poloBaseId,
                    comunidadeNome = comunidade?.nome ?: ""
                )
            } else {
                _uiState.value = _uiState.value.copy(carregando = false, erro = "Criança não encontrada")
            }
        }
    }

    fun onNomeChange(nome: String) {
        _uiState.value = _uiState.value.copy(nome = nome, erro = null)
    }

    fun onSexoChange(sexo: String) {
        _uiState.value = _uiState.value.copy(sexo = sexo, erro = null)
    }

    fun onDataNascimentoChange(data: String) {
        _uiState.value = _uiState.value.copy(
            dataNascimentoTexto = DataUtils.aplicarMascara(data),
            erro = null
        )
    }

    fun onNomeResponsavelChange(nome: String) {
        _uiState.value = _uiState.value.copy(nomeResponsavel = nome, erro = null)
    }

    fun onNumeroCnsChange(cns: String) {
        val somenteDigitos = cns.filter(Char::isDigit).take(15)
        _uiState.value = _uiState.value.copy(numeroCns = somenteDigitos, erro = null)
    }

    fun onPoloBaseSelecionado(poloId: Long) {
        _uiState.value = _uiState.value.copy(poloBaseId = poloId, erro = null)
    }

    fun onComunidadeNomeChange(comunidade: String) {
        _uiState.value = _uiState.value.copy(comunidadeNome = comunidade, erro = null)
    }

    fun salvar(onSucesso: () -> Unit) {
        val state = _uiState.value
        if (state.nome.isBlank()) {
            _uiState.value = state.copy(erro = "Informe o nome da criança")
            return
        }

        val dataNascTimestamp = DataUtils.parse(state.dataNascimentoTexto)
        if (dataNascTimestamp == null) {
            _uiState.value = state.copy(erro = "Informe uma data de nascimento válida (dd/mm/aaaa)")
            return
        }

        if (dataNascTimestamp > System.currentTimeMillis()) {
            _uiState.value = state.copy(erro = "A data de nascimento não pode ser futura")
            return
        }

        val poloBaseId = state.poloBaseId
        if (poloBaseId == null) {
            _uiState.value = state.copy(erro = "Selecione o Polo Base")
            return
        }

        if (state.comunidadeNome.isBlank()) {
            _uiState.value = state.copy(erro = "Informe ou selecione a Comunidade")
            return
        }

        if (state.nomeResponsavel.isBlank()) {
            _uiState.value = state.copy(erro = "Informe o nome do responsável")
            return
        }

        val cns = state.numeroCns.trim().ifEmpty { null }
        if (cns != null && cns.length != 15) {
            _uiState.value = state.copy(erro = "O CNS deve conter exatamente 15 dígitos")
            return
        }

        _uiState.value = state.copy(salvando = true, erro = null)

        viewModelScope.launch {
            if (cns != null) {
                val existente = criancaRepository.buscarCriancaPorCns(cns)
                if (existente != null && existente.id != criancaId) {
                    _uiState.value = _uiState.value.copy(
                        salvando = false,
                        erro = "Este CNS já está cadastrado para outra criança (${existente.nome})"
                    )
                    return@launch
                }
            }

            val criancaAtual = criancaRepository.buscarCriancaPorId(criancaId)
            if (criancaAtual == null) {
                _uiState.value = _uiState.value.copy(salvando = false, erro = "Criança não encontrada")
                return@launch
            }

            val comunidadeId = territorioRepository.obterOuCriarComunidade(
                poloBaseId = poloBaseId,
                nomeComunidade = state.comunidadeNome.trim()
            )

            criancaRepository.atualizarCrianca(
                criancaAtual.copy(
                    nome = state.nome.trim(),
                    sexo = state.sexo,
                    dataNascimento = dataNascTimestamp,
                    nomeResponsavel = state.nomeResponsavel.trim(),
                    numeroCns = cns,
                    comunidadeId = comunidadeId
                )
            )

            _uiState.value = _uiState.value.copy(salvando = false)
            onSucesso()
        }
    }
}
