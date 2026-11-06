package com.nutridisco.app.domain.repository

import com.nutridisco.app.data.local.entity.CriancaEntity
import com.nutridisco.app.data.local.model.CriancaComComunidade
import kotlinx.coroutines.flow.Flow

interface CriancaRepository {
    fun listarCriancas(): Flow<List<CriancaComComunidade>>
    fun buscarCriancaPorIdFlow(id: Long): Flow<CriancaComComunidade?>
    suspend fun buscarCriancaPorId(id: Long): CriancaEntity?
    fun pesquisarCriancas(termo: String): Flow<List<CriancaComComunidade>>
    suspend fun salvarCrianca(crianca: CriancaEntity): Long
    suspend fun atualizarCrianca(crianca: CriancaEntity)
    suspend fun deletarCrianca(crianca: CriancaEntity)
}
