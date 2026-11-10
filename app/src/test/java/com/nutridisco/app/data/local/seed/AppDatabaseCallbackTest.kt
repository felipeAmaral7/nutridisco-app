package com.nutridisco.app.data.local.seed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppDatabaseCallbackTest {

    private val seedReal: String by lazy {
        File("src/main/assets/${AppDatabaseCallback.ASSET_FILE}").readText(Charsets.UTF_8)
    }

    @Test
    fun `separarStatements deve ignorar comentarios e separar por ponto e virgula`() {
        val sql = """
            -- Comentario de cabecalho
            INSERT INTO teste VALUES (1, 'A');

            -- Outro comentario
            INSERT INTO teste VALUES (2, 'B');
        """.trimIndent()

        val statements = AppDatabaseCallback.separarStatements(sql)

        assertEquals(2, statements.size)
        assertEquals("INSERT INTO teste VALUES (1, 'A')", statements[0])
        assertEquals("INSERT INTO teste VALUES (2, 'B')", statements[1])
    }

    @Test
    fun `prepararStatements deve transformar INSERT em INSERT OR IGNORE`() {
        val statements = AppDatabaseCallback.prepararStatements(
            "INSERT INTO a VALUES (1);\ninsert  into b VALUES (2);\nINSERT OR IGNORE INTO c VALUES (3);"
        )

        assertEquals(
            listOf(
                "INSERT OR IGNORE INTO a VALUES (1)",
                "INSERT OR IGNORE INTO b VALUES (2)",
                "INSERT OR IGNORE INTO c VALUES (3)"
            ),
            statements
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `prepararStatements deve recusar comandos que nao sejam INSERT`() {
        AppDatabaseCallback.prepararStatements("CREATE TABLE x (id INTEGER);\nINSERT INTO x VALUES (1);")
    }

    @Test
    fun `seed real deve conter apenas INSERTs nas tabelas de seed`() {
        val statements = AppDatabaseCallback.prepararStatements(seedReal)

        assertTrue(statements.isNotEmpty())
        statements.forEach { statement ->
            assertTrue(statement.startsWith("INSERT OR IGNORE INTO"))
            val tabela = statement.removePrefix("INSERT OR IGNORE INTO").trim().substringBefore(" ")
            assertTrue("Tabela fora do seed: $tabela", tabela in AppDatabaseCallback.TABELAS_SEED)
        }
        AppDatabaseCallback.TABELAS_SEED.forEach { tabela ->
            assertTrue("Seed sem dados para $tabela", statements.any { it.contains("INTO $tabela ") })
        }
    }

    @Test
    fun `seed real nao deve conter DDL`() {
        val semComentarios = AppDatabaseCallback.separarStatements(seedReal).joinToString("\n")

        listOf("CREATE", "ALTER", "DROP").forEach { comando ->
            assertFalse(
                "O seed não deve conter $comando (a estrutura pertence ao Room)",
                Regex("\\b$comando\\b", RegexOption.IGNORE_CASE).containsMatchIn(semComentarios)
            )
        }
    }
}
