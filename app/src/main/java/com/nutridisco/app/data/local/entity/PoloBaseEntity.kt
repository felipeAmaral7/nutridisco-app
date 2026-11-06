package com.nutridisco.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa os Polos Base territoriais do DSEI Leste de Roraima.
 * Carregados via Seed Data.
 */
@Entity(tableName = "polos_base")
data class PoloBaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "nome")
    val nome: String,

    @ColumnInfo(name = "etnorregiao")
    val etnorregiao: String
)
