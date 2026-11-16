package com.nutridisco.app.domain.model

/**
 * Parâmetros de avaliação antropométrica fornecidos durante o fluxo de cadastro da criança.
 * Utilizado para persistência atômica da criança e seu primeiro atendimento.
 */
data class DadosNovaAvaliacao(
    val peso: Double,
    val estatura: Double,
    val idadeMesesMomento: Int,
    val classificacaoEstatura: ClassificacaoEstatura,
    val classificacaoPeso: ClassificacaoPeso,
    val observacoes: String? = null,
    val dataAtendimento: Long = System.currentTimeMillis()
)
