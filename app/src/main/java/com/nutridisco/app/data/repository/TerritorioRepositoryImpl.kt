package com.nutridisco.app.data.repository

import com.nutridisco.app.data.local.dao.ComunidadeDao
import com.nutridisco.app.data.local.dao.PoloBaseDao
import com.nutridisco.app.data.local.entity.ComunidadeEntity
import com.nutridisco.app.data.local.entity.PoloBaseEntity
import com.nutridisco.app.di.IoDispatcher
import com.nutridisco.app.domain.repository.TerritorioRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TerritorioRepositoryImpl @Inject constructor(
    private val poloBaseDao: PoloBaseDao,
    private val comunidadeDao: ComunidadeDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TerritorioRepository {

    override fun listarPolosBase(): Flow<List<PoloBaseEntity>> =
        poloBaseDao.listarTodos()

    override suspend fun buscarPoloBasePorId(id: Long): PoloBaseEntity? =
        withContext(ioDispatcher) {
            poloBaseDao.buscarPorId(id)
        }

    override suspend fun inserirPolosBase(polos: List<PoloBaseEntity>) =
        withContext(ioDispatcher) {
            poloBaseDao.inserirTodos(polos)
        }

    override fun listarComunidadesPorPolo(poloBaseId: Long): Flow<List<ComunidadeEntity>> =
        comunidadeDao.listarPorPoloBase(poloBaseId)

    override fun listarTodasComunidades(): Flow<List<ComunidadeEntity>> =
        comunidadeDao.listarTodas()

    override fun buscarComunidadesPorTermo(termo: String): Flow<List<ComunidadeEntity>> =
        comunidadeDao.buscarPorTermo(termo)

    override suspend fun obterOuCriarComunidade(poloBaseId: Long, nomeComunidade: String): Long =
        withContext(ioDispatcher) {
            val nomeNormalizado = nomeComunidade.trim()
            val existente = comunidadeDao.buscarPorNomeEPolo(poloBaseId, nomeNormalizado)
            if (existente != null) {
                existente.id
            } else {
                comunidadeDao.inserir(
                    ComunidadeEntity(
                        poloBaseId = poloBaseId,
                        nome = nomeNormalizado
                    )
                )
            }
        }

    override suspend fun buscarComunidadePorId(id: Long): ComunidadeEntity? =
        withContext(ioDispatcher) {
            comunidadeDao.buscarPorId(id)
        }

    override suspend fun inserirComunidades(comunidades: List<ComunidadeEntity>) =
        withContext(ioDispatcher) {
            comunidadeDao.inserirTodas(comunidades)
        }
}
