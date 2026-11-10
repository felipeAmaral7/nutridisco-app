package com.nutridisco.app.domain.model

import com.nutridisco.app.data.local.entity.ReferenciaAntropometricaEntity

data class ResultadoClassificacao(
    val idadeMeses: Int,
    val peso: Double,
    val estatura: Double,
    val sexo: String,
    val classificacaoEstatura: ClassificacaoEstatura,
    val classificacaoPeso: ClassificacaoPeso,
    val referenciaEstatura: ReferenciaAntropometricaEntity,
    val referenciaPeso: ReferenciaAntropometricaEntity
)
