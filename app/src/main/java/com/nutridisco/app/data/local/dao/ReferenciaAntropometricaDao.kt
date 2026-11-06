package com.nutridisco.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nutridisco.app.data.local.entity.ReferenciaAntropometricaEntity

@Dao
interface ReferenciaAntropometricaDao {

    @Query(
        """
        SELECT * FROM referencias_antropometricas 
        WHERE tipo_parametro = :tipoParametro 
          AND sexo = :sexo 
          AND idade_meses = :idadeMeses 
        LIMIT 1
        """
    )
    suspend fun buscarReferencia(
        tipoParametro: String,
        sexo: String,
        idadeMeses: Int
    ): ReferenciaAntropometricaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(referencias: List<ReferenciaAntropometricaEntity>)

    @Query("SELECT COUNT(*) FROM referencias_antropometricas")
    suspend fun contar(): Int
}
