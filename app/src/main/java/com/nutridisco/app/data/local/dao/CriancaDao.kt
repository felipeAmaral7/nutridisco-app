package com.nutridisco.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nutridisco.app.data.local.entity.CriancaEntity
import com.nutridisco.app.data.local.model.CriancaComComunidade
import kotlinx.coroutines.flow.Flow

@Dao
interface CriancaDao {

    @Query(
        """
        SELECT 
            c.id AS id,
            c.nome AS nome,
            c.sexo AS sexo,
            c.data_nascimento AS data_nascimento,
            c.nome_responsavel AS nome_responsavel,
            c.numero_cns AS numero_cns,
            com.id AS comunidade_id,
            com.nome AS comunidade_nome,
            pb.id AS polo_base_id,
            pb.nome AS polo_base_nome
        FROM criancas c
        INNER JOIN comunidades com ON c.comunidade_id = com.id
        INNER JOIN polos_base pb ON com.polo_base_id = pb.id
        ORDER BY c.nome ASC
        """
    )
    fun listarTodasComComunidade(): Flow<List<CriancaComComunidade>>

    @Query(
        """
        SELECT 
            c.id AS id,
            c.nome AS nome,
            c.sexo AS sexo,
            c.data_nascimento AS data_nascimento,
            c.nome_responsavel AS nome_responsavel,
            c.numero_cns AS numero_cns,
            com.id AS comunidade_id,
            com.nome AS comunidade_nome,
            pb.id AS polo_base_id,
            pb.nome AS polo_base_nome
        FROM criancas c
        INNER JOIN comunidades com ON c.comunidade_id = com.id
        INNER JOIN polos_base pb ON com.polo_base_id = pb.id
        WHERE c.id = :id
        LIMIT 1
        """
    )
    fun buscarPorIdComComunidadeFlow(id: Long): Flow<CriancaComComunidade?>

    @Query("SELECT * FROM criancas WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): CriancaEntity?

    @Query(
        """
        SELECT 
            c.id AS id,
            c.nome AS nome,
            c.sexo AS sexo,
            c.data_nascimento AS data_nascimento,
            c.nome_responsavel AS nome_responsavel,
            c.numero_cns AS numero_cns,
            com.id AS comunidade_id,
            com.nome AS comunidade_nome,
            pb.id AS polo_base_id,
            pb.nome AS polo_base_nome
        FROM criancas c
        INNER JOIN comunidades com ON c.comunidade_id = com.id
        INNER JOIN polos_base pb ON com.polo_base_id = pb.id
        WHERE LOWER(c.nome) LIKE '%' || LOWER(:termo) || '%'
           OR LOWER(c.nome_responsavel) LIKE '%' || LOWER(:termo) || '%'
        ORDER BY c.nome ASC
        """
    )
    fun pesquisarPorNome(termo: String): Flow<List<CriancaComComunidade>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(crianca: CriancaEntity): Long

    @Update
    suspend fun atualizar(crianca: CriancaEntity)

    @Delete
    suspend fun deletar(crianca: CriancaEntity)
}
