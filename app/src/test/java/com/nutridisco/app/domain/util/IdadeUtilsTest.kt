package com.nutridisco.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class IdadeUtilsTest {

    private val zoneId = ZoneId.of("UTC")

    private fun toMillis(year: Int, month: Int, day: Int): Long {
        return LocalDate.of(year, month, day)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    @Test
    fun `calcularIdadeEmMesesCompletos deve retornar 0 quando bebe tem menos de 1 mes`() {
        val nasc = toMillis(2024, 1, 10)
        val aval = toMillis(2024, 2, 8) // 29 dias depois
        val meses = IdadeUtils.calcularIdadeEmMesesCompletos(nasc, aval, zoneId)
        assertEquals(0, meses)
    }

    @Test
    fun `calcularIdadeEmMesesCompletos deve retornar 1 quando bebe tem exatamente 1 mes`() {
        val nasc = toMillis(2024, 1, 10)
        val aval = toMillis(2024, 2, 10)
        val meses = IdadeUtils.calcularIdadeEmMesesCompletos(nasc, aval, zoneId)
        assertEquals(1, meses)
    }

    @Test
    fun `calcularIdadeEmMesesCompletos deve retornar apenas meses completos e ignorar dias parciais`() {
        val nasc = toMillis(2023, 5, 15)
        val aval = toMillis(2024, 5, 14) // 11 meses e 29 dias
        val meses = IdadeUtils.calcularIdadeEmMesesCompletos(nasc, aval, zoneId)
        assertEquals(11, meses)
    }

    @Test
    fun `calcularIdadeEmMesesCompletos deve retornar 24 para exatamente 2 anos de idade`() {
        val nasc = toMillis(2022, 3, 1)
        val aval = toMillis(2024, 3, 1)
        val meses = IdadeUtils.calcularIdadeEmMesesCompletos(nasc, aval, zoneId)
        assertEquals(24, meses)
    }

    @Test
    fun `formatarIdadeExtenso deve formatar anos e meses corretamente`() {
        val nasc = toMillis(2023, 1, 1)
        val aval = toMillis(2024, 4, 1) // 1 ano e 3 meses
        val texto = IdadeUtils.formatarIdadeExtenso(nasc, aval, zoneId)
        assertEquals("1 ano e 3 meses", texto)
    }
}
