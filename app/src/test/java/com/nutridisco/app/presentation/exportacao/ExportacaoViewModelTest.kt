package com.nutridisco.app.presentation.exportacao

import android.content.Context
import android.content.Intent
import com.nutridisco.app.data.local.entity.AvaliacaoEntity
import com.nutridisco.app.data.local.model.AvaliacaoDetalhada
import com.nutridisco.app.domain.export.CsvExportManager
import com.nutridisco.app.domain.export.PdfExportManager
import com.nutridisco.app.domain.model.ClassificacaoEstatura
import com.nutridisco.app.domain.model.ClassificacaoPeso
import com.nutridisco.app.domain.repository.AvaliacaoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ExportacaoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val avaliacoesFlow = MutableStateFlow<List<AvaliacaoDetalhada>>(emptyList())

    private val avaliacaoRepo = object : AvaliacaoRepository {
        override fun listarPorCrianca(criancaId: Long): Flow<List<AvaliacaoEntity>> = throw NotImplementedError()
        override fun buscarUltimaAvaliacao(criancaId: Long): Flow<AvaliacaoEntity?> = throw NotImplementedError()
        override suspend fun buscarPorId(id: Long): AvaliacaoEntity? = null
        override fun listarTodasDetalhadasFlow(): Flow<List<AvaliacaoDetalhada>> = avaliacoesFlow
        override suspend fun listarTodasParaExportacao(): List<AvaliacaoDetalhada> = avaliacoesFlow.value
        override suspend fun salvarAvaliacao(avaliacao: AvaliacaoEntity): Long = 1L
        override suspend fun atualizarAvaliacao(avaliacao: AvaliacaoEntity) {}
        override suspend fun deletarAvaliacao(avaliacao: AvaliacaoEntity) {}
    }

    private var csvGerado = false
    private var pdfGerado = false

    private val fakeContext = object : android.content.ContextWrapper(null) {
        override fun startActivity(intent: Intent?) {}
        override fun getPackageName(): String = "com.nutridisco.app"
    }

    private val fakeCsvManager = object : CsvExportManager(fakeContext) {
        override suspend fun gerarCsvAvaliacoes(avaliacoes: List<AvaliacaoDetalhada>): File {
            csvGerado = true
            return File.createTempFile("teste", ".csv")
        }
        override fun criarIntentCompartilhamento(file: File): Intent = Intent()
    }

    private val fakePdfManager = object : PdfExportManager(fakeContext) {
        override suspend fun gerarPdfAvaliacoes(avaliacoes: List<AvaliacaoDetalhada>): File {
            pdfGerado = true
            return File.createTempFile("teste", ".pdf")
        }
        override fun criarIntentCompartilhamento(file: File): Intent = Intent()
    }

    private fun criarViewModel() = ExportacaoViewModel(
        avaliacaoRepository = avaliacaoRepo,
        csvExportManager = fakeCsvManager,
        pdfExportManager = fakePdfManager
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        avaliacoesFlow.value = emptyList()
        csvGerado = false
        pdfGerado = false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial deve ter PDF selecionado por padrao`() {
        val viewModel = criarViewModel()
        assertEquals(FormatoExportacao.PDF, viewModel.uiState.value.formatoSelecionado)
        assertFalse(viewModel.uiState.value.exportando)
        assertNull(viewModel.uiState.value.erro)
    }

    @Test
    fun `selecionarFormato deve alterar formatoSelecionado`() {
        val viewModel = criarViewModel()
        viewModel.selecionarFormato(FormatoExportacao.CSV)
        assertEquals(FormatoExportacao.CSV, viewModel.uiState.value.formatoSelecionado)

        viewModel.selecionarFormato(FormatoExportacao.PDF)
        assertEquals(FormatoExportacao.PDF, viewModel.uiState.value.formatoSelecionado)
    }

    @Test
    fun `exportar com lista vazia deve exibir erro`() = runTest(testDispatcher) {
        val viewModel = criarViewModel()
        viewModel.exportar(FormatoExportacao.PDF, fakeContext)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.exportando)
        assertEquals("Não há avaliações registradas para exportar.", viewModel.uiState.value.erro)
        assertFalse(pdfGerado)
        assertFalse(csvGerado)
    }

    @Test
    fun `exportar PDF com dados deve chamar PdfExportManager`() = runTest(testDispatcher) {
        avaliacoesFlow.value = listOf(
            AvaliacaoDetalhada(
                avaliacaoId = 1L,
                poloBaseNome = "Polo 1",
                comunidadeNome = "Aldeia 1",
                nomeProfissional = "Enf. Teste",
                registroCoren = "123",
                criancaId = 10L,
                criancaNome = "Davi",
                sexo = "M",
                dataNascimento = 0L,
                dataAtendimento = 1000L,
                idadeMesesMomento = 10,
                peso = 9.0,
                estatura = 72.0,
                classificacaoEstatura = ClassificacaoEstatura.ESTATURA_ADEQUADA,
                classificacaoPeso = ClassificacaoPeso.PESO_ADEQUADO,
                observacoes = null
            )
        )

        val viewModel = criarViewModel()
        viewModel.exportar(FormatoExportacao.PDF, fakeContext)
        advanceUntilIdle()

        assertTrue(pdfGerado)
        assertFalse(csvGerado)
        assertFalse(viewModel.uiState.value.exportando)
        assertTrue(viewModel.uiState.value.mensagemSucesso?.contains("PDF") == true)
    }

    @Test
    fun `exportar CSV com dados deve chamar CsvExportManager`() = runTest(testDispatcher) {
        avaliacoesFlow.value = listOf(
            AvaliacaoDetalhada(
                avaliacaoId = 1L,
                poloBaseNome = "Polo 1",
                comunidadeNome = "Aldeia 1",
                nomeProfissional = "Enf. Teste",
                registroCoren = "123",
                criancaId = 10L,
                criancaNome = "Davi",
                sexo = "M",
                dataNascimento = 0L,
                dataAtendimento = 1000L,
                idadeMesesMomento = 10,
                peso = 9.0,
                estatura = 72.0,
                classificacaoEstatura = ClassificacaoEstatura.ESTATURA_ADEQUADA,
                classificacaoPeso = ClassificacaoPeso.PESO_ADEQUADO,
                observacoes = null
            )
        )

        val viewModel = criarViewModel()
        viewModel.exportar(FormatoExportacao.CSV, fakeContext)
        advanceUntilIdle()

        assertTrue(csvGerado)
        assertFalse(pdfGerado)
        assertFalse(viewModel.uiState.value.exportando)
        assertTrue(viewModel.uiState.value.mensagemSucesso?.contains("CSV") == true)
    }
}
