package com.nutridisco.app.presentation.avaliacaorapida

import com.nutridisco.app.data.local.entity.ReferenciaAntropometricaEntity
import com.nutridisco.app.domain.model.ClassificacaoEstatura
import com.nutridisco.app.domain.model.ClassificacaoPeso
import com.nutridisco.app.domain.repository.ReferenciaAntropometricaRepository
import com.nutridisco.app.domain.usecase.CalcularClassificacaoNutricionalUseCase
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

    private lateinit var useCase: CalcularClassificacaoNutricionalUseCase
    private lateinit var viewModel: AvaliacaoRapidaViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo.shouldThrow = false
        fakeRepo.retornarNulo = false
        useCase = CalcularClassificacaoNutricionalUseCase(fakeRepo)
        viewModel = AvaliacaoRapidaViewModel(useCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `idade deve comecar em 2 anos e 6 meses`() {
        val state = viewModel.uiState.value
        assertEquals(2, state.anos)
        assertEquals(6, state.meses)
        assertEquals(30, state.idadeMeses)
    }

    @Test
    fun `anos e meses devem ser limitados aos tetos maximos e calcular idadeMeses`() {
        viewModel.onAnosChange(5) // Max é 4
        viewModel.onMesesChange(15) // Max é 11

        val state = viewModel.uiState.value
        assertEquals(4, state.anos)
        assertEquals(11, state.meses)
        assertEquals(59, state.idadeMeses)
    }

    @Test
    fun `podeCalcular deve ser verdadeiro apenas com peso e estatura positivos`() {
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
        viewModel.onAnosChange(2)
        viewModel.onMesesChange(0)
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
        assertEquals(ClassificacaoPeso.PESO_ADEQUADO, state.resultado?.classificacaoPeso)
        assertEquals(ClassificacaoEstatura.ESTATURA_ADEQUADA, state.resultado?.classificacaoEstatura)
    }

    @Test
    fun `calcular com sucesso deve emitir navegacao unica para o resultado`() = runTest(testDispatcher) {
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
        viewModel.onPesoChange("60.0")
        viewModel.onEstaturaChange("85.0")

        viewModel.calcular()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.navegarParaResultado)
    }

    @Test
    fun `calcular com valores fora dos limites deve exibir erro amigavel`() = runTest(testDispatcher) {
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
        viewModel.onAnosChange(3)
        viewModel.onPesoChange("14,0")

        viewModel.limpar()

        val state = viewModel.uiState.value
        assertEquals("F", state.sexo)
        assertEquals(AvaliacaoRapidaViewModel.ANOS_INICIAL, state.anos)
        assertEquals(AvaliacaoRapidaViewModel.MESES_INICIAL, state.meses)
        assertEquals("", state.peso)
        assertNull(state.resultado)
    }
}
