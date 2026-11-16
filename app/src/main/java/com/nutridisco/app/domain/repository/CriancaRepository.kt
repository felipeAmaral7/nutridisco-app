package com.nutridisco.app.domain.repository

import com.nutridisco.app.data.local.entity.CriancaEntity
import com.nutridisco.app.data.local.model.CriancaComComunidade
import com.nutridisco.app.domain.model.DadosNovaAvaliacao
import kotlinx.coroutines.flow.Flow

interface CriancaRepository {
    fun listarCriancas(): Flow<List<CriancaComComunidade>>
    fun buscarCriancaPorIdFlow(id: Long): Flow<CriancaComComunidade?>
    suspend fun buscarCriancaPorId(id: Long): CriancaEntity?
    suspend fun buscarCriancaPorCns(cns: String): CriancaEntity?
    fun pesquisarCriancas(termo: String): Flow<List<CriancaComComunidade>>
    suspend fun salvarCrianca(crianca: CriancaEntity): Long

    /**
     * Cadastra a criança, vincula ou cria a comunidade no polo base e, se informada,
     * salva a avaliação inicial de forma 100% atômica dentro de uma única transação de banco.
     * Caso ocorra qualquer falha, toda a operação sofre rollback.
     *
     * @return O id gerado para a nova criança cadastrada.
     */
    suspend fun cadastrarCriancaComAvaliacao(
        crianca: CriancaEntity,
        poloBaseId: Long,
        nomeComunidade: String,
        dadosAvaliacao: DadosNovaAvaliacao?
    ): Long = throw UnsupportedOperationException("Operação não implementada.")

    suspend fun atualizarCrianca(crianca: CriancaEntity)
    suspend fun deletarCrianca(crianca: CriancaEntity)
}
