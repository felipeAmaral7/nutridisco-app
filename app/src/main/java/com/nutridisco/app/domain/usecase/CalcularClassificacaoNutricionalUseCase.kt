package com.nutridisco.app.domain.usecase

import com.nutridisco.app.data.local.entity.ReferenciaAntropometricaEntity
import com.nutridisco.app.domain.model.ClassificacaoEstatura
import com.nutridisco.app.domain.model.ClassificacaoPeso
import com.nutridisco.app.domain.model.ResultadoClassificacao
import com.nutridisco.app.domain.repository.ReferenciaAntropometricaRepository
import javax.inject.Inject

/**
 * Exceção lançada quando a idade informada ou calculada for igual ou superior a 60 meses (5 anos).
 * O padrão infantil do NutriDisco atende estritamente crianças de 0 a 59 meses completos.
 */
class IdadeLimiteExcedidaException(val idadeMeses: Int) : IllegalArgumentException(
    "A idade informada ($idadeMeses meses) é igual ou superior a 60 meses. " +
    "O NutriDisco é restrito ao acompanhamento de crianças de 0 a 59 meses completos (menores de 5 anos)."
)

/**
 * Exceção lançada quando a tabela de referências antropométricas da OMS não possui dados
 * cadastrados para a idade e sexo informados. Impede falso-negativo silencioso (classificar
 * uma criança em risco como "adequada" por falta de parâmetro de comparação).
 */
class ReferenciaNaoEncontradaException(
    val tipoParametro: String,
    val sexo: String,
    val idadeMeses: Int
) : IllegalStateException(
    "Tabela de referência antropométrica ($tipoParametro) não encontrada para $sexo de $idadeMeses meses. " +
    "Não é possível classificar com segurança."
) {
    companion object {
        /** Mensagem exibida ao profissional (sem detalhes técnicos). */
        const val MENSAGEM_USUARIO =
            "Tabela de referência antropométrica da OMS indisponível para esta idade e sexo. Não foi possível avaliar."
    }
}

class CalcularClassificacaoNutricionalUseCase @Inject constructor(
    private val referenciaRepository: ReferenciaAntropometricaRepository
) {

    suspend operator fun invoke(
        idadeMeses: Int,
        sexo: String,
        peso: Double,
        estatura: Double
    ): ResultadoClassificacao {
        // Validação estrita de idade limite (>= 60 meses bloqueia)
        if (idadeMeses >= IDADE_LIMITE_MESES) {
            throw IdadeLimiteExcedidaException(idadeMeses)
        }
        if (idadeMeses < 0) {
            throw IllegalArgumentException("A idade não pode ser negativa ($idadeMeses meses).")
        }

        val sexoNormalizado = sexo.trim().uppercase()

        val refEstatura = referenciaRepository.buscarReferencia(
            tipoParametro = PARAM_ESTATURA,
            sexo = sexoNormalizado,
            idadeMeses = idadeMeses
        ) ?: throw ReferenciaNaoEncontradaException(PARAM_ESTATURA, sexoNormalizado, idadeMeses)

        val refPeso = referenciaRepository.buscarReferencia(
            tipoParametro = PARAM_PESO,
            sexo = sexoNormalizado,
            idadeMeses = idadeMeses
        ) ?: throw ReferenciaNaoEncontradaException(PARAM_PESO, sexoNormalizado, idadeMeses)

        val classEstatura = classificarEstatura(estatura, refEstatura)
        val classPeso = classificarPeso(peso, refPeso)

        return ResultadoClassificacao(
            idadeMeses = idadeMeses,
            peso = peso,
            estatura = estatura,
            sexo = sexoNormalizado,
            classificacaoEstatura = classEstatura,
            classificacaoPeso = classPeso,
            referenciaEstatura = refEstatura,
            referenciaPeso = refPeso
        )
    }

    companion object {
        const val IDADE_LIMITE_MESES = 60
        const val PARAM_ESTATURA = "ESTATURA"
        const val PARAM_PESO = "PESO"

        /**
         * Regra de classificação Estatura para a Idade:
         * < escore_menos_3 -> Muito baixa estatura para a idade
         * >= escore_menos_3 e < escore_menos_2 -> Baixa estatura para a idade
         * >= escore_menos_2 -> Estatura adequada para a idade
         */
        fun classificarEstatura(
            estatura: Double,
            ref: ReferenciaAntropometricaEntity
        ): ClassificacaoEstatura {
            return when {
                estatura < ref.escoreMenos3 -> ClassificacaoEstatura.MUITO_BAIXA_ESTATURA
                estatura < ref.escoreMenos2 -> ClassificacaoEstatura.BAIXA_ESTATURA
                else -> ClassificacaoEstatura.ESTATURA_ADEQUADA
            }
        }

        /**
         * Regra de classificação Peso para a Idade:
         * < escore_menos_3 -> Muito baixo peso para a idade
         * >= escore_menos_3 e < escore_menos_2 -> Baixo peso para a idade
         * >= escore_menos_2 e <= escore_mais_2 -> Peso adequado para a idade
         * > escore_mais_2 -> Peso Elevado para a idade
         */
        fun classificarPeso(
            peso: Double,
            ref: ReferenciaAntropometricaEntity
        ): ClassificacaoPeso {
            val escoreMais2 = ref.escoreMais2
            return when {
                peso < ref.escoreMenos3 -> ClassificacaoPeso.MUITO_BAIXO_PESO
                peso < ref.escoreMenos2 -> ClassificacaoPeso.BAIXO_PESO
                escoreMais2 != null && peso > escoreMais2 -> ClassificacaoPeso.PESO_ELEVADO
                else -> ClassificacaoPeso.PESO_ADEQUADO
            }
        }
    }
}
