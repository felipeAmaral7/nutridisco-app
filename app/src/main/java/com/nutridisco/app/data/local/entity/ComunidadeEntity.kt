package com.nutridisco.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa as comunidades indígenas vinculadas a um Polo Base.
 * Permite inserção pré-carregada e criação on-the-fly pelo usuário.
 */
@Entity(
    tableName = "comunidades",
    foreignKeys = [
        ForeignKey(
            entity = PoloBaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["polo_base_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["polo_base_id"]),
        Index(value = ["polo_base_id", "nome"], unique = true)
    ]
)
data class ComunidadeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "polo_base_id")
    val poloBaseId: Long,

    @ColumnInfo(name = "nome")
    val nome: String
)
