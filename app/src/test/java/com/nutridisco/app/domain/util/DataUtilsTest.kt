package com.nutridisco.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class DataUtilsTest {

    private val zoneId = ZoneId.of("America/Boa_Vista")

    private fun meiaNoite(ano: Int, mes: Int, dia: Int): Long =
        LocalDate.of(ano, mes, dia).atStartOfDay(zoneId).toInstant().toEpochMilli()

    @Test
    fun `aplicarMascara deve inserir barras conforme os digitos`() {
        assertEquals("", DataUtils.aplicarMascara(""))
        assertEquals("12", DataUtils.aplicarMascara("12"))
        assertEquals("12/0", DataUtils.aplicarMascara("120"))
        assertEquals("12/02/2025", DataUtils.aplicarMascara("12022025"))
        assertEquals("12/02/2025", DataUtils.aplicarMascara("12/02/20259"))
    }

    @Test
    fun `parse deve aceitar com ou sem barras e retornar a meia-noite do dia`() {
        assertEquals(meiaNoite(2025, 2, 12), DataUtils.parse("12/02/2025", zoneId))
        assertEquals(meiaNoite(2025, 2, 12), DataUtils.parse("12022025", zoneId))
    }

    @Test
    fun `parse deve recusar data incompleta ou inexistente`() {
        assertNull(DataUtils.parse("12/02/25", zoneId))
        assertNull(DataUtils.parse("31/02/2025", zoneId))
        assertNull(DataUtils.parse("29/02/2025", zoneId))
        assertNull(DataUtils.parse("00/01/2025", zoneId))
        assertNull(DataUtils.parse("10/13/2025", zoneId))
    }

    @Test
    fun `parse deve aceitar 29 de fevereiro em ano bissexto`() {
        assertEquals(meiaNoite(2024, 2, 29), DataUtils.parse("29/02/2024", zoneId))
    }

    @Test
    fun `formatar deve ser o inverso de parse`() {
        assertEquals("05/09/2023", DataUtils.formatar(DataUtils.parse("05092023", zoneId)!!, zoneId))
    }
}
