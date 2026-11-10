package com.nutridisco.app.domain.model

/**
 * ATENÇÃO: o NOME de cada constante é gravado no banco (coluna avaliacoes.classificacao_estatura).
 * O texto em [descricao] pode ser alterado livremente; já renomear ou remover uma constante
 * exige uma Migration que atualize as avaliações salvas.
 */
enum class ClassificacaoEstatura(val descricao: String, val nivelRisco: NivelRisco) {
    MUITO_BAIXA_ESTATURA("Muito baixa estatura para a idade", NivelRisco.VERMELHO),
    BAIXA_ESTATURA("Baixa estatura para a idade", NivelRisco.AMARELO),
    ESTATURA_ADEQUADA("Estatura adequada para a idade", NivelRisco.VERDE)
}
