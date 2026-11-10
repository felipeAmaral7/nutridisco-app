package com.nutridisco.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nutridisco.app.data.local.dao.AvaliacaoDao
import com.nutridisco.app.data.local.dao.ComunidadeDao
import com.nutridisco.app.data.local.dao.ConfiguracaoDispositivoDao
import com.nutridisco.app.data.local.dao.CriancaDao
import com.nutridisco.app.data.local.dao.PoloBaseDao
import com.nutridisco.app.data.local.dao.ReferenciaAntropometricaDao
import com.nutridisco.app.data.local.entity.AvaliacaoEntity
import com.nutridisco.app.data.local.entity.ComunidadeEntity
import com.nutridisco.app.data.local.entity.ConfiguracaoDispositivoEntity
import com.nutridisco.app.data.local.entity.CriancaEntity
import com.nutridisco.app.data.local.entity.PoloBaseEntity
import com.nutridisco.app.data.local.entity.ReferenciaAntropometricaEntity

@Database(
    entities = [
        ConfiguracaoDispositivoEntity::class,
        PoloBaseEntity::class,
        ComunidadeEntity::class,
        CriancaEntity::class,
        AvaliacaoEntity::class,
        ReferenciaAntropometricaEntity::class
    ],
    version = AppDatabase.VERSION,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun configuracaoDispositivoDao(): ConfiguracaoDispositivoDao
    abstract fun poloBaseDao(): PoloBaseDao
    abstract fun comunidadeDao(): ComunidadeDao
    abstract fun criancaDao(): CriancaDao
    abstract fun avaliacaoDao(): AvaliacaoDao
    abstract fun referenciaAntropometricaDao(): ReferenciaAntropometricaDao

    companion object {
        const val DATABASE_NAME = "nutridisco.db"

        /**
         * Versão atual do schema. Ao alterar qualquer entidade:
         * 1. incremente este valor;
         * 2. crie a Migration correspondente em [com.nutridisco.app.data.local.migrations.Migrations];
         * 3. rode o build para gerar o novo JSON em app/schemas/ e faça commit dele.
         * O teste MigrationsTest falha se a migração ou o schema estiverem faltando.
         */
        const val VERSION = 2
    }
}
