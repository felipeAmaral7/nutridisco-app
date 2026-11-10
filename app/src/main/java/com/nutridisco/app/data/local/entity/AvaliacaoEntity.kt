package com.nutridisco.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nutridisco.app.domain.model.ClassificacaoEstatura
import com.nutridisco.app.domain.model.ClassificacaoPeso

/**
 * Representa uma avaliação antropométrica pontual.
 * Mantém snapshot do profissional e comunidade para auditoria e histórico inalterável.
 *
 * As classificações são gravadas pelo nome do enum (ex.: "BAIXA_ESTATURA"), não pelo texto
 * exibido — assim mudar o texto de uma classificação não invalida avaliações antigas.
 */
@Entity(
    tableName = "avaliacoes",
    foreignKeys = [
        ForeignKey(
            entity = CriancaEntity::class,
            parentColumns = ["id"],
            childColumns = ["crianca_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ComunidadeEntity::class,
            parentColumns = ["id"],
            childColumns = ["comunidade_atendimento_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["crianca_id"]),
        Index(value = ["comunidade_atendimento_id"]),
        Index(value = ["data_atendimento"])
    ]
)
data class AvaliacaoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "crianca_id")
    val criancaId: Long,

    @ColumnInfo(name = "comunidade_atendimento_id")
    val comunidadeAtendimentoId: Long,

    @ColumnInfo(name = "data_atendimento")
    val dataAtendimento: Long,

    @ColumnInfo(name = "idade_meses_momento")
    val idadeMesesMomento: Int,

    @ColumnInfo(name = "peso")
    val peso: Double,

    @ColumnInfo(name = "estatura")
    val estatura: Double,

    @ColumnInfo(name = "classificacao_estatura")
    val classificacaoEstatura: ClassificacaoEstatura,

    @ColumnInfo(name = "classificacao_peso")
    val classificacaoPeso: ClassificacaoPeso,

    @ColumnInfo(name = "observacoes")
    val observacoes: String? = null,

    @ColumnInfo(name = "nome_profissional")
    val nomeProfissional: String,

    @ColumnInfo(name = "registro_coren")
    val registroCoren: String
)
