package com.nutridisco.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa os dados cadastrais da criança.
 * dataNascimento é gravado em Unix Timestamp (milissegundos) para cálculo de idade exata em meses.
 */
@Entity(
    tableName = "criancas",
    foreignKeys = [
        ForeignKey(
            entity = ComunidadeEntity::class,
            parentColumns = ["id"],
            childColumns = ["comunidade_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["comunidade_id"]),
        Index(value = ["nome"])
    ]
)
data class CriancaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "comunidade_id")
    val comunidadeId: Long,

    @ColumnInfo(name = "nome")
    val nome: String,

    @ColumnInfo(name = "sexo")
    val sexo: String, // 'M' ou 'F'

    @ColumnInfo(name = "data_nascimento")
    val dataNascimento: Long,

    @ColumnInfo(name = "nome_responsavel")
    val nomeResponsavel: String,

    @ColumnInfo(name = "numero_cns")
    val numeroCns: String? = null
)
