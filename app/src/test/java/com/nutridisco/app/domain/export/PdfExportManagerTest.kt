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

    @Test
    fun `calcularLargurasColunas deve preencher exatamente a largura util da pagina`() {
        val campos = listOf(CampoExportacao.DATA_AVALIACAO, CampoExportacao.NOME_CRIANCA, CampoExportacao.RESPONSAVEL)
        val larguras = PdfExportManager.calcularLargurasColunas(campos)

        assertEquals(3, larguras.size)
        val larguraUtil = PdfExportManager.PAGE_WIDTH - PdfExportManager.MARGIN_LEFT - PdfExportManager.MARGIN_RIGHT
        assertEquals(larguraUtil, larguras.sum(), 0.01f)
    }

    @Test
    fun `obterTextoFormatado deve formatar dados do item incluindo responsavel`() {
        val item = com.nutridisco.app.data.local.model.AvaliacaoDetalhada(
            avaliacaoId = 1L,
            poloBaseNome = "Polo 1",
            comunidadeNome = "Aldeia 1",
            nomeProfissional = "Enf. Teste",
            registroCoren = "123",
            criancaId = 10L,
            criancaNome = "Davi",
            nomeResponsavel = "Maria Mae",
            sexo = "M",
            dataNascimento = 0L,
            dataAtendimento = 1000L,
            idadeMesesMomento = 10,
            peso = 9.0,
            estatura = 72.0,
            classificacaoEstatura = com.nutridisco.app.domain.model.ClassificacaoEstatura.ESTATURA_ADEQUADA,
            classificacaoPeso = com.nutridisco.app.domain.model.ClassificacaoPeso.PESO_ADEQUADO,
            observacoes = null
        )

        val (textoResp, isBoldResp) = PdfExportManager.obterTextoFormatado(item, CampoExportacao.RESPONSAVEL)
        assertEquals("Maria Mae", textoResp)
        assertEquals(false, isBoldResp)

        val (textoNome, isBoldNome) = PdfExportManager.obterTextoFormatado(item, CampoExportacao.NOME_CRIANCA)
        assertEquals("Davi", textoNome)
        assertEquals(true, isBoldNome)
    }
}
