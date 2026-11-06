package com.nutridisco.app.domain.model

enum class ClassificacaoEstatura(val descricao: String, val nivelRisco: NivelRisco) {
    MUITO_BAIXA_ESTATURA("Muito baixa estatura para a idade", NivelRisco.VERMELHO),
    BAIXA_ESTATURA("Baixa estatura para a idade", NivelRisco.AMARELO),
    ESTATURA_ADEQUADA("Estatura adequada para a idade", NivelRisco.VERDE);

    companion object {
        fun fromDescricao(descricao: String): ClassificacaoEstatura? {
            return entries.firstOrNull { it.descricao.equals(descricao, ignoreCase = true) }
        }
    }
}
