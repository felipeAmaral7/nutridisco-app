package com.nutridisco.app.data.local.seed

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Callback do RoomDatabase para executar o script de Seed Data (seed_nutridisco.sql)
 * contendo Polos Base, Comunidades e a Tabela de Referências Antropométricas da OMS.
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
        // Garante que se o banco já existia antes do seed, os dados sejam injetados
        executarSeedSeVazio(db)
    }

    private fun executarSeedSeVazio(db: SupportSQLiteDatabase) {
        try {
            val cursor = db.query("SELECT COUNT(*) FROM referencias_antropometricas")
            val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
            cursor.close()

            if (count == 0) {
                executarSeed(db, "onOpen (banco sem dados)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar contagem de referências: ${e.message}", e)
        }
    }

    private fun executarSeed(db: SupportSQLiteDatabase, origem: String) {
        Log.i(TAG, "Iniciando execução do Seed Data a partir de $origem...")
        try {
            val statements = lerStatementsDoAsset(context, ASSET_FILE)
            Log.d(TAG, "Total de statements lidos do asset: ${statements.size}")

            db.beginTransaction()
            try {
                for (statement in statements) {
                    val trimmed = statement.trim()
                    // Executa apenas os INSERTs, pois o Room já criou o schema DDL
                    if (trimmed.startsWith("INSERT", ignoreCase = true)) {
                        db.execSQL(trimmed)
                    }
                }
                db.setTransactionSuccessful()
                Log.i(TAG, "Seed Data executado com sucesso no NutriDisco!")
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha crítica ao executar o Seed Data: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "NutriDiscoSeed"
        const val ASSET_FILE = "database/seed_nutridisco.sql"

        fun lerStatementsDoAsset(context: Context, assetPath: String): List<String> {
            val inputStream = context.assets.open(assetPath)
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val sb = StringBuilder()

            reader.forEachLine { line ->
                val trimmed = line.trim()
                // Ignora comentários de linha do SQL
                if (!trimmed.startsWith("--")) {
                    sb.append(line).append("\n")
                }
            }
            reader.close()

            return sb.toString()
                .split(";")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }
    }
}
