package com.nutridisco.app.data.local.migrations

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nutridisco.app.data.local.AppDatabase
import android.database.sqlite.SQLiteConstraintException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testa as migrações contra os schemas exportados em app/schemas/.
 * Roda em aparelho/emulador: ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class MigrationInstrumentedTest {

    private val nomeBanco = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migracao2Para3_converteTextosEmNomesDoEnum() {
        helper.createDatabase(nomeBanco, 2).use { db ->
            inserirBase(db)
            inserirAvaliacao(db, 1, "Baixa estatura para a idade", "Peso Elevado para a idade")
            inserirAvaliacao(db, 2, "ESTATURA ADEQUADA PARA A IDADE", " muito baixo peso para a idade ")
        }

        val db = helper.runMigrationsAndValidate(nomeBanco, 3, true, Migrations.MIGRATION_2_3)

        assertEquals("BAIXA_ESTATURA" to "PESO_ELEVADO", classificacoes(db, 1))
        assertEquals("ESTATURA_ADEQUADA" to "MUITO_BAIXO_PESO", classificacoes(db, 2))
        db.close()
    }

    @Test
    fun migracao2Para3_recalculaValoresDesconhecidosPelaTabelaOms() {
        helper.createDatabase(nomeBanco, 2).use { db ->
            inserirBase(db)
            // Referência fictícia para menino de 12 meses
            db.execSQL(
                "INSERT INTO referencias_antropometricas " +
                    "(tipo_parametro, sexo, idade_meses, escore_menos_3, escore_menos_2, escore_mais_2) VALUES " +
                    "('ESTATURA', 'M', 12, 68.6, 71.0, NULL), ('PESO', 'M', 12, 6.9, 7.7, 12.0)"
            )
            // estatura 70.0 → entre -3 e -2 (baixa); peso 13.0 → acima de +2 (elevado)
            inserirAvaliacao(db, 1, "texto desconhecido", "outro texto", estatura = 70.0, peso = 13.0)
        }

        val db = helper.runMigrationsAndValidate(nomeBanco, 3, true, Migrations.MIGRATION_2_3)

        assertEquals("BAIXA_ESTATURA" to "PESO_ELEVADO", classificacoes(db, 1))
        db.close()
    }

    @Test
    fun migracao3Para4_normalizaCnsEMantemApenasPrimeiroDuplicado() {
        helper.createDatabase(nomeBanco, 3).use { db ->
            inserirBase(db) // criança 1, sem CNS
            inserirCrianca(db, 2, "898 0010 0000 0001")
            inserirCrianca(db, 3, "898001000000001") // mesmo CNS da 2, sem espaços
            inserirCrianca(db, 4, "   ") // vazio
            inserirCrianca(db, 5, "700000000000005")
        }

        val db = helper.runMigrationsAndValidate(nomeBanco, 4, true, Migrations.MIGRATION_3_4)

        assertNull(cns(db, 1))
        assertEquals("898001000000001", cns(db, 2))
        assertNull(cns(db, 3)) // duplicado: CNS removido, criança mantida
        assertNull(cns(db, 4))
        assertEquals("700000000000005", cns(db, 5))
        assertEquals(5, db.query("SELECT COUNT(*) FROM criancas").use { it.moveToFirst(); it.getInt(0) })
        db.close()
    }

    @Test
    fun migracao3Para4_indicePermiteVariosSemCnsERecusaRepetido() {
        helper.createDatabase(nomeBanco, 3).use { db -> inserirBase(db) }

        val db = helper.runMigrationsAndValidate(nomeBanco, 4, true, Migrations.MIGRATION_3_4)

        inserirCrianca(db, 2, null)
        inserirCrianca(db, 3, null) // vários NULL são permitidos
        inserirCrianca(db, 4, "700000000000004")
        assertThrows(SQLiteConstraintException::class.java) {
            inserirCrianca(db, 5, "700000000000004")
        }
        db.close()
    }

    private fun inserirCrianca(db: androidx.sqlite.db.SupportSQLiteDatabase, id: Long, cns: String?) {
        db.execSQL(
            "INSERT INTO criancas (id, comunidade_id, nome, sexo, data_nascimento, nome_responsavel, numero_cns) " +
                "VALUES (?, 1, ?, 'F', 0, 'Responsável', ?)",
            arrayOf(id, "Criança $id", cns)
        )
    }

    private fun cns(db: androidx.sqlite.db.SupportSQLiteDatabase, id: Long): String? =
        db.query("SELECT numero_cns FROM criancas WHERE id = ?", arrayOf(id)).use { c ->
            c.moveToFirst()
            if (c.isNull(0)) null else c.getString(0)
        }

    private fun inserirBase(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("INSERT INTO polos_base (id, nome, etnorregiao) VALUES (1, 'Polo', 'Serras')")
        db.execSQL("INSERT INTO comunidades (id, polo_base_id, nome) VALUES (1, 1, 'Comunidade')")
        db.execSQL(
            "INSERT INTO criancas (id, comunidade_id, nome, sexo, data_nascimento, nome_responsavel) " +
                "VALUES (1, 1, 'Criança', 'M', 0, 'Responsável')"
        )
    }

    private fun inserirAvaliacao(
        db: androidx.sqlite.db.SupportSQLiteDatabase,
        id: Long,
        estaturaTexto: String,
        pesoTexto: String,
        estatura: Double = 75.0,
        peso: Double = 10.0,
    ) {
        db.execSQL(
            "INSERT INTO avaliacoes (id, crianca_id, comunidade_atendimento_id, data_atendimento, " +
                "idade_meses_momento, peso, estatura, classificacao_estatura, classificacao_peso, " +
                "nome_profissional, registro_coren) VALUES (?, 1, 1, 0, 12, ?, ?, ?, ?, 'Prof', 'COREN')",
            arrayOf(id, peso, estatura, estaturaTexto, pesoTexto)
        )
    }

    private fun classificacoes(db: androidx.sqlite.db.SupportSQLiteDatabase, id: Long): Pair<String, String> =
        db.query(
            "SELECT classificacao_estatura, classificacao_peso FROM avaliacoes WHERE id = ?",
            arrayOf(id)
        ).use { c ->
            c.moveToFirst()
            c.getString(0) to c.getString(1)
        }
}
