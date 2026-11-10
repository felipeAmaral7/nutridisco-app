package com.nutridisco.app.domain.usecase

import com.nutridisco.app.data.local.entity.ReferenciaAntropometricaEntity
import com.nutridisco.app.domain.model.ClassificacaoEstatura
import com.nutridisco.app.domain.model.ClassificacaoPeso
import com.nutridisco.app.domain.model.NivelRisco
import com.nutridisco.app.domain.repository.ReferenciaAntropometricaRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalcularClassificacaoNutricionalUseCaseTest {

    private val fakeRepo = object : ReferenciaAntropometricaRepository {
        var retornarEstatura: Boolean = true
        var retornarPeso: Boolean = true

        override suspend fun buscarReferencia(
            tipoParametro: String,
            sexo: String,
            idadeMeses: Int
        ): ReferenciaAntropometricaEntity? {
            return when {
                tipoParametro == "ESTATURA" && retornarEstatura -> ReferenciaAntropometricaEntity(
                    id = 1,
                    tipoParametro = "ESTATURA",
                    sexo = sexo,
                    idadeMeses = idadeMeses,
                    escoreMenos3 = 78.0,
                    escoreMenos2 = 81.0,
                    escoreMais2 = 93.0
                )
                tipoParametro == "PESO" && retornarPeso -> ReferenciaAntropometricaEntity(
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

    private val useCase = CalcularClassificacaoNutricionalUseCase(fakeRepo)

    @Before
    fun setUp() {
        fakeRepo.retornarEstatura = true
        fakeRepo.retornarPeso = true
    }

    @Test
    fun `classificarEstatura deve classificar muito baixa estatura quando abaixo de escore -3`() {
        val ref = ReferenciaAntropometricaEntity(
            id = 1,
            tipoParametro = "ESTATURA",
            sexo = "M",
            idadeMeses = 12,
            escoreMenos3 = 71.0,
            escoreMenos2 = 73.4,
            escoreMais2 = 80.5
        )

        val resultado = CalcularClassificacaoNutricionalUseCase.classificarEstatura(70.9, ref)
        assertEquals(ClassificacaoEstatura.MUITO_BAIXA_ESTATURA, resultado)
        assertEquals(NivelRisco.VERMELHO, resultado.nivelRisco)
    }

    @Test
    fun `classificarEstatura deve classificar baixa estatura quando entre escore -3 e -2`() {
        val ref = ReferenciaAntropometricaEntity(
            id = 1,
            tipoParametro = "ESTATURA",
            sexo = "M",
            idadeMeses = 12,
            escoreMenos3 = 71.0,
            escoreMenos2 = 73.4,
            escoreMais2 = 80.5
        )

        val resultado = CalcularClassificacaoNutricionalUseCase.classificarEstatura(71.0, ref)
        assertEquals(ClassificacaoEstatura.BAIXA_ESTATURA, resultado)
        assertEquals(NivelRisco.AMARELO, resultado.nivelRisco)

        val resultado2 = CalcularClassificacaoNutricionalUseCase.classificarEstatura(73.3, ref)
        assertEquals(ClassificacaoEstatura.BAIXA_ESTATURA, resultado2)
    }

    @Test
    fun `classificarEstatura deve classificar estatura adequada quando maior ou igual a escore -2`() {
        val ref = ReferenciaAntropometricaEntity(
            id = 1,
            tipoParametro = "ESTATURA",
            sexo = "M",
            idadeMeses = 12,
            escoreMenos3 = 71.0,
            escoreMenos2 = 73.4,
            escoreMais2 = 80.5
        )

        val resultado = CalcularClassificacaoNutricionalUseCase.classificarEstatura(73.4, ref)
        assertEquals(ClassificacaoEstatura.ESTATURA_ADEQUADA, resultado)
        assertEquals(NivelRisco.VERDE, resultado.nivelRisco)
    }

    @Test
    fun `classificarPeso deve classificar muito baixo peso quando abaixo de escore -3`() {
        val ref = ReferenciaAntropometricaEntity(
            id = 2,
            tipoParametro = "PESO",
            sexo = "M",
            idadeMeses = 12,
            escoreMenos3 = 7.7,
            escoreMenos2 = 8.6,
            escoreMais2 = 12.0
        )

        val resultado = CalcularClassificacaoNutricionalUseCase.classificarPeso(7.6, ref)
        assertEquals(ClassificacaoPeso.MUITO_BAIXO_PESO, resultado)
        assertEquals(NivelRisco.VERMELHO, resultado.nivelRisco)
    }

    @Test
    fun `classificarPeso deve classificar baixo peso quando entre escore -3 e -2`() {
        val ref = ReferenciaAntropometricaEntity(
            id = 2,
            tipoParametro = "PESO",
            sexo = "M",
            idadeMeses = 12,
            escoreMenos3 = 7.7,
            escoreMenos2 = 8.6,
            escoreMais2 = 12.0
        )

        val resultado = CalcularClassificacaoNutricionalUseCase.classificarPeso(7.7, ref)
        assertEquals(ClassificacaoPeso.BAIXO_PESO, resultado)
        assertEquals(NivelRisco.AMARELO, resultado.nivelRisco)
    }

    @Test
    fun `classificarPeso deve classificar peso adequado quando entre escore -2 e +2`() {
        val ref = ReferenciaAntropometricaEntity(
            id = 2,
            tipoParametro = "PESO",
            sexo = "M",
            idadeMeses = 12,
            escoreMenos3 = 7.7,
            escoreMenos2 = 8.6,
            escoreMais2 = 12.0
        )

        val resultado = CalcularClassificacaoNutricionalUseCase.classificarPeso(8.6, ref)
        assertEquals(ClassificacaoPeso.PESO_ADEQUADO, resultado)
        assertEquals(NivelRisco.VERDE, resultado.nivelRisco)

        val resultado2 = CalcularClassificacaoNutricionalUseCase.classificarPeso(12.0, ref)
        assertEquals(ClassificacaoPeso.PESO_ADEQUADO, resultado2)
    }

    @Test
    fun `classificarPeso deve classificar peso elevado quando acima de escore +2`() {
        val ref = ReferenciaAntropometricaEntity(
            id = 2,
            tipoParametro = "PESO",
            sexo = "M",
            idadeMeses = 12,
            escoreMenos3 = 7.7,
            escoreMenos2 = 8.6,
            escoreMais2 = 12.0
        )

        val resultado = CalcularClassificacaoNutricionalUseCase.classificarPeso(12.1, ref)
        assertEquals(ClassificacaoPeso.PESO_ELEVADO, resultado)
        assertEquals(NivelRisco.VERMELHO, resultado.nivelRisco)
    }

    @Test
    fun `invoke deve integrar busca e classificacao de estatura e peso com sucesso`() = runBlocking {
        val resultado = useCase(
            idadeMeses = 24,
            sexo = "m",
            peso = 16.0,   // Acima de 15.3 -> Peso Elevado
            estatura = 85.0 // Acima de 81.0 -> Estatura Adequada
        )

        assertEquals("M", resultado.sexo)
        assertEquals(24, resultado.idadeMeses)
        assertEquals(ClassificacaoEstatura.ESTATURA_ADEQUADA, resultado.classificacaoEstatura)
        assertEquals(ClassificacaoPeso.PESO_ELEVADO, resultado.classificacaoPeso)
    }

    @Test(expected = IdadeLimiteExcedidaException::class)
    fun `invoke deve bloquear e lancar IdadeLimiteExcedidaException quando idade for exatamente 60 meses`() = runBlocking {
        useCase(
            idadeMeses = 60,
            sexo = "M",
            peso = 18.0,
            estatura = 110.0
        )
        Unit
    }

    @Test(expected = IdadeLimiteExcedidaException::class)
    fun `invoke deve bloquear e lancar IdadeLimiteExcedidaException quando idade for superior a 60 meses`() = runBlocking {
        useCase(
            idadeMeses = 72,
            sexo = "F",
            peso = 22.0,
            estatura = 120.0
        )
        Unit
    }

    @Test
    fun `invoke deve permitir calculo com sucesso para limite superior permitido de 59 meses`() = runBlocking {
        val resultado = useCase(
            idadeMeses = 59,
            sexo = "F",
            peso = 14.0,
            estatura = 95.0
        )
        assertEquals(59, resultado.idadeMeses)
    }

    @Test
    fun `invoke deve permitir calculo com sucesso para recem-nascido de 0 meses`() = runBlocking {
        val resultado = useCase(
            idadeMeses = 0,
            sexo = "M",
            peso = 3.5,
            estatura = 50.0
        )
        assertEquals(0, resultado.idadeMeses)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke deve bloquear e lancar IllegalArgumentException quando idade for negativa`() = runBlocking {
        useCase(
            idadeMeses = -1,
            sexo = "M",
            peso = 3.5,
            estatura = 50.0
        )
        Unit
    }

    @Test(expected = ReferenciaNaoEncontradaException::class)
    fun `invoke deve lancar ReferenciaNaoEncontradaException quando referencia de estatura nao existir`() = runBlocking {
        fakeRepo.retornarEstatura = false
        useCase(
            idadeMeses = 24,
            sexo = "M",
            peso = 12.0,
            estatura = 85.0
        )
        Unit
    }

    @Test(expected = ReferenciaNaoEncontradaException::class)
    fun `invoke deve lancar ReferenciaNaoEncontradaException quando referencia de peso nao existir`() = runBlocking {
        fakeRepo.retornarPeso = false
        useCase(
            idadeMeses = 24,
            sexo = "M",
            peso = 12.0,
            estatura = 85.0
        )
        Unit
    }
}
