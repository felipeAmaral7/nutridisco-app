package com.nutridisco.app.data.repository

import com.nutridisco.app.data.local.dao.CriancaDao
import com.nutridisco.app.data.local.entity.CriancaEntity
import com.nutridisco.app.data.local.model.CriancaComComunidade
import com.nutridisco.app.di.IoDispatcher
import com.nutridisco.app.domain.repository.CriancaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CriancaRepositoryImpl @Inject constructor(
    private val dao: CriancaDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CriancaRepository {

    override fun listarCriancas(): Flow<List<CriancaComComunidade>> =
        dao.listarTodasComComunidade()

    override fun buscarCriancaPorIdFlow(id: Long): Flow<CriancaComComunidade?> =
        dao.buscarPorIdComComunidadeFlow(id)

    override suspend fun buscarCriancaPorId(id: Long): CriancaEntity? =
        withContext(ioDispatcher) {
            dao.buscarPorId(id)
        }

    override suspend fun buscarCriancaPorCns(cns: String): CriancaEntity? =
        withContext(ioDispatcher) {
            dao.buscarPorCns(cns)
        }

    override fun pesquisarCriancas(termo: String): Flow<List<CriancaComComunidade>> =
        dao.pesquisarPorNome(termo)

    override suspend fun salvarCrianca(crianca: CriancaEntity): Long =
        withContext(ioDispatcher) {
            dao.inserir(crianca)
        }

    override suspend fun atualizarCrianca(crianca: CriancaEntity) =
        withContext(ioDispatcher) {
            dao.atualizar(crianca)
        }

    override suspend fun deletarCrianca(crianca: CriancaEntity) =
        withContext(ioDispatcher) {
            dao.deletar(crianca)
        }
}
