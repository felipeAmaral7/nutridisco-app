package com.nutridisco.app.data.local.model

import androidx.room.ColumnInfo

data class CriancaComComunidade(
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "nome")
    val nome: String,

    @ColumnInfo(name = "sexo")
    val sexo: String,

    @ColumnInfo(name = "data_nascimento")
    val dataNascimento: Long,

    @ColumnInfo(name = "nome_responsavel")
    val nomeResponsavel: String,

    @ColumnInfo(name = "numero_cns")
    val numeroCns: String?,

    @ColumnInfo(name = "comunidade_id")
    val comunidadeId: Long,

    @ColumnInfo(name = "comunidade_nome")
    val comunidadeNome: String,

    @ColumnInfo(name = "polo_base_id")
    val poloBaseId: Long,

    @ColumnInfo(name = "polo_base_nome")
    val poloBaseNome: String
)
