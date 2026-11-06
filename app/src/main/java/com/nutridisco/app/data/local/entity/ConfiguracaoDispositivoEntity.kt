package com.nutridisco.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Singleton de configuração local do dispositivo.
 * id é fixado em 1 para garantir apenas um registro no dispositivo.
 * Substitui o sistema de login para trabalho 100% offline.
 */
@Entity(
    tableName = "configuracao_dispositivo",
    foreignKeys = [
        ForeignKey(
            entity = PoloBaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["polo_base_id_padrao"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["polo_base_id_padrao"])]
)
data class ConfiguracaoDispositivoEntity(
    @PrimaryKey
    val id: Long = 1L,

    @ColumnInfo(name = "nome_profissional")
    val nomeProfissional: String,

    @ColumnInfo(name = "registro_coren")
    val registroCoren: String,

    @ColumnInfo(name = "polo_base_id_padrao")
    val poloBaseIdPadrao: Long? = null
)
