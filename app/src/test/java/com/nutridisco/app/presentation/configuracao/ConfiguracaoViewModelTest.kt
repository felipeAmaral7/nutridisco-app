package com.nutridisco.app.presentation.configuracao

import com.nutridisco.app.data.local.entity.ComunidadeEntity
import com.nutridisco.app.data.local.entity.ConfiguracaoDispositivoEntity
import com.nutridisco.app.data.local.entity.PoloBaseEntity
import com.nutridisco.app.domain.repository.ConfiguracaoRepository
import com.nutridisco.app.domain.repository.TerritorioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
class ConfiguracaoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val configuracaoFlow = MutableStateFlow<ConfiguracaoDispositivoEntity?>(null)
    private var configuracaoSalva: ConfiguracaoDispositivoEntity? = null

    private val configuracaoRepo = object : ConfiguracaoRepository {
        override fun getConfiguracaoFlow(): Flow<ConfiguracaoDispositivoEntity?> = configuracaoFlow
        override suspend fun getConfiguracao(): ConfiguracaoDispositivoEntity? = configuracaoFlow.value
        override suspend fun salvarConfiguracao(configuracao: ConfiguracaoDispositivoEntity) {
            configuracaoSalva = configuracao
            configuracaoFlow.value = configuracao
        }
    }

    private val territorioRepo = object : TerritorioRepository {
        override fun listarPolosBase(): Flow<List<PoloBaseEntity>> = flowOf(
            listOf(
                PoloBaseEntity(id = 1L, nome = "Surucucu", etnorregiao = "Yanomami"),
                PoloBaseEntity(id = 2L, nome = "Auaris", etnorregiao = "Sanöma")
            )
        )
        override suspend fun buscarPoloBasePorId(id: Long): PoloBaseEntity? = null
        override suspend fun inserirPolosBase(polos: List<PoloBaseEntity>) {}
        override fun listarComunidadesPorPolo(poloBaseId: Long): Flow<List<ComunidadeEntity>> = flowOf(emptyList())
        override fun listarTodasComunidades(): Flow<List<ComunidadeEntity>> = flowOf(emptyList())
        override fun buscarComunidadesPorTermo(termo: String): Flow<List<ComunidadeEntity>> = flowOf(emptyList())
        override suspend fun obterOuCriarComunidade(poloBaseId: Long, nomeComunidade: String): Long = 1L
        override suspend fun buscarComunidadePorId(id: Long): ComunidadeEntity? = null
        override suspend fun inserirComunidades(comunidades: List<ComunidadeEntity>) {}
    }

    private fun criarViewModel() = ConfiguracaoViewModel(
        configuracaoRepository = configuracaoRepo,
        territorioRepository = territorioRepo
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        configuracaoFlow.value = null
        configuracaoSalva = null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ao inicializar com configuracao existente deve preencher uiState`() = runTest(testDispatcher) {
        configuracaoFlow.value = ConfiguracaoDispositivoEntity(
            nomeProfissional = "Enf. Ana",
            registroCoren = "COREN-RR 9999",
            poloBaseIdPadrao = 2L
        )

        val viewModel = criarViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Enf. Ana", state.nomeProfissional)
        assertEquals("COREN-RR 9999", state.registroCoren)
        assertEquals(2L, state.poloBaseIdPadrao)
        assertNull(state.erro)
    }

    @Test
    fun `deve bloquear salvamento quando nome do profissional esta em branco`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()

        viewModel.onNomeProfissionalChange("   ")
        viewModel.onRegistroCorenChange("COREN-RR 12345")
        viewModel.onPoloBaseSelecionado(1L)

        var sucessoChamado = false
        viewModel.salvarConfiguracao { sucessoChamado = true }
        advanceUntilIdle()

        assertFalse(sucessoChamado)
        assertEquals("Informe o nome do profissional", viewModel.uiState.value.erro)
        assertNull(configuracaoSalva)
    }

    @Test
    fun `deve bloquear salvamento quando registro COREN esta em branco`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()

        viewModel.onNomeProfissionalChange("Enf. Carlos")
        viewModel.onRegistroCorenChange("   ")
        viewModel.onPoloBaseSelecionado(1L)

        var sucessoChamado = false
        viewModel.salvarConfiguracao { sucessoChamado = true }
        advanceUntilIdle()

        assertFalse(sucessoChamado)
        assertEquals("Informe o registro COREN", viewModel.uiState.value.erro)
        assertNull(configuracaoSalva)
    }

    @Test
    fun `deve bloquear salvamento quando polo base padrao nao foi selecionado`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()

        viewModel.onNomeProfissionalChange("Enf. Carlos")
        viewModel.onRegistroCorenChange("COREN-RR 12345")
        // poloBaseIdPadrao permanece null

        var sucessoChamado = false
        viewModel.salvarConfiguracao { sucessoChamado = true }
        advanceUntilIdle()

        assertFalse(sucessoChamado)
        assertEquals("Selecione o Polo Base padrão", viewModel.uiState.value.erro)
        assertNull(configuracaoSalva)
    }

    @Test
    fun `deve salvar configuracao com sucesso quando todos os campos obrigatorios estao preenchidos`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()

        viewModel.onNomeProfissionalChange("  Enf. Joana  ")
        viewModel.onRegistroCorenChange("  COREN-RR 78910  ")
        viewModel.onPoloBaseSelecionado(1L)

        var sucessoChamado = false
        viewModel.salvarConfiguracao { sucessoChamado = true }
        advanceUntilIdle()

        assertTrue(sucessoChamado)
        assertTrue(viewModel.uiState.value.salvoComSucesso)
        assertNull(viewModel.uiState.value.erro)

        assertEquals("Enf. Joana", configuracaoSalva?.nomeProfissional)
        assertEquals("COREN-RR 78910", configuracaoSalva?.registroCoren)
        assertEquals(1L, configuracaoSalva?.poloBaseIdPadrao)
    }

    @Test
    fun `alterar campos deve limpar mensagem de erro e estado de sucesso`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()

        viewModel.salvarConfiguracao {}
        advanceUntilIdle()
        assertEquals("Informe o nome do profissional", viewModel.uiState.value.erro)

        viewModel.onNomeProfissionalChange("Enf. Joana")
        assertNull(viewModel.uiState.value.erro)
        assertFalse(viewModel.uiState.value.salvoComSucesso)
    }
}
