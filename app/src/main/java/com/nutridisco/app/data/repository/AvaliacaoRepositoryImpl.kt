package com.nutridisco.app.data.repository

import com.nutridisco.app.data.local.dao.AvaliacaoDao
import com.nutridisco.app.data.local.entity.AvaliacaoEntity
import com.nutridisco.app.data.local.model.AvaliacaoDetalhada
import com.nutridisco.app.di.IoDispatcher
import com.nutridisco.app.domain.repository.AvaliacaoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AvaliacaoRepositoryImpl @Inject constructor(
    private val dao: AvaliacaoDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AvaliacaoRepository {

    override fun listarPorCrianca(criancaId: Long): Flow<List<AvaliacaoEntity>> =
        dao.listarPorCrianca(criancaId)

    override fun buscarUltimaAvaliacao(criancaId: Long): Flow<AvaliacaoEntity?> =
        dao.buscarUltimaAvaliacao(criancaId)

    override fun listarTodasDetalhadasFlow(): Flow<List<AvaliacaoDetalhada>> =
        dao.listarTodasDetalhadasFlow()

    override suspend fun listarTodasParaExportacao(): List<AvaliacaoDetalhada> =
        withContext(ioDispatcher) {
            dao.listarTodasDetalhadasParaExportacao()
        }

    override suspend fun salvarAvaliacao(avaliacao: AvaliacaoEntity): Long =
        withContext(ioDispatcher) {
            dao.inserir(avaliacao)
        }

    override suspend fun deletarAvaliacao(avaliacao: AvaliacaoEntity) =
        withContext(ioDispatcher) {
            dao.deletar(avaliacao)
        }
}
