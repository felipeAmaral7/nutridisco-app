package com.nutridisco.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nutridisco.app.data.local.entity.ConfiguracaoDispositivoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracaoDispositivoDao {

    @Query("SELECT * FROM configuracao_dispositivo WHERE id = 1 LIMIT 1")
    fun getConfiguracaoFlow(): Flow<ConfiguracaoDispositivoEntity?>

    @Query("SELECT * FROM configuracao_dispositivo WHERE id = 1 LIMIT 1")
    suspend fun getConfiguracao(): ConfiguracaoDispositivoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarConfiguracao(configuracao: ConfiguracaoDispositivoEntity)
}
