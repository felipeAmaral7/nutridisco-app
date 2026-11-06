package com.nutridisco.app.data.local.seed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class AppDatabaseCallbackTest {

    @Test
    fun `parse de statements deve ignorar comentarios e separar corretamente comandos SQL`() {
        val sqlScript = """
            -- Comentario de cabecalho
            CREATE TABLE teste (id INT);
            
            -- Insercao de dados
            INSERT INTO teste VALUES (1, 'A');
            INSERT INTO teste VALUES (2, 'B');
        """.trimIndent()

        val lines = sqlScript.lines().filter { !it.trim().startsWith("--") }
        val content = lines.joinToString("\n")
        val statements = content.split(";").map { it.trim() }.filter { it.isNotEmpty() }

        assertEquals(3, statements.size)
        assertTrue(statements[0].startsWith("CREATE TABLE"))
        assertTrue(statements[1].startsWith("INSERT INTO"))
        assertTrue(statements[2].startsWith("INSERT INTO"))
    }
}
