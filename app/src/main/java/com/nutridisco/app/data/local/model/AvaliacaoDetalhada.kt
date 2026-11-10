package com.nutridisco.app.data.local.model

import androidx.room.ColumnInfo
import com.nutridisco.app.domain.model.ClassificacaoEstatura
import com.nutridisco.app.domain.model.ClassificacaoPeso

/**
 * Modelo de projeção para listagens completas e exportação CSV.
 */
data class AvaliacaoDetalhada(
    @ColumnInfo(name = "avaliacao_id")
    val avaliacaoId: Long,

    @ColumnInfo(name = "polo_base_nome")
    val poloBaseNome: String,

    @ColumnInfo(name = "comunidade_nome")
    val comunidadeNome: String,

    @ColumnInfo(name = "nome_profissional")
    val nomeProfissional: String,

    @ColumnInfo(name = "registro_coren")
    val registroCoren: String,

    @ColumnInfo(name = "crianca_id")
    val criancaId: Long,

    @ColumnInfo(name = "crianca_nome")
    val criancaNome: String,

    @ColumnInfo(name = "sexo")
    val sexo: String,

    @ColumnInfo(name = "data_nascimento")
    val dataNascimento: Long,

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
    val observacoes: String?
)
