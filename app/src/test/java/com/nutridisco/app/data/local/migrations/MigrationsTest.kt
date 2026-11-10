package com.nutridisco.app.data.local.migrations

import com.nutridisco.app.data.local.AppDatabase
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Proteção contra perda de dados em atualizações: garante que cada aumento de versão
 * do banco tenha uma migração explícita e um schema exportado e versionado.
 */
class MigrationsTest {

    /** Primeira versão que foi para os aparelhos; a versão 1 foi só de desenvolvimento. */
    private val primeiraVersaoEmProducao = 2

    @Test
    fun `cada versao a partir da primeira em producao deve ter migracao para a seguinte`() {
        val caminhos = Migrations.TODAS.map { it.startVersion to it.endVersion }.toSet()

        for (versao in primeiraVersaoEmProducao until AppDatabase.VERSION) {
            assertTrue(
                "Falta Migration($versao, ${versao + 1}) em Migrations.TODAS. " +
                    "Sem ela, aparelhos na versão $versao não conseguem abrir o banco.",
                (versao to versao + 1) in caminhos
            )
        }
    }

    @Test
    fun `schema de cada versao deve estar exportado em app-schemas`() {
        val pasta = File("schemas/${AppDatabase::class.java.name}")

        for (versao in primeiraVersaoEmProducao..AppDatabase.VERSION) {
            val arquivo = File(pasta, "$versao.json")
            assertTrue(
                "Schema da versão $versao não encontrado em ${arquivo.path}. " +
                    "Rode o build e faça commit da pasta app/schemas/.",
                arquivo.isFile
            )
        }
    }
}
