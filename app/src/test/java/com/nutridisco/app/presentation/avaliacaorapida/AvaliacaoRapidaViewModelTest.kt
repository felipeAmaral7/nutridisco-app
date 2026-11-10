package com.nutridisco.app.presentation.avaliacaorapida

import com.nutridisco.app.data.local.entity.ReferenciaAntropometricaEntity
import com.nutridisco.app.domain.model.ClassificacaoEstatura
import com.nutridisco.app.domain.model.ClassificacaoPeso
import com.nutridisco.app.domain.repository.ReferenciaAntropometricaRepository
import com.nutridisco.app.domain.usecase.CalcularClassificacaoNutricionalUseCase
import com.nutridisco.app.domain.util.DataUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class AvaliacaoRapidaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeRepo = object : ReferenciaAntropometricaRepository {
        var shouldThrow = false
        var retornarNulo = false

        override suspend fun buscarReferencia(
            tipoParametro: String,
            sexo: String,
            idadeMeses: Int
        ): ReferenciaAntropometricaEntity? {
            if (shouldThrow) throw IllegalStateException("Erro simulado de banco")
            if (retornarNulo) return null
            return when (tipoParametro) {
                "ESTATURA" -> ReferenciaAntropometricaEntity(
                    id = 1,
                    tipoParametro = "ESTATURA",
                    sexo = sexo,
                    idadeMeses = idadeMeses,
                    escoreMenos3 = 78.0,
                    escoreMenos2 = 81.0,
                    escoreMais2 = 93.0
                )
                "PESO" -> ReferenciaAntropometricaEntity(
                    id = 2,
                    tipoParametro = "PESO",
                    sexo = sexo,
                    idadeMeses = idadeMeses,
                    escoreMenos3 = 9.0,
                    escoreMenos2 = 10.2,
                    escoreMais2 = 15.3
                )
                else -> null
            }
        }

        override suspend fun inserirTodas(referencias: List<ReferenciaAntropometricaEntity>) {}
        override suspend fun contar(): Int = 2
    }

    /** "Hoje" fixo nos testes: 10/11/2026, meio-dia em Boa Vista. */
    private val clock = Clock.fixed(Instant.parse("2026-11-10T16:00:00Z"), ZoneId.of("America/Boa_Vista"))

    private lateinit var useCase: CalcularClassificacaoNutricionalUseCase
    private lateinit var viewModel: AvaliacaoRapidaViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo.shouldThrow = false
        fakeRepo.retornarNulo = false
        useCase = CalcularClassificacaoNutricionalUseCase(fakeRepo)
        viewModel = AvaliacaoRapidaViewModel(useCase, clock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `data de nascimento valida deve calcular idade em meses e por extenso`() {
        viewModel.onDataNascimentoChange("10022025")

        val status = viewModel.uiState.value.dataNascimentoStatus as DataNascimentoStatus.Valida
        assertEquals(21, status.idadeMeses)
        assertEquals("1 ano e 9 meses", status.idadeExtenso)
    }

    @Test
    fun `campo de data deve guardar apenas os digitos`() {
        viewModel.onDataNascimentoChange("10/02/2025")
        assertEquals("10022025", viewModel.uiState.value.dataNascimento)

        viewModel.onDataNascimentoChange("100220251")
        assertEquals("10022025", viewModel.uiState.value.dataNascimento)
    }

    @Test
    fun `data incompleta nao deve ser interpretada`() {
        viewModel.onDataNascimentoChange("1002")
        assertEquals(DataNascimentoStatus.Incompleta, viewModel.uiState.value.dataNascimentoStatus)
    }

    @Test
    fun `data inexistente deve ser invalida`() {
        viewModel.onDataNascimentoChange("31022025")
        assertEquals(DataNascimentoStatus.Invalida, viewModel.uiState.value.dataNascimentoStatus)
    }

    @Test
    fun `data no futuro deve ser recusada`() {
        viewModel.onDataNascimentoChange("11112026")
        assertEquals(DataNascimentoStatus.Futura, viewModel.uiState.value.dataNascimentoStatus)
    }

    @Test
    fun `59 meses deve ser aceito e 60 meses deve ficar fora da faixa`() {
        viewModel.onDataNascimentoChange("11112021")
        val status = viewModel.uiState.value.dataNascimentoStatus as DataNascimentoStatus.Valida
        assertEquals(59, status.idadeMeses)

        viewModel.onDataNascimentoChange("10112021")
        assertEquals(DataNascimentoStatus.ForaDaFaixa(60), viewModel.uiState.value.dataNascimentoStatus)
    }

    @Test
    fun `calcular sem data valida deve exibir erro e nao navegar`() = runTest(testDispatcher) {
        viewModel.onDataNascimentoChange("1002")
        viewModel.onPesoChange("11,5")
        viewModel.onEstaturaChange("85,0")

        viewModel.calcular()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.erro?.contains("data de nascimento") == true)
        assertNull(state.resultado)
        assertFalse(state.navegarParaResultado)
    }

    @Test
    fun `podeCalcular deve exigir data valida, peso e estatura positivos`() {
        assertFalse(viewModel.uiState.value.podeCalcular)

        viewModel.onDataNascimentoChange(DATA_24_MESES)
        assertFalse(viewModel.uiState.value.podeCalcular)

        viewModel.onPesoChange("10,5")
        assertFalse(viewModel.uiState.value.podeCalcular)

        viewModel.onEstaturaChange("82,0")
        assertTrue(viewModel.uiState.value.podeCalcular)
    }

    @Test
    fun `onPesoChange e onEstaturaChange devem aceitar decimais com virgula ou ponto`() {
        viewModel.onPesoChange("12,4")
        viewModel.onEstaturaChange("85.5")

        assertEquals(12.4, viewModel.uiState.value.pesoValor)
        assertEquals(85.5, viewModel.uiState.value.estaturaValor)
    }

    @Test
    fun `ponto digitado deve ser exibido como virgula`() {
        viewModel.onPesoChange("12.4")
        viewModel.onEstaturaChange("85.5")

        assertEquals("12,4", viewModel.uiState.value.peso)
        assertEquals("85,5", viewModel.uiState.value.estatura)
    }

    @Test
    fun `nao deve aceitar dois separadores decimais misturados`() {
        viewModel.onPesoChange("12,4")
        viewModel.onPesoChange("12,4.")

        assertEquals("12,4", viewModel.uiState.value.peso)
    }

    @Test
    fun `calcular deve atualizar resultado com sucesso quando entradas sao validas`() = runTest(testDispatcher) {
        viewModel.onSexoChange("F")
        viewModel.onDataNascimentoChange(DATA_24_MESES)
        viewModel.onPesoChange("11,5")
        viewModel.onEstaturaChange("85,0")

        viewModel.calcular()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.calculando)
        assertNull(state.erro)
        assertNotNull(state.resultado)
        assertEquals("F", state.resultado?.sexo)
        assertEquals(24, state.resultado?.idadeMeses)
        assertEquals("10/11/2024", DataUtils.formatar(state.dataNascimentoResultado!!, clock.zone))
        assertEquals(ClassificacaoPeso.PESO_ADEQUADO, state.resultado?.classificacaoPeso)
        assertEquals(ClassificacaoEstatura.ESTATURA_ADEQUADA, state.resultado?.classificacaoEstatura)
    }

    @Test
    fun `calcular com sucesso deve emitir navegacao unica para o resultado`() = runTest(testDispatcher) {
        viewModel.onDataNascimentoChange(DATA_24_MESES)
        viewModel.onPesoChange("11,5")
        viewModel.onEstaturaChange("85,0")

        viewModel.calcular()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.navegarParaResultado)

        viewModel.onNavegouParaResultado()

        val state = viewModel.uiState.value
        assertFalse(state.navegarParaResultado)
        assertNotNull("o resultado deve continuar disponível para a tela de Resultado", state.resultado)
    }

    @Test
    fun `calcular com erro nao deve navegar para o resultado`() = runTest(testDispatcher) {
        viewModel.onDataNascimentoChange(DATA_24_MESES)
        viewModel.onPesoChange("60.0")
        viewModel.onEstaturaChange("85.0")

        viewModel.calcular()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.navegarParaResultado)
    }

    @Test
    fun `calcular com valores fora dos limites deve exibir erro amigavel`() = runTest(testDispatcher) {
        viewModel.onDataNascimentoChange(DATA_24_MESES)
        viewModel.onPesoChange("60.0") // Acima do teto de 50 kg
        viewModel.onEstaturaChange("85.0")

        viewModel.calcular()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.calculando)
        assertNull(state.resultado)
        assertNotNull(state.erro)
        assertTrue(state.erro?.contains("peso válido") == true)
    }

    @Test
    fun `calcular com erro inesperado do repositorio deve resetar calculando e exibir erro`() = runTest(testDispatcher) {
        viewModel.onDataNascimentoChange(DATA_24_MESES)
        fakeRepo.shouldThrow = true
        viewModel.onPesoChange("10.0")
        viewModel.onEstaturaChange("80.0")

        viewModel.calcular()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.calculando)
        assertNull(state.resultado)
        assertEquals(AvaliacaoRapidaViewModel.ERRO_INESPERADO, state.erro)
    }

    @Test
    fun `calcular com referencia antropometrica ausente deve exibir erro clinico e cancelar resultado`() = runTest(testDispatcher) {
        viewModel.onDataNascimentoChange(DATA_24_MESES)
        fakeRepo.retornarNulo = true
        viewModel.onPesoChange("10.0")
        viewModel.onEstaturaChange("80.0")

        viewModel.calcular()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.calculando)
        assertNull(state.resultado)
        assertEquals(AvaliacaoRapidaViewModel.ERRO_REFERENCIA_INDISPONIVEL, state.erro)
    }

    @Test
    fun `limpar deve redefinir o estado mantendo o sexo`() {
        viewModel.onSexoChange("F")
        viewModel.onDataNascimentoChange(DATA_24_MESES)
        viewModel.onPesoChange("14,0")

        viewModel.limpar()

        val state = viewModel.uiState.value
        assertEquals("F", state.sexo)
        assertEquals("", state.dataNascimento)
        assertEquals(DataNascimentoStatus.Incompleta, state.dataNascimentoStatus)
        assertEquals("", state.peso)
        assertNull(state.resultado)
    }

    private companion object {
        /** 10/11/2024: exatamente 24 meses em relação ao "hoje" fixo do [clock]. */
        const val DATA_24_MESES = "10112024"
    }
}
