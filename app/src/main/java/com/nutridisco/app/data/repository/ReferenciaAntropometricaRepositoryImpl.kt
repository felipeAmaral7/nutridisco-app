package com.nutridisco.app.data.repository

import com.nutridisco.app.data.local.dao.ReferenciaAntropometricaDao
import com.nutridisco.app.data.local.entity.ReferenciaAntropometricaEntity
import com.nutridisco.app.di.IoDispatcher
import com.nutridisco.app.domain.repository.ReferenciaAntropometricaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReferenciaAntropometricaRepositoryImpl @Inject constructor(
    private val dao: ReferenciaAntropometricaDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ReferenciaAntropometricaRepository {

    override suspend fun buscarReferencia(
        tipoParametro: String,
        sexo: String,
        idadeMeses: Int
    ): ReferenciaAntropometricaEntity? = withContext(ioDispatcher) {
        dao.buscarReferencia(
            tipoParametro = tipoParametro,
            sexo = sexo,
            idadeMeses = idadeMeses
        )
    }

    override suspend fun inserirTodas(referencias: List<ReferenciaAntropometricaEntity>) =
        withContext(ioDispatcher) {
            dao.inserirTodas(referencias)
        }

    override suspend fun contar(): Int = withContext(ioDispatcher) {
        dao.contar()
    }
}
