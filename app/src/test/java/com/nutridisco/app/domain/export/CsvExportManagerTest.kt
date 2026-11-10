package com.nutridisco.app.domain.export

import com.nutridisco.app.domain.model.ClassificacaoEstatura
import com.nutridisco.app.domain.model.ClassificacaoPeso
import com.nutridisco.app.data.local.model.AvaliacaoDetalhada
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CsvExportManagerTest {

    private val zoneId = ZoneId.of("UTC")

    private fun toMillis(year: Int, month: Int, day: Int): Long {
        return LocalDate.of(year, month, day)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    @Test
    fun `gerarConteudoCsv deve conter cabecalho e linhas formatadas com separador ponto e virgula`() {
        val avaliacao = AvaliacaoDetalhada(
            avaliacaoId = 10,
            poloBaseNome = "Polo Surumu",
            comunidadeNome = "Aldeia Barro",
            nomeProfissional = "Enf. João Souza",
            registroCoren = "COREN-RR 99999",
            criancaId = 1,
            criancaNome = "Pedro Macuxi",
            sexo = "M",
            dataNascimento = toMillis(2023, 1, 10),
            dataAtendimento = toMillis(2024, 1, 10),
            idadeMesesMomento = 12,
            peso = 10.5,
            estatura = 75.2,
            classificacaoEstatura = ClassificacaoEstatura.ESTATURA_ADEQUADA,
            classificacaoPeso = ClassificacaoPeso.PESO_ADEQUADO,
            observacoes = "Criança ativa; sem sintomas"
        )

        val csv = CsvExportManager.gerarConteudoCsv(listOf(avaliacao))

        // Verifica cabeçalho
        assertTrue(csv.contains("Polo Base;Comunidade;Profissional;COREN;ID Crianca;Nome Crianca;Sexo"))
        // Verifica linha de dados
        assertTrue(csv.contains("Polo Surumu;Aldeia Barro;Enf. João Souza;COREN-RR 99999;1;Pedro Macuxi;M;"))
        assertTrue(csv.contains(";12;10.50;75.2;Estatura adequada para a idade;Peso adequado para a idade;\"Criança ativa; sem sintomas\""))
    }

    @Test
    fun `escapeCsv deve escapar aspas duplas e envolver campos com ponto e virgula`() {
        val textoComPontoEVirgula = "Item A; Item B"
        val escapado1 = CsvExportManager.escapeCsv(textoComPontoEVirgula)
        assertTrue(escapado1 == "\"Item A; Item B\"")

        val textoComAspas = "Comunidade \"São Jorge\""
        val escapado2 = CsvExportManager.escapeCsv(textoComAspas)
        assertTrue(escapado2 == "\"Comunidade \"\"São Jorge\"\"\"")
    }
}
