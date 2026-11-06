package com.nutridisco.app.domain.repository

import com.nutridisco.app.data.local.entity.ReferenciaAntropometricaEntity

interface ReferenciaAntropometricaRepository {
    suspend fun buscarReferencia(
        tipoParametro: String,
        sexo: String,
        idadeMeses: Int
    ): ReferenciaAntropometricaEntity?

    suspend fun inserirTodas(referencias: List<ReferenciaAntropometricaEntity>)
    suspend fun contar(): Int
}
