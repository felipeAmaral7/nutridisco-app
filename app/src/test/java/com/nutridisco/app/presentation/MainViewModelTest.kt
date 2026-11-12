package com.nutridisco.app.presentation

import com.nutridisco.app.data.local.entity.ConfiguracaoDispositivoEntity
import com.nutridisco.app.domain.repository.ConfiguracaoRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val configuracaoFlow = MutableStateFlow<ConfiguracaoDispositivoEntity?>(null)

    private val configuracaoRepo = object : ConfiguracaoRepository {
        override fun getConfiguracaoFlow(): Flow<ConfiguracaoDispositivoEntity?> = configuracaoFlow
        override suspend fun getConfiguracao(): ConfiguracaoDispositivoEntity? = configuracaoFlow.value
        override suspend fun salvarConfiguracao(configuracao: ConfiguracaoDispositivoEntity) {
            configuracaoFlow.value = configuracao
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        configuracaoFlow.value = null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `quando configuracao e nula estaConfigurado deve ser false`() = runTest(testDispatcher) {
        val viewModel = MainViewModel(configuracaoRepo)
        advanceUntilIdle()

        assertEquals(false, viewModel.estaConfigurado.value)
    }

    @Test
    fun `quando nome do profissional e em branco estaConfigurado deve ser false`() = runTest(testDispatcher) {
        configuracaoFlow.value = ConfiguracaoDispositivoEntity(
            nomeProfissional = "   ",
            registroCoren = "12345",
            poloBaseIdPadrao = 1L
        )

        val viewModel = MainViewModel(configuracaoRepo)
        advanceUntilIdle()

        assertFalse(viewModel.estaConfigurado.value == true)
    }

    @Test
    fun `quando polo base padrao e nulo estaConfigurado deve ser false`() = runTest(testDispatcher) {
        configuracaoFlow.value = ConfiguracaoDispositivoEntity(
            nomeProfissional = "Enf. Joana",
            registroCoren = "12345",
            poloBaseIdPadrao = null
        )

        val viewModel = MainViewModel(configuracaoRepo)
        advanceUntilIdle()

        assertFalse(viewModel.estaConfigurado.value == true)
    }

    @Test
    fun `quando configuracao e valida estaConfigurado deve ser true`() = runTest(testDispatcher) {
        configuracaoFlow.value = ConfiguracaoDispositivoEntity(
            nomeProfissional = "Enf. Joana",
            registroCoren = "12345",
            poloBaseIdPadrao = 1L
        )

        val viewModel = MainViewModel(configuracaoRepo)
        advanceUntilIdle()

        assertTrue(viewModel.estaConfigurado.value == true)
    }
}
