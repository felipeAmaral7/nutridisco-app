package com.nutridisco.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nutridisco.app.data.local.entity.PoloBaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PoloBaseDao {

    @Query("SELECT * FROM polos_base ORDER BY nome ASC")
    fun listarTodos(): Flow<List<PoloBaseEntity>>

    @Query("SELECT * FROM polos_base WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): PoloBaseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(polos: List<PoloBaseEntity>)

    @Query("SELECT COUNT(*) FROM polos_base")
    suspend fun contar(): Int
}
