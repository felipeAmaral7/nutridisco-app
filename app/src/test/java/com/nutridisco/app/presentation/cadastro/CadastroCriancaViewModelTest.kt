package com.nutridisco.app.presentation.cadastro

import com.nutridisco.app.data.local.entity.AvaliacaoEntity
import com.nutridisco.app.data.local.entity.ComunidadeEntity
import com.nutridisco.app.data.local.entity.ConfiguracaoDispositivoEntity
import com.nutridisco.app.data.local.entity.CriancaEntity
import com.nutridisco.app.data.local.entity.PoloBaseEntity
import com.nutridisco.app.data.local.entity.ReferenciaAntropometricaEntity
import com.nutridisco.app.data.local.model.AvaliacaoDetalhada
import com.nutridisco.app.data.local.model.CriancaComComunidade
import com.nutridisco.app.domain.repository.AvaliacaoRepository
import com.nutridisco.app.domain.repository.ConfiguracaoRepository
import com.nutridisco.app.domain.repository.CriancaRepository
import com.nutridisco.app.domain.repository.ReferenciaAntropometricaRepository
import com.nutridisco.app.domain.repository.TerritorioRepository
import com.nutridisco.app.domain.usecase.CalcularClassificacaoNutricionalUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class CadastroCriancaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private var configuracao: ConfiguracaoDispositivoEntity? = null
    private val criancasSalvas = mutableListOf<CriancaEntity>()
    private val avaliacoesSalvas = mutableListOf<AvaliacaoEntity>()
    private val comunidadesCriadas = mutableListOf<Pair<Long, String>>()

    private val configuracaoRepo = object : ConfiguracaoRepository {
        override fun getConfiguracaoFlow(): Flow<ConfiguracaoDispositivoEntity?> = flowOf(configuracao)
        override suspend fun getConfiguracao() = configuracao
        override suspend fun salvarConfiguracao(configuracao: ConfiguracaoDispositivoEntity) {}
    }

    private val criancaRepo = object : CriancaRepository {
        override fun listarCriancas(): Flow<List<CriancaComComunidade>> = flowOf(emptyList())
        override fun buscarCriancaPorIdFlow(id: Long): Flow<CriancaComComunidade?> = flowOf(null)
        override suspend fun buscarCriancaPorId(id: Long): CriancaEntity? = null
        override suspend fun buscarCriancaPorCns(cns: String): CriancaEntity? = null
        override fun pesquisarCriancas(termo: String): Flow<List<CriancaComComunidade>> = flowOf(emptyList())
        override suspend fun salvarCrianca(crianca: CriancaEntity): Long {
            criancasSalvas += crianca
            return criancasSalvas.size.toLong()
        }
        override suspend fun atualizarCrianca(crianca: CriancaEntity) {}
        override suspend fun deletarCrianca(crianca: CriancaEntity) {}
    }

    private val territorioRepo = object : TerritorioRepository {
        override fun listarPolosBase(): Flow<List<PoloBaseEntity>> = flowOf(emptyList())
        override suspend fun buscarPoloBasePorId(id: Long): PoloBaseEntity? = null
        override suspend fun inserirPolosBase(polos: List<PoloBaseEntity>) {}
        override fun listarComunidadesPorPolo(poloBaseId: Long): Flow<List<ComunidadeEntity>> = flowOf(emptyList())
        override fun listarTodasComunidades(): Flow<List<ComunidadeEntity>> = flowOf(emptyList())
        override fun buscarComunidadesPorTermo(termo: String): Flow<List<ComunidadeEntity>> = flowOf(emptyList())
        override suspend fun obterOuCriarComunidade(poloBaseId: Long, nomeComunidade: String): Long {
            comunidadesCriadas += poloBaseId to nomeComunidade
            return 10L
        }
        override suspend fun buscarComunidadePorId(id: Long): ComunidadeEntity? = null
        override suspend fun inserirComunidades(comunidades: List<ComunidadeEntity>) {}
    }

    private val avaliacaoRepo = object : AvaliacaoRepository {
        override fun listarPorCrianca(criancaId: Long): Flow<List<AvaliacaoEntity>> = flowOf(emptyList())
        override fun buscarUltimaAvaliacao(criancaId: Long): Flow<AvaliacaoEntity?> = flowOf(null)
        override fun listarTodasDetalhadasFlow(): Flow<List<AvaliacaoDetalhada>> = flowOf(emptyList())
        override suspend fun listarTodasParaExportacao(): List<AvaliacaoDetalhada> = emptyList()
        override suspend fun salvarAvaliacao(avaliacao: AvaliacaoEntity): Long {
            avaliacoesSalvas += avaliacao
            return avaliacoesSalvas.size.toLong()
        }
        override suspend fun deletarAvaliacao(avaliacao: AvaliacaoEntity) {}
    }

    private val referenciaRepo = object : ReferenciaAntropometricaRepository {
        override suspend fun buscarReferencia(tipoParametro: String, sexo: String, idadeMeses: Int) =
            ReferenciaAntropometricaEntity(
                tipoParametro = tipoParametro,
                sexo = sexo,
                idadeMeses = idadeMeses,
                escoreMenos3 = if (tipoParametro == "PESO") 7.0 else 70.0,
                escoreMenos2 = if (tipoParametro == "PESO") 8.0 else 74.0,
                escoreMais2 = if (tipoParametro == "PESO") 15.0 else null
            )
        override suspend fun inserirTodas(referencias: List<ReferenciaAntropometricaEntity>) {}
        override suspend fun contar(): Int = 1
    }

    private fun criarViewModel() = CadastroCriancaViewModel(
        criancaRepository = criancaRepo,
        territorioRepository = territorioRepo,
        configuracaoRepository = configuracaoRepo,
        avaliacaoRepository = avaliacaoRepo,
        calcularClassificacaoUseCase = CalcularClassificacaoNutricionalUseCase(referenciaRepo)
    )

    /** Nascimento há 21 meses, para ficar dentro da faixa de 0 a 59 meses em qualquer data de execução. */
    private val dataNascimento21Meses: Long =
        LocalDate.now().minusMonths(21).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun CadastroCriancaViewModel.preencherFormulario() {
        preencherDadosDaAvaliacao("F", dataNascimento21Meses)
        onNomeChange("Ana")
        onComunidadeNomeChange("Maturuca")
        onNomeResponsavelChange("Maria")
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        configuracao = ConfiguracaoDispositivoEntity(
            nomeProfissional = "Enf. Teste",
            registroCoren = "123",
            poloBaseIdPadrao = 5L
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `salvar registro deve usar o polo base padrao e gravar as observacoes`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()
        viewModel.preencherFormulario()
        viewModel.onObservacoesChange("  Criança com diarreia há 3 dias  ")

        var criancaId: Long? = null
        viewModel.salvar(dadosAvaliacaoPendente = 10.5 to 80.0, onSucesso = { criancaId = it })
        advanceUntilIdle()

        assertEquals(1L, criancaId)
        assertEquals(listOf(5L to "Maturuca"), comunidadesCriadas)
        assertEquals("F", criancasSalvas.single().sexo)
        val avaliacao = avaliacoesSalvas.single()
        assertEquals("Criança com diarreia há 3 dias", avaliacao.observacoes)
        assertEquals(21, avaliacao.idadeMesesMomento)
        assertEquals(10.5, avaliacao.peso, 0.0)
        assertEquals(80.0, avaliacao.estatura, 0.0)
    }

    @Test
    fun `observacoes em branco devem ser gravadas como nulas`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()
        viewModel.preencherFormulario()
        viewModel.onObservacoesChange("   ")

        viewModel.salvar(dadosAvaliacaoPendente = 10.5 to 80.0, onSucesso = {})
        advanceUntilIdle()

        assertNull(avaliacoesSalvas.single().observacoes)
    }

    @Test
    fun `sem polo base padrao deve avisar e nao gravar nada`() = runTest(testDispatcher) {
        configuracao = configuracao?.copy(poloBaseIdPadrao = null)
        val viewModel = criarViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.poloBasePadraoAusente)

        viewModel.preencherFormulario()
        viewModel.salvar(dadosAvaliacaoPendente = 10.5 to 80.0, onSucesso = {})
        advanceUntilIdle()

        assertEquals(CadastroCriancaViewModel.ERRO_SEM_POLO_PADRAO, viewModel.uiState.value.erro)
        assertTrue(criancasSalvas.isEmpty())
        assertTrue(avaliacoesSalvas.isEmpty())
    }

    @Test
    fun `sexo e data vindos da avaliacao nao podem ser alterados`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()
        viewModel.preencherDadosDaAvaliacao("F", dataNascimento21Meses)
        val dataOriginal = viewModel.uiState.value.dataNascimentoTexto

        viewModel.onSexoChange("M")
        viewModel.onDataNascimentoChange("01012020")

        assertEquals("F", viewModel.uiState.value.sexo)
        assertEquals(dataOriginal, viewModel.uiState.value.dataNascimentoTexto)
        assertFalse(viewModel.uiState.value.poloBasePadraoAusente)
    }
}
