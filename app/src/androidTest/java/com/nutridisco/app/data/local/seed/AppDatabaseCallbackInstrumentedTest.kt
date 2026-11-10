package com.nutridisco.app.data.local.seed

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nutridisco.app.data.local.AppDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Executa o seed real (assets/database/seed_nutridisco.sql) sobre um banco Room de verdade.
 * Roda em aparelho/emulador: ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseCallbackInstrumentedTest {

    private val nomeBanco = "seed-test.db"
    private lateinit var context: Context
    private lateinit var callback: AppDatabaseCallback
    private lateinit var database: AppDatabase
    private lateinit var db: SupportSQLiteDatabase

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(nomeBanco)
        callback = AppDatabaseCallback(context)
        database = Room.databaseBuilder(context, AppDatabase::class.java, nomeBanco)
            .addCallback(callback)
            .build()
        db = database.openHelper.writableDatabase // dispara onCreate + onOpen
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(nomeBanco)
    }

    @Test
    fun criacaoDoBanco_carregaSeedCompleto() {
        assertContagensDoSeed()
    }

    @Test
    fun abertura_comReferenciasVazias_recompletaSemTravarNemDuplicar() {
        // Cenário do bug: polos e comunidades existem, mas as referências da OMS estão vazias.
        // Antes, o seed era reexecutado inteiro e o INSERT dos polos (id fixo) lançava exceção.
        db.execSQL("DELETE FROM referencias_antropometricas")

        callback.onOpen(db)

        assertContagensDoSeed()
    }

    @Test
    fun abertura_comSeedParcial_completaApenasOQueFalta() {
        db.execSQL("DELETE FROM referencias_antropometricas WHERE tipo_parametro = 'PESO'")
        db.execSQL("DELETE FROM comunidades")

        callback.onOpen(db)

        assertContagensDoSeed()
    }

    @Test
    fun abertura_comSeedCompleto_naoAlteraNada() {
        db.execSQL("INSERT INTO comunidades (polo_base_id, nome) VALUES (1, 'Comunidade criada no app')")

        callback.onOpen(db)

        assertEquals(COMUNIDADES + 1, contar("comunidades"))
        assertEquals(POLOS, contar("polos_base"))
        assertEquals(REFERENCIAS, contar("referencias_antropometricas"))
    }

    private fun assertContagensDoSeed() {
        assertEquals(POLOS, contar("polos_base"))
        assertEquals(COMUNIDADES, contar("comunidades"))
        assertEquals(REFERENCIAS, contar("referencias_antropometricas"))
    }

    private fun contar(tabela: String): Int =
        db.query("SELECT COUNT(*) FROM $tabela").use { c ->
            c.moveToFirst()
            c.getInt(0)
        }

    private companion object {
        const val POLOS = 42
        const val COMUNIDADES = 354
        const val REFERENCIAS = 240 // PESO/ESTATURA × M/F × 0..59 meses
    }
}
