package com.nutridisco.app.domain.repository

import com.nutridisco.app.data.local.entity.ComunidadeEntity
import com.nutridisco.app.data.local.entity.PoloBaseEntity
import kotlinx.coroutines.flow.Flow

interface TerritorioRepository {
    fun listarPolosBase(): Flow<List<PoloBaseEntity>>
    suspend fun buscarPoloBasePorId(id: Long): PoloBaseEntity?
    suspend fun inserirPolosBase(polos: List<PoloBaseEntity>)

    fun listarComunidadesPorPolo(poloBaseId: Long): Flow<List<ComunidadeEntity>>
    fun listarTodasComunidades(): Flow<List<ComunidadeEntity>>
    fun buscarComunidadesPorTermo(termo: String): Flow<List<ComunidadeEntity>>
    suspend fun obterOuCriarComunidade(poloBaseId: Long, nomeComunidade: String): Long
    suspend fun buscarComunidadePorId(id: Long): ComunidadeEntity?
    suspend fun inserirComunidades(comunidades: List<ComunidadeEntity>)
}
