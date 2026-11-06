package com.nutridisco.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nutridisco.app.data.local.entity.ComunidadeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComunidadeDao {

    @Query("SELECT * FROM comunidades WHERE polo_base_id = :poloBaseId ORDER BY nome ASC")
    fun listarPorPoloBase(poloBaseId: Long): Flow<List<ComunidadeEntity>>

    @Query("SELECT * FROM comunidades ORDER BY nome ASC")
    fun listarTodas(): Flow<List<ComunidadeEntity>>

    @Query("SELECT * FROM comunidades WHERE polo_base_id = :poloBaseId AND LOWER(nome) = LOWER(:nome) LIMIT 1")
    suspend fun buscarPorNomeEPolo(poloBaseId: Long, nome: String): ComunidadeEntity?

    @Query("SELECT * FROM comunidades WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): ComunidadeEntity?

    @Query("SELECT * FROM comunidades WHERE LOWER(nome) LIKE '%' || LOWER(:termo) || '%' ORDER BY nome ASC")
    fun buscarPorTermo(termo: String): Flow<List<ComunidadeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserir(comunidade: ComunidadeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(comunidades: List<ComunidadeEntity>)
}
