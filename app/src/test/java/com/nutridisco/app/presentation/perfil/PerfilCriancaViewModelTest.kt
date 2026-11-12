package com.nutridisco.app.presentation.perfil

import androidx.lifecycle.SavedStateHandle
import com.nutridisco.app.data.local.entity.AvaliacaoEntity
import com.nutridisco.app.data.local.entity.CriancaEntity
import com.nutridisco.app.data.local.model.CriancaComComunidade
import com.nutridisco.app.domain.model.ClassificacaoEstatura
import com.nutridisco.app.domain.model.ClassificacaoPeso
import com.nutridisco.app.domain.repository.AvaliacaoRepository
import com.nutridisco.app.domain.repository.CriancaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PerfilCriancaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val criancaId = 10L
    private val criancaMock = CriancaComComunidade(
        id = criancaId,
        nome = "Davi Yanomami",
        sexo = "M",
        dataNascimento = 1000L,
        nomeResponsavel = "Raoni",
        numeroCns = "123456789012345",
        comunidadeId = 1L,
        comunidadeNome = "Watoriki",
        poloBaseId = 2L,
        poloBaseNome = "Demini"
    )

    private val criancasDeletadas = mutableListOf<CriancaEntity>()
    private val avaliacoesDeletadas = mutableListOf<AvaliacaoEntity>()

    private val avaliacoesFlow = MutableStateFlow(
        listOf(
            AvaliacaoEntity(
                id = 100L,
                criancaId = criancaId,
                comunidadeAtendimentoId = 1L,
                dataAtendimento = 3000L,
                idadeMesesMomento = 12,
                peso = 10.0,
                estatura = 75.0,
                classificacaoEstatura = ClassificacaoEstatura.ESTATURA_ADEQUADA,
                classificacaoPeso = ClassificacaoPeso.PESO_ADEQUADO,
                observacoes = null,
                nomeProfissional = "Enf. Teste",
                registroCoren = "123"
            )
        )
    )

    private val criancaRepo = object : CriancaRepository {
        override fun listarCriancas(): Flow<List<CriancaComComunidade>> = throw NotImplementedError()
        override fun buscarCriancaPorIdFlow(id: Long): Flow<CriancaComComunidade?> = MutableStateFlow(criancaMock)
        override suspend fun buscarCriancaPorId(id: Long): CriancaEntity? = CriancaEntity(
            id = criancaMock.id,
            nome = criancaMock.nome,
            sexo = criancaMock.sexo,
            dataNascimento = criancaMock.dataNascimento,
            nomeResponsavel = criancaMock.nomeResponsavel,
            numeroCns = criancaMock.numeroCns,
            comunidadeId = criancaMock.comunidadeId
        )
        override suspend fun buscarCriancaPorCns(cns: String): CriancaEntity? = null
        override fun pesquisarCriancas(termo: String): Flow<List<CriancaComComunidade>> = throw NotImplementedError()
        override suspend fun salvarCrianca(crianca: CriancaEntity): Long = 1L
        override suspend fun atualizarCrianca(crianca: CriancaEntity) {}
        override suspend fun deletarCrianca(crianca: CriancaEntity) {
            criancasDeletadas += crianca
        }
    }

    private val avaliacaoRepo = object : AvaliacaoRepository {
        override fun listarPorCrianca(criancaId: Long): Flow<List<AvaliacaoEntity>> = avaliacoesFlow
        override fun buscarUltimaAvaliacao(criancaId: Long): Flow<AvaliacaoEntity?> = throw NotImplementedError()
        override fun listarTodasDetalhadasFlow() = throw NotImplementedError()
        override suspend fun listarTodasParaExportacao() = emptyList<com.nutridisco.app.data.local.model.AvaliacaoDetalhada>()
        override suspend fun salvarAvaliacao(avaliacao: AvaliacaoEntity): Long = 1L
        override suspend fun deletarAvaliacao(avaliacao: AvaliacaoEntity) {
            avaliacoesDeletadas += avaliacao
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        criancasDeletadas.clear()
        avaliacoesDeletadas.clear()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deve carregar crianca e lista de avaliacoes`() = runTest(testDispatcher) {
        val viewModel = PerfilCriancaViewModel(
            savedStateHandle = SavedStateHandle(mapOf("criancaId" to criancaId)),
            criancaRepository = criancaRepo,
            avaliacaoRepository = avaliacaoRepo
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.crianca.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.avaliacoes.collect() }
        advanceUntilIdle()

        assertNotNull(viewModel.crianca.value)
        assertEquals("Davi Yanomami", viewModel.crianca.value?.nome)
        assertEquals(1, viewModel.avaliacoes.value.size)
        assertEquals(100L, viewModel.avaliacoes.value.first().id)
    }

    @Test
    fun `excluirAvaliacao deve delegar ao repositorio`() = runTest(testDispatcher) {
        val viewModel = PerfilCriancaViewModel(
            savedStateHandle = SavedStateHandle(mapOf("criancaId" to criancaId)),
            criancaRepository = criancaRepo,
            avaliacaoRepository = avaliacaoRepo
        )
        val avaliacao = avaliacoesFlow.value.first()
        viewModel.excluirAvaliacao(avaliacao)
        advanceUntilIdle()

        assertEquals(1, avaliacoesDeletadas.size)
        assertEquals(100L, avaliacoesDeletadas.first().id)
    }

    @Test
    fun `excluirCrianca deve deletar crianca e chamar callback de sucesso`() = runTest(testDispatcher) {
        val viewModel = PerfilCriancaViewModel(
            savedStateHandle = SavedStateHandle(mapOf("criancaId" to criancaId)),
            criancaRepository = criancaRepo,
            avaliacaoRepository = avaliacaoRepo
        )
        var callbackChamado = false
        viewModel.excluirCrianca { callbackChamado = true }
        advanceUntilIdle()

        assertTrue(callbackChamado)
        assertEquals(1, criancasDeletadas.size)
        assertEquals(criancaId, criancasDeletadas.first().id)
    }
}
