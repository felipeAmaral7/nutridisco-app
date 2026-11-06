package com.nutridisco.app.data.repository

import com.nutridisco.app.data.local.dao.ConfiguracaoDispositivoDao
import com.nutridisco.app.data.local.entity.ConfiguracaoDispositivoEntity
import com.nutridisco.app.di.IoDispatcher
import com.nutridisco.app.domain.repository.ConfiguracaoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ConfiguracaoRepositoryImpl @Inject constructor(
    private val dao: ConfiguracaoDispositivoDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ConfiguracaoRepository {

    override fun getConfiguracaoFlow(): Flow<ConfiguracaoDispositivoEntity?> =
        dao.getConfiguracaoFlow()

    override suspend fun getConfiguracao(): ConfiguracaoDispositivoEntity? =
        withContext(ioDispatcher) {
            dao.getConfiguracao()
        }

    override suspend fun salvarConfiguracao(configuracao: ConfiguracaoDispositivoEntity) =
        withContext(ioDispatcher) {
            dao.salvarConfiguracao(configuracao)
        }
}
