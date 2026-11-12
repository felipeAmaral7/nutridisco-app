package com.nutridisco.app.presentation.avaliacoessalvas

import com.nutridisco.app.data.local.entity.AvaliacaoEntity
import com.nutridisco.app.data.local.model.AvaliacaoDetalhada
import com.nutridisco.app.domain.model.ClassificacaoEstatura
import com.nutridisco.app.domain.model.ClassificacaoPeso
import com.nutridisco.app.domain.repository.AvaliacaoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AvaliacoesSalvasViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val avaliacoesFlow = MutableStateFlow<List<AvaliacaoDetalhada>>(emptyList())

    private val avaliacaoRepo = object : AvaliacaoRepository {
        override fun listarPorCrianca(criancaId: Long): Flow<List<AvaliacaoEntity>> = throw NotImplementedError()
        override fun buscarUltimaAvaliacao(criancaId: Long): Flow<AvaliacaoEntity?> = throw NotImplementedError()
        override fun listarTodasDetalhadasFlow(): Flow<List<AvaliacaoDetalhada>> = avaliacoesFlow
        override suspend fun listarTodasParaExportacao(): List<AvaliacaoDetalhada> = avaliacoesFlow.value
        override suspend fun salvarAvaliacao(avaliacao: AvaliacaoEntity): Long = 1L
        override suspend fun deletarAvaliacao(avaliacao: AvaliacaoEntity) {}
    }

    private fun criarItem(
        id: Long,
        nome: String,
        comunidade: String = "Maturuca",
        polo: String = "Surucucu",
        data: Long
    ) = AvaliacaoDetalhada(
        avaliacaoId = id,
        poloBaseNome = polo,
        comunidadeNome = comunidade,
        nomeProfissional = "Enf. Teste",
        registroCoren = "123",
        criancaId = id * 10,
        criancaNome = nome,
        sexo = "F",
        dataNascimento = 0L,
        dataAtendimento = data,
        idadeMesesMomento = 12,
        peso = 9.5,
        estatura = 75.0,
        classificacaoEstatura = ClassificacaoEstatura.ESTATURA_ADEQUADA,
        classificacaoPeso = ClassificacaoPeso.PESO_ADEQUADO,
        observacoes = null
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        avaliacoesFlow.value = listOf(
            criarItem(id = 1L, nome = "Zulmira Yanomami", data = 1000L),
            criarItem(id = 2L, nome = "Ana Macuxi", data = 3000L),
            criarItem(id = 3L, nome = "Bernardo Wapichana", data = 2000L)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ordem padrao deve ser mais recentes primeiro`() = runTest(testDispatcher) {
        val viewModel = AvaliacoesSalvasViewModel(avaliacaoRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.avaliacoes.collect() }
        advanceUntilIdle()

        assertEquals(TipoOrdenacao.RECENTES, viewModel.ordenacao.value)
        val nomes = viewModel.avaliacoes.value.map { it.criancaNome }
        assertEquals(listOf("Ana Macuxi", "Bernardo Wapichana", "Zulmira Yanomami"), nomes)
    }

    @Test
    fun `alternar ordenacao deve mudar para alfabetica e ordenar nomes A-Z`() = runTest(testDispatcher) {
        val viewModel = AvaliacoesSalvasViewModel(avaliacaoRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.avaliacoes.collect() }
        advanceUntilIdle()

        viewModel.alternarOrdenacao()
        advanceUntilIdle()

        assertEquals(TipoOrdenacao.ALFABETICA, viewModel.ordenacao.value)
        val nomes = viewModel.avaliacoes.value.map { it.criancaNome }
        assertEquals(listOf("Ana Macuxi", "Bernardo Wapichana", "Zulmira Yanomami"), nomes)

        // Adiciona itens desordenados alfabeticamente para provar ordenação
        avaliacoesFlow.value = listOf(
            criarItem(id = 1L, nome = "Carlos", data = 100L),
            criarItem(id = 2L, nome = "Ana", data = 200L),
            criarItem(id = 3L, nome = "Bruno", data = 300L)
        )
        advanceUntilIdle()

        val nomesOrdenados = viewModel.avaliacoes.value.map { it.criancaNome }
        assertEquals(listOf("Ana", "Bruno", "Carlos"), nomesOrdenados)
    }

    @Test
    fun `alternar ordenacao duas vezes deve voltar para recentes`() = runTest(testDispatcher) {
        val viewModel = AvaliacoesSalvasViewModel(avaliacaoRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.avaliacoes.collect() }
        advanceUntilIdle()

        viewModel.alternarOrdenacao()
        assertEquals(TipoOrdenacao.ALFABETICA, viewModel.ordenacao.value)

        viewModel.alternarOrdenacao()
        assertEquals(TipoOrdenacao.RECENTES, viewModel.ordenacao.value)
    }

    @Test
    fun `busca por termo deve filtrar criancas e manter ordenacao ativa`() = runTest(testDispatcher) {
        val viewModel = AvaliacoesSalvasViewModel(avaliacaoRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.avaliacoes.collect() }
        advanceUntilIdle()

        viewModel.onTermoBuscaChange("Macuxi")
        advanceUntilIdle()

        assertEquals(1, viewModel.avaliacoes.value.size)
        assertEquals("Ana Macuxi", viewModel.avaliacoes.value.first().criancaNome)

        viewModel.limparTermoBusca()
        advanceUntilIdle()

        assertEquals(3, viewModel.avaliacoes.value.size)
    }
}
