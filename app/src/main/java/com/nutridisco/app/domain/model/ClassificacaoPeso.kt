package com.nutridisco.app.domain.model

/**
 * ATENÇÃO: o NOME de cada constante é gravado no banco (coluna avaliacoes.classificacao_peso).
 * O texto em [descricao] pode ser alterado livremente; já renomear ou remover uma constante
 * exige uma Migration que atualize as avaliações salvas.
 */
enum class ClassificacaoPeso(val descricao: String, val nivelRisco: NivelRisco) {
    MUITO_BAIXO_PESO("Muito baixo peso para a idade", NivelRisco.VERMELHO),
    BAIXO_PESO("Baixo peso para a idade", NivelRisco.AMARELO),
    PESO_ADEQUADO("Peso adequado para a idade", NivelRisco.VERDE),
    PESO_ELEVADO("Peso Elevado para a idade", NivelRisco.VERMELHO)
}
