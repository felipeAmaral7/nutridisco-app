package com.nutridisco.app.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object IdadeUtils {

    /**
     * Calcula a idade estritamente em meses completos entre duas datas timestamp em milissegundos.
     * Exemplo: Uma criança com 2 meses e 28 dias terá resultado = 2.
     */
    fun calcularIdadeEmMesesCompletos(
        dataNascimentoMillis: Long,
        dataReferenciaMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Int {
        val dataNasc = Instant.ofEpochMilli(dataNascimentoMillis).atZone(zoneId).toLocalDate()
        val dataRef = Instant.ofEpochMilli(dataReferenciaMillis).atZone(zoneId).toLocalDate()

        if (dataRef.isBefore(dataNasc)) return 0

        return ChronoUnit.MONTHS.between(dataNasc, dataRef).toInt()
    }

    /**
     * Retorna a descrição amigável da idade (ex.: "1 ano e 3 meses", "5 meses", recém-nascido).
     */
    fun formatarIdadeExtenso(
        dataNascimentoMillis: Long,
        dataReferenciaMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val dataNasc = Instant.ofEpochMilli(dataNascimentoMillis).atZone(zoneId).toLocalDate()
        val dataRef = Instant.ofEpochMilli(dataReferenciaMillis).atZone(zoneId).toLocalDate()

        if (dataRef.isBefore(dataNasc)) return "Data futura inválida"

        val periodo = Period.between(dataNasc, dataRef)
        val anos = periodo.years
        val meses = periodo.months
        val dias = periodo.days

        return when {
            anos > 0 && meses > 0 -> "$anos ${if (anos == 1) "ano" else "anos"} e $meses ${if (meses == 1) "mês" else "meses"}"
            anos > 0 -> "$anos ${if (anos == 1) "ano" else "anos"}"
            meses > 0 -> "$meses ${if (meses == 1) "mês" else "meses"}"
            dias > 0 -> "$dias ${if (dias == 1) "dia" else "dias"}"
            else -> "Recém-nascido"
        }
    }
}
