package com.nutridisco.app.data.local.seed

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Callback do RoomDatabase que carrega o Seed Data (seed_nutridisco.sql):
 * Polos Base, Comunidades e a Tabela de Referências Antropométricas da OMS.
 *
 * O seed é idempotente: cada INSERT é executado como INSERT OR IGNORE, então pode ser
 * reexecutado sobre um banco parcialmente preenchido sem violar chaves/índices únicos
 * (ids fixos dos polos, comunidade única por polo, referência única por tipo/sexo/idade).
 */
class AppDatabaseCallback(
    private val context: Context
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        executarSeed(db, "onCreate")
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        // Recupera bancos com seed incompleto (ex.: falha anterior ou tabela esvaziada)
        executarSeedSeIncompleto(db)
    }

    private fun executarSeedSeIncompleto(db: SupportSQLiteDatabase) {
        val tabelasVazias = TABELAS_SEED.filter { tabela -> contarLinhas(db, tabela) == 0 }
        if (tabelasVazias.isNotEmpty()) {
            executarSeed(db, "onOpen (tabelas vazias: ${tabelasVazias.joinToString()})")
        }
    }

    private fun contarLinhas(db: SupportSQLiteDatabase, tabela: String): Int =
        db.query("SELECT COUNT(*) FROM $tabela").use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }

    private fun executarSeed(db: SupportSQLiteDatabase, origem: String) {
        Log.i(TAG, "Iniciando execução do Seed Data a partir de $origem...")
        val conteudo = context.assets.open(ASSET_FILE).use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
        }
        val statements = prepararStatements(conteudo)
        Log.d(TAG, "Total de statements do seed: ${statements.size}")

        val jaEmTransacao = db.inTransaction()
        if (!jaEmTransacao) db.beginTransaction()
        try {
            statements.forEach(db::execSQL)
            if (!jaEmTransacao) db.setTransactionSuccessful()
            Log.i(TAG, "Seed Data executado com sucesso no NutriDisco!")
        } catch (e: Exception) {
            // Só chega aqui por erro real (asset ausente/corrompido), não por dados já existentes.
            Log.e(TAG, "Falha crítica ao executar o Seed Data: ${e.message}", e)
            throw e
        } finally {
            if (!jaEmTransacao) db.endTransaction()
        }
    }

    companion object {
        private const val TAG = "NutriDiscoSeed"
        const val ASSET_FILE = "database/seed_nutridisco.sql"

        /** Tabelas preenchidas pelo seed; se qualquer uma estiver vazia, o seed é reexecutado. */
        val TABELAS_SEED = listOf("polos_base", "comunidades", "referencias_antropometricas")

        /**
         * Converte o conteúdo do arquivo de seed nos comandos a executar:
         * remove comentários de linha, separa por ";", mantém só INSERTs e os torna
         * INSERT OR IGNORE. Qualquer outro comando (CREATE, ALTER, DELETE...) é recusado,
         * pois a estrutura do banco pertence ao Room e às Migrations.
         */
        fun prepararStatements(conteudo: String): List<String> =
            separarStatements(conteudo).map { statement ->
                require(REGEX_INSERT.containsMatchIn(statement)) {
                    "O seed deve conter apenas INSERTs. Comando inválido: ${statement.take(60)}"
                }
                statement.replaceFirst(REGEX_INSERT, "INSERT OR IGNORE INTO")
            }

        fun separarStatements(conteudo: String): List<String> =
            conteudo.lineSequence()
                .filterNot { it.trim().startsWith("--") }
                .joinToString("\n")
                .split(";")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

        /** "INSERT INTO" ou "INSERT OR IGNORE INTO" no início do comando. */
        private val REGEX_INSERT = Regex("^INSERT\\s+(OR\\s+IGNORE\\s+)?INTO\\b", RegexOption.IGNORE_CASE)
    }
}
