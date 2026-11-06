package com.nutridisco.app.domain.model

enum class ClassificacaoPeso(val descricao: String, val nivelRisco: NivelRisco) {
    MUITO_BAIXO_PESO("Muito baixo peso para a idade", NivelRisco.VERMELHO),
    BAIXO_PESO("Baixo peso para a idade", NivelRisco.AMARELO),
    PESO_ADEQUADO("Peso adequado para a idade", NivelRisco.VERDE),
    PESO_ELEVADO("Peso Elevado para a idade", NivelRisco.VERMELHO);

    companion object {
        fun fromDescricao(descricao: String): ClassificacaoPeso? {
            return entries.firstOrNull { it.descricao.equals(descricao, ignoreCase = true) }
        }
    }
}
