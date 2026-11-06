package com.nutridisco.app.domain.repository

import com.nutridisco.app.data.local.entity.ConfiguracaoDispositivoEntity
import kotlinx.coroutines.flow.Flow

interface ConfiguracaoRepository {
    fun getConfiguracaoFlow(): Flow<ConfiguracaoDispositivoEntity?>
    suspend fun getConfiguracao(): ConfiguracaoDispositivoEntity?
    suspend fun salvarConfiguracao(configuracao: ConfiguracaoDispositivoEntity)
}
