package com.nutridisco.app.domain.export

import org.junit.Assert.assertEquals
import org.junit.Test

class PdfExportManagerTest {

    @Test
    fun `calcularTotalPaginas deve calcular paginacao com limites corretos`() {
        assertEquals(1, PdfExportManager.calcularTotalPaginas(0))
        assertEquals(1, PdfExportManager.calcularTotalPaginas(1))
        assertEquals(1, PdfExportManager.calcularTotalPaginas(PdfExportManager.ITENS_PAGINA_1))

        // 1 item a mais que a primeira página vai para a segunda
        assertEquals(2, PdfExportManager.calcularTotalPaginas(PdfExportManager.ITENS_PAGINA_1 + 1))

        // Primeira página (23) + segunda página (28) = 51 itens
        assertEquals(2, PdfExportManager.calcularTotalPaginas(PdfExportManager.ITENS_PAGINA_1 + PdfExportManager.ITENS_DEMAIS_PAGINAS))

        // 52 itens requer 3 páginas
        assertEquals(3, PdfExportManager.calcularTotalPaginas(PdfExportManager.ITENS_PAGINA_1 + PdfExportManager.ITENS_DEMAIS_PAGINAS + 1))
    }

    @Test
    fun `truncarTexto deve limitar caracteres e adicionar reticencias`() {
        assertEquals("Texto Curto", PdfExportManager.truncarTexto("Texto Curto", 20))
        assertEquals("Texto Mui…", PdfExportManager.truncarTexto("Texto Muito Longo", 10))
    }

    @Test
    fun `resumirStatus deve simplificar rotulos longos da OMS`() {
        assertEquals("M. Baixa", PdfExportManager.resumirStatus("Muito baixa estatura para a idade"))
        assertEquals("Baixa", PdfExportManager.resumirStatus("Baixa estatura para a idade"))
        assertEquals("Adequado", PdfExportManager.resumirStatus("Estatura adequada para a idade"))
        assertEquals("M. Baixo", PdfExportManager.resumirStatus("Muito baixo peso para a idade"))
        assertEquals("B. Peso", PdfExportManager.resumirStatus("Baixo peso para a idade"))
        assertEquals("Elevado", PdfExportManager.resumirStatus("Peso elevado para a idade"))
    }
}
