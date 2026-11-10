package com.nutridisco.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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

    /**
     * 2 → 3: as classificações passam a ser gravadas pelo nome do enum
     * (ex.: "BAIXA_ESTATURA") em vez do texto exibido ("Baixa estatura para a idade").
     * O schema não muda; apenas os dados de `avaliacoes` são convertidos.
     *
     * Os textos abaixo são os que a versão 2 gravava e ficam congelados aqui de propósito:
     * a migração não pode depender do texto atual dos enums, que pode mudar no futuro.
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            ESTATURA_V2.forEach { (texto, nome) ->
                db.execSQL(
                    "UPDATE avaliacoes SET classificacao_estatura = ? " +
                        "WHERE LOWER(TRIM(classificacao_estatura)) = LOWER(?)",
                    arrayOf(nome, texto)
                )
            }
            PESO_V2.forEach { (texto, nome) ->
                db.execSQL(
                    "UPDATE avaliacoes SET classificacao_peso = ? " +
                        "WHERE LOWER(TRIM(classificacao_peso)) = LOWER(?)",
                    arrayOf(nome, texto)
                )
            }

            // Segurança: qualquer valor não reconhecido é recalculado a partir das medidas
            // salvas e da tabela da OMS, com as mesmas regras do
            // CalcularClassificacaoNutricionalUseCase (sem referência → adequado).
            db.execSQL(
                """
                UPDATE avaliacoes SET classificacao_estatura = COALESCE((
                    SELECT CASE
                        WHEN avaliacoes.estatura < r.escore_menos_3 THEN 'MUITO_BAIXA_ESTATURA'
                        WHEN avaliacoes.estatura < r.escore_menos_2 THEN 'BAIXA_ESTATURA'
                        ELSE 'ESTATURA_ADEQUADA'
                    END
                    FROM referencias_antropometricas r
                    JOIN criancas c ON c.id = avaliacoes.crianca_id
                    WHERE r.tipo_parametro = 'ESTATURA'
                      AND r.sexo = UPPER(TRIM(c.sexo))
                      AND r.idade_meses = avaliacoes.idade_meses_momento
                ), 'ESTATURA_ADEQUADA')
                WHERE classificacao_estatura NOT IN (${ESTATURA_V2.values.joinToString { "'$it'" }})
                """.trimIndent()
            )
            db.execSQL(
                """
                UPDATE avaliacoes SET classificacao_peso = COALESCE((
                    SELECT CASE
                        WHEN avaliacoes.peso < r.escore_menos_3 THEN 'MUITO_BAIXO_PESO'
                        WHEN avaliacoes.peso < r.escore_menos_2 THEN 'BAIXO_PESO'
                        WHEN r.escore_mais_2 IS NOT NULL AND avaliacoes.peso > r.escore_mais_2 THEN 'PESO_ELEVADO'
                        ELSE 'PESO_ADEQUADO'
                    END
                    FROM referencias_antropometricas r
                    JOIN criancas c ON c.id = avaliacoes.crianca_id
                    WHERE r.tipo_parametro = 'PESO'
                      AND r.sexo = UPPER(TRIM(c.sexo))
                      AND r.idade_meses = avaliacoes.idade_meses_momento
                ), 'PESO_ADEQUADO')
                WHERE classificacao_peso NOT IN (${PESO_V2.values.joinToString { "'$it'" }})
                """.trimIndent()
            )
        }
    }

    /** Texto gravado na versão 2 → nome do enum gravado a partir da versão 3. */
    private val ESTATURA_V2 = linkedMapOf(
        "Muito baixa estatura para a idade" to "MUITO_BAIXA_ESTATURA",
        "Baixa estatura para a idade" to "BAIXA_ESTATURA",
        "Estatura adequada para a idade" to "ESTATURA_ADEQUADA",
    )

    private val PESO_V2 = linkedMapOf(
        "Muito baixo peso para a idade" to "MUITO_BAIXO_PESO",
        "Baixo peso para a idade" to "BAIXO_PESO",
        "Peso adequado para a idade" to "PESO_ADEQUADO",
        "Peso Elevado para a idade" to "PESO_ELEVADO",
    )

    /** Todas as migrações, em ordem. Versão atual: [com.nutridisco.app.data.local.AppDatabase.VERSION]. */
    val TODAS: Array<Migration> = arrayOf(MIGRATION_2_3)
}
