package com.nutridisco.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nutridisco.app.data.local.entity.AvaliacaoEntity
import com.nutridisco.app.data.local.model.AvaliacaoDetalhada
import kotlinx.coroutines.flow.Flow

@Dao
interface AvaliacaoDao {

    @Query("SELECT * FROM avaliacoes WHERE crianca_id = :criancaId ORDER BY data_atendimento DESC")
    fun listarPorCrianca(criancaId: Long): Flow<List<AvaliacaoEntity>>

    @Query("SELECT * FROM avaliacoes WHERE crianca_id = :criancaId ORDER BY data_atendimento DESC LIMIT 1")
    fun buscarUltimaAvaliacao(criancaId: Long): Flow<AvaliacaoEntity?>

    @Query("$SELECT_AVALIACAO_DETALHADA ORDER BY a.data_atendimento DESC")
    fun listarTodasDetalhadasFlow(): Flow<List<AvaliacaoDetalhada>>

    @Query("$SELECT_AVALIACAO_DETALHADA ORDER BY a.data_atendimento ASC")
    suspend fun listarTodasDetalhadasParaExportacao(): List<AvaliacaoDetalhada>

    /** ABORT: inserir com um id já existente é erro de programação e não deve sobrescrever a avaliação. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(avaliacao: AvaliacaoEntity): Long

    @Delete
    suspend fun deletar(avaliacao: AvaliacaoEntity)
}

/**
 * Consulta base da avaliação com criança, comunidade e polo, compartilhada pela listagem
 * e pela exportação para as duas nunca divergirem. Cada consulta acrescenta seu ORDER BY.
 */
private const val SELECT_AVALIACAO_DETALHADA = """
    SELECT
        a.id AS avaliacao_id,
        pb.nome AS polo_base_nome,
        com.nome AS comunidade_nome,
        a.nome_profissional AS nome_profissional,
        a.registro_coren AS registro_coren,
        c.id AS crianca_id,
        c.nome AS crianca_nome,
        c.sexo AS sexo,
        c.data_nascimento AS data_nascimento,
        a.data_atendimento AS data_atendimento,
        a.idade_meses_momento AS idade_meses_momento,
        a.peso AS peso,
        a.estatura AS estatura,
        a.classificacao_estatura AS classificacao_estatura,
        a.classificacao_peso AS classificacao_peso,
        a.observacoes AS observacoes
    FROM avaliacoes a
    INNER JOIN criancas c ON a.crianca_id = c.id
    INNER JOIN comunidades com ON a.comunidade_atendimento_id = com.id
    INNER JOIN polos_base pb ON com.polo_base_id = pb.id
"""
