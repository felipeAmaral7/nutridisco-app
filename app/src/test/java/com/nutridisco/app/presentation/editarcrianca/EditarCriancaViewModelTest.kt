package com.nutridisco.app.presentation.editarcrianca

import androidx.lifecycle.SavedStateHandle
import com.nutridisco.app.data.local.entity.ComunidadeEntity
import com.nutridisco.app.data.local.entity.CriancaEntity
import com.nutridisco.app.data.local.entity.PoloBaseEntity
import com.nutridisco.app.data.local.model.CriancaComComunidade
import com.nutridisco.app.domain.repository.CriancaRepository
import com.nutridisco.app.domain.repository.TerritorioRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class EditarCriancaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val criancaId = 10L
    private val dataNascimentoTimestamp = com.nutridisco.app.domain.util.DataUtils.parse("01/01/2025")!!

    private var criancaMock = CriancaEntity(
        id = criancaId,
        nome = "Davi Yanomami",
        sexo = "M",
        dataNascimento = dataNascimentoTimestamp,
        nomeResponsavel = "Raoni",
        numeroCns = "123456789012345",
        comunidadeId = 1L
    )

    private var criancaAtualizada: CriancaEntity? = null
    private val outraCriancaComCns = CriancaEntity(
        id = 99L,
        nome = "Outra Criança",
        sexo = "F",
        dataNascimento = dataNascimentoTimestamp,
        nomeResponsavel = "Maria",
        numeroCns = "999999999999999",
        comunidadeId = 1L
    )

    private val criancaRepo = object : CriancaRepository {
        override fun listarCriancas(): Flow<List<CriancaComComunidade>> = throw NotImplementedError()
        override fun buscarCriancaPorIdFlow(id: Long): Flow<CriancaComComunidade?> = throw NotImplementedError()
        override suspend fun buscarCriancaPorId(id: Long): CriancaEntity? = if (id == criancaId) criancaMock else null
        override suspend fun buscarCriancaPorCns(cns: String): CriancaEntity? = when (cns) {
            criancaMock.numeroCns -> criancaMock
            outraCriancaComCns.numeroCns -> outraCriancaComCns
            else -> null
        }
        override fun pesquisarCriancas(termo: String): Flow<List<CriancaComComunidade>> = throw NotImplementedError()
        override suspend fun salvarCrianca(crianca: CriancaEntity): Long = 1L
        override suspend fun atualizarCrianca(crianca: CriancaEntity) {
            criancaAtualizada = crianca
            criancaMock = crianca
        }
        override suspend fun deletarCrianca(crianca: CriancaEntity) {}
    }

    private val territorioRepo = object : TerritorioRepository {
        override fun listarPolosBase(): Flow<List<PoloBaseEntity>> = flowOf(
            listOf(PoloBaseEntity(id = 2L, nome = "Demini", etnorregiao = "Yanomami"))
        )
        override suspend fun buscarPoloBasePorId(id: Long): PoloBaseEntity? = null
        override suspend fun inserirPolosBase(polos: List<PoloBaseEntity>) {}
        override fun listarComunidadesPorPolo(poloBaseId: Long): Flow<List<ComunidadeEntity>> = flowOf(emptyList())
        override fun listarTodasComunidades(): Flow<List<ComunidadeEntity>> = flowOf(emptyList())
        override fun buscarComunidadesPorTermo(termo: String): Flow<List<ComunidadeEntity>> = flowOf(emptyList())
        override suspend fun obterOuCriarComunidade(poloBaseId: Long, nomeComunidade: String): Long = 1L
        override suspend fun buscarComunidadePorId(id: Long): ComunidadeEntity? = ComunidadeEntity(
            id = 1L,
            poloBaseId = 2L,
            nome = "Watoriki"
        )
        override suspend fun inserirComunidades(comunidades: List<ComunidadeEntity>) {}
    }

    private fun criarViewModel() = EditarCriancaViewModel(
        savedStateHandle = SavedStateHandle(mapOf("criancaId" to criancaId)),
        criancaRepository = criancaRepo,
        territorioRepository = territorioRepo
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        criancaAtualizada = null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deve carregar dados da crianca existente ao inicializar`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertEquals("Davi Yanomami", state.nome)
        assertEquals("M", state.sexo)
        assertEquals("01/01/2025", state.dataNascimentoTexto)
        assertEquals("Raoni", state.nomeResponsavel)
        assertEquals("123456789012345", state.numeroCns)
        assertEquals(2L, state.poloBaseId)
        assertEquals("Watoriki", state.comunidadeNome)
        assertNull(state.erro)
    }

    @Test
    fun `deve bloquear edicao quando nome estiver em branco`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()

        viewModel.onNomeChange("   ")
        var sucesso = false
        viewModel.salvar { sucesso = true }
        advanceUntilIdle()

        assertFalse(sucesso)
        assertEquals("Informe o nome da criança", viewModel.uiState.value.erro)
        assertNull(criancaAtualizada)
    }

    @Test
    fun `deve bloquear edicao quando data for invalida`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()

        viewModel.onDataNascimentoChange("99999999")
        var sucesso = false
        viewModel.salvar { sucesso = true }
        advanceUntilIdle()

        assertFalse(sucesso)
        assertEquals("Informe uma data de nascimento válida (dd/mm/aaaa)", viewModel.uiState.value.erro)
    }

    @Test
    fun `deve bloquear edicao quando CNS ja pertencer a outra crianca`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()

        viewModel.onNumeroCnsChange("999999999999999") // CNS da outraCriancaComCns
        var sucesso = false
        viewModel.salvar { sucesso = true }
        advanceUntilIdle()

        assertFalse(sucesso)
        assertTrue(viewModel.uiState.value.erro?.contains("já está cadastrado para outra criança") == true)
        assertNull(criancaAtualizada)
    }

    @Test
    fun `deve permitir salvar mantendo o mesmo CNS da propria crianca`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()

        // Mantém o mesmo CNS 123456789012345 e altera apenas o nome
        viewModel.onNomeChange("Davi Yanomami Editado")
        var sucesso = false
        viewModel.salvar { sucesso = true }
        advanceUntilIdle()

        assertTrue(sucesso)
        assertNull(viewModel.uiState.value.erro)
        assertEquals("Davi Yanomami Editado", criancaAtualizada?.nome)
    }

    @Test
    fun `deve salvar e atualizar crianca com sucesso quando campos validos`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        advanceUntilIdle()

        viewModel.onNomeChange("Davi Novo Nome")
        viewModel.onSexoChange("M")
        viewModel.onDataNascimentoChange("15/02/2025")
        viewModel.onNomeResponsavelChange("Raoni Novo")
        viewModel.onPoloBaseSelecionado(2L)
        viewModel.onComunidadeNomeChange("Nova Aldeia")

        var sucesso = false
        viewModel.salvar { sucesso = true }
        advanceUntilIdle()

        assertTrue(sucesso)
        assertFalse(viewModel.uiState.value.salvando)
        assertNull(viewModel.uiState.value.erro)

        assertEquals("Davi Novo Nome", criancaAtualizada?.nome)
        assertEquals("Raoni Novo", criancaAtualizada?.nomeResponsavel)
        assertEquals(criancaId, criancaAtualizada?.id)
    }
}
