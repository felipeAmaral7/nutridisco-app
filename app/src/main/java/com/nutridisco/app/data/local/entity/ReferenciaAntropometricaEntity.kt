package com.nutridisco.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabela de Consulta (Lookup Table) pré-carregada com os padrões da OMS/SISVAN (0 a 60 meses).
 * Evita cálculos complexos de Box-Cox/LMS no dispositivo offline.
 */
@Entity(
    tableName = "referencias_antropometricas",
    indices = [
        Index(
            value = ["tipo_parametro", "sexo", "idade_meses"],
            unique = true
        )
    ]
)
data class ReferenciaAntropometricaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "tipo_parametro")
    val tipoParametro: String, // "PESO" ou "ESTATURA"

    @ColumnInfo(name = "sexo")
    val sexo: String, // "M" ou "F"

    @ColumnInfo(name = "idade_meses")
    val idadeMeses: Int,

    @ColumnInfo(name = "escore_menos_3")
    val escoreMenos3: Double,

    @ColumnInfo(name = "escore_menos_2")
    val escoreMenos2: Double,

    @ColumnInfo(name = "escore_mais_2")
    val escoreMais2: Double
)
