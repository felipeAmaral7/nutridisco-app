package com.nutridisco.app.data.local.migrations

import androidx.room.migration.Migration

/**
 * Migrações explícitas do banco do NutriDisco.
 *
 * O app é 100% offline: os dados das crianças e avaliações existem apenas no aparelho.
 * Por isso o banco NUNCA é recriado em atualizações — toda mudança de versão precisa de
 * uma [Migration] aqui, que altera o schema preservando os dados.
 *
 * Como adicionar uma migração (ex.: versão 2 → 3):
 * ```
 * val MIGRATION_2_3 = object : Migration(2, 3) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         db.execSQL("ALTER TABLE avaliacoes ADD COLUMN nova_coluna TEXT")
 *     }
 * }
 * ```
 * e inclua `MIGRATION_2_3` em [TODAS]. Compare o SQL com o JSON gerado em
 * `app/schemas/` para a nova versão e, se possível, teste com `MigrationTestHelper`
 * (androidTest) — os schemas já estão disponíveis como assets de teste.
 *
 * Sem a migração, o app falha ao abrir o banco (os dados continuam intactos no aparelho
 * e podem ser recuperados com uma nova versão), em vez de apagar tudo silenciosamente.
 */
object Migrations {

    /** Todas as migrações, em ordem. Versão atual: [com.nutridisco.app.data.local.AppDatabase.VERSION]. */
    val TODAS: Array<Migration> = arrayOf()
}
