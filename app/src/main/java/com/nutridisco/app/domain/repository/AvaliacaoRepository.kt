package com.nutridisco.app.domain.repository

import com.nutridisco.app.data.local.entity.AvaliacaoEntity
import com.nutridisco.app.data.local.model.AvaliacaoDetalhada
import kotlinx.coroutines.flow.Flow

interface AvaliacaoRepository {
    fun listarPorCrianca(criancaId: Long): Flow<List<AvaliacaoEntity>>
    fun buscarUltimaAvaliacao(criancaId: Long): Flow<AvaliacaoEntity?>
    fun listarTodasDetalhadasFlow(): Flow<List<AvaliacaoDetalhada>>
    suspend fun listarTodasParaExportacao(): List<AvaliacaoDetalhada>
    suspend fun salvarAvaliacao(avaliacao: AvaliacaoEntity): Long
    suspend fun deletarAvaliacao(avaliacao: AvaliacaoEntity)
}
