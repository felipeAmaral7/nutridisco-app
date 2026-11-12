package com.nutridisco.app.presentation.editaravaliacao

import androidx.lifecycle.SavedStateHandle
import com.nutridisco.app.data.local.entity.AvaliacaoEntity
import com.nutridisco.app.data.local.entity.CriancaEntity
import com.nutridisco.app.data.local.model.AvaliacaoDetalhada
import com.nutridisco.app.data.local.model.CriancaComComunidade
import com.nutridisco.app.domain.model.ClassificacaoEstatura
import com.nutridisco.app.domain.model.ClassificacaoPeso
import com.nutridisco.app.data.local.entity.ReferenciaAntropometricaEntity
import com.nutridisco.app.domain.repository.ReferenciaAntropometricaRepository
import com.nutridisco.app.domain.repository.AvaliacaoRepository
import com.nutridisco.app.domain.repository.CriancaRepository
import com.nutridisco.app.domain.usecase.CalcularClassificacaoNutricionalUseCase
import com.nutridisco.app.domain.usecase.ReferenciaNaoEncontradaException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditarAvaliacaoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val avaliacaoId = 100L
    private val criancaId = 10L

    private val criancaMock = CriancaEntity(
        id = criancaId,
        nome = "Davi Yanomami",
        sexo = "M",
        dataNascimento = 1000L,
        nomeResponsavel = "Raoni",
        numeroCns = "123456789012345",
        comunidadeId = 1L
    )

    private var avaliacaoMock = AvaliacaoEntity(
        id = avaliacaoId,
        criancaId = criancaId,
        comunidadeAtendimentoId = 1L,
        dataAtendimento = 5000L,
        idadeMesesMomento = 12,
        peso = 9.5,
        estatura = 75.0,
        classificacaoEstatura = ClassificacaoEstatura.ESTATURA_ADEQUADA,
        classificacaoPeso = ClassificacaoPeso.PESO_ADEQUADO,
        observacoes = "Primeira consulta",
        nomeProfissional = "Enf. Teste",
        registroCoren = "COREN-RR 123"
    )

    private var avaliacaoAtualizada: AvaliacaoEntity? = null
    private var deveLancarExcecaoReferencia = false

    private val avaliacaoRepo = object : AvaliacaoRepository {
        override fun listarPorCrianca(criancaId: Long): Flow<List<AvaliacaoEntity>> = throw NotImplementedError()
        override fun buscarUltimaAvaliacao(criancaId: Long): Flow<AvaliacaoEntity?> = throw NotImplementedError()
        override suspend fun buscarPorId(id: Long): AvaliacaoEntity? = if (id == avaliacaoId) avaliacaoMock else null
        override fun listarTodasDetalhadasFlow(): Flow<List<AvaliacaoDetalhada>> = throw NotImplementedError()
        override suspend fun listarTodasParaExportacao(): List<AvaliacaoDetalhada> = emptyList()
        override suspend fun salvarAvaliacao(avaliacao: AvaliacaoEntity): Long = 1L
        override suspend fun atualizarAvaliacao(avaliacao: AvaliacaoEntity) {
            avaliacaoAtualizada = avaliacao
            avaliacaoMock = avaliacao
        }
        override suspend fun deletarAvaliacao(avaliacao: AvaliacaoEntity) {}
    }

    private val criancaRepo = object : CriancaRepository {
        override fun listarCriancas(): Flow<List<CriancaComComunidade>> = throw NotImplementedError()
        override fun buscarCriancaPorIdFlow(id: Long): Flow<CriancaComComunidade?> = throw NotImplementedError()
        override suspend fun buscarCriancaPorId(id: Long): CriancaEntity? = if (id == criancaId) criancaMock else null
        override suspend fun buscarCriancaPorCns(cns: String): CriancaEntity? = null
        override fun pesquisarCriancas(termo: String): Flow<List<CriancaComComunidade>> = throw NotImplementedError()
        override suspend fun salvarCrianca(crianca: CriancaEntity): Long = 1L
        override suspend fun atualizarCrianca(crianca: CriancaEntity) {}
        override suspend fun deletarCrianca(crianca: CriancaEntity) {}
    }

    private val fakeReferenciaRepo = object : ReferenciaAntropometricaRepository {
        var retornarReferencia: Boolean = true

        override suspend fun buscarReferencia(
            tipoParametro: String,
            sexo: String,
            idadeMeses: Int
        ): ReferenciaAntropometricaEntity? {
            if (!retornarReferencia) return null
            return when (tipoParametro) {
                "ESTATURA" -> ReferenciaAntropometricaEntity(
                    id = 1,
                    tipoParametro = "ESTATURA",
                    sexo = sexo,
                    idadeMeses = idadeMeses,
                    escoreMenos3 = 70.0,
                    escoreMenos2 = 75.0,
                    escoreMais2 = 85.0
                )
                "PESO" -> ReferenciaAntropometricaEntity(
                    id = 2,
                    tipoParametro = "PESO",
                    sexo = sexo,
                    idadeMeses = idadeMeses,
                    escoreMenos3 = 8.0,
                    escoreMenos2 = 9.0,
                    escoreMais2 = 13.0
                )
                else -> null
            }
        }

        override suspend fun inserirTodas(referencias: List<ReferenciaAntropometricaEntity>) {}
        override suspend fun contar(): Int = 2
    }

    private val useCase = CalcularClassificacaoNutricionalUseCase(fakeReferenciaRepo)

    private fun criarViewModel() = EditarAvaliacaoViewModel(
        savedStateHandle = SavedStateHandle(mapOf("avaliacaoId" to avaliacaoId)),
        avaliacaoRepository = avaliacaoRepo,
        criancaRepository = criancaRepo,
        calcularClassificacaoUseCase = useCase
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        avaliacaoAtualizada = null
        fakeReferenciaRepo.retornarReferencia = true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deve carregar dados da avaliacao ao inicializar`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertEquals("Davi Yanomami", state.criancaNome)
        assertEquals("M", state.criancaSexo)
        assertEquals(12, state.idadeMesesMomento)
        assertEquals("9,5", state.peso)
        assertEquals("75,0", state.estatura)
        assertEquals("Primeira consulta", state.observacoes)
        assertNull(state.erro)
    }

    @Test
    fun `deve bloquear edicao quando peso for invalido`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()

        viewModel.onPesoChange("0")
        var sucesso = false
        viewModel.salvar { sucesso = true }
        advanceUntilIdle()

        assertFalse(sucesso)
        assertEquals("Informe um peso válido (ex: 10,5 kg)", viewModel.uiState.value.erro)
        assertNull(avaliacaoAtualizada)
    }

    @Test
    fun `deve bloquear edicao quando estatura for invalida`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()

        viewModel.onEstaturaChange("200") // > 150.0
        var sucesso = false
        viewModel.salvar { sucesso = true }
        advanceUntilIdle()

        assertFalse(sucesso)
        assertEquals("Informe uma estatura válida (ex: 80,0 cm)", viewModel.uiState.value.erro)
        assertNull(avaliacaoAtualizada)
    }

    @Test
    fun `deve recalcular classificacao e atualizar avaliacao com sucesso`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()

        viewModel.onPesoChange("16,0")
        viewModel.onEstaturaChange("72,0")
        viewModel.onObservacoesChange("Dados corrigidos após conferência da caderneta")

        var sucesso = false
        viewModel.salvar { sucesso = true }
        advanceUntilIdle()

        assertTrue(sucesso)
        assertNull(viewModel.uiState.value.erro)
        assertEquals(16.0, avaliacaoAtualizada?.peso)
        assertEquals(72.0, avaliacaoAtualizada?.estatura)
        assertEquals(ClassificacaoPeso.PESO_ELEVADO, avaliacaoAtualizada?.classificacaoPeso)
        assertEquals(ClassificacaoEstatura.BAIXA_ESTATURA, avaliacaoAtualizada?.classificacaoEstatura)
        assertEquals("Dados corrigidos após conferência da caderneta", avaliacaoAtualizada?.observacoes)
    }

    @Test
    fun `deve tratar erro quando referencia OMS nao for encontrada`() = runTest(testDispatcher) {
        fakeReferenciaRepo.retornarReferencia = false
        val viewModel = criarViewModel()
        advanceUntilIdle()

        viewModel.onPesoChange("10,0")
        viewModel.onEstaturaChange("75,0")

        var sucesso = false
        viewModel.salvar { sucesso = true }
        advanceUntilIdle()

        assertFalse(sucesso)
        assertTrue(viewModel.uiState.value.erro?.contains("Referência OMS indisponível") == true)
        assertNull(avaliacaoAtualizada)
    }
}
