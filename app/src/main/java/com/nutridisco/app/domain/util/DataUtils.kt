package com.nutridisco.app.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

/**
 * Conversões de datas digitadas no formato brasileiro (dd/MM/aaaa).
 * As datas são gravadas como o início do dia (meia-noite) no fuso informado.
 */
object DataUtils {

    const val TOTAL_DIGITOS = 8

    private val formatoDigitos = DateTimeFormatter.ofPattern("ddMMuuuu").withResolverStyle(ResolverStyle.STRICT)
    private val formatoExibicao = DateTimeFormatter.ofPattern("dd/MM/uuuu")

    /** Mantém só os dígitos, limitado a 8 (ddMMaaaa). */
    fun somenteDigitos(texto: String): String = texto.filter(Char::isDigit).take(TOTAL_DIGITOS)

    /** "12022025" -> "12/02/2025"; aceita entrada parcial ("120" -> "12/0"). */
    fun aplicarMascara(digitos: String): String = buildString {
        somenteDigitos(digitos).forEachIndexed { i, c ->
            if (i == 2 || i == 4) append('/')
            append(c)
        }
    }

    /**
     * Converte "12/02/2025" ou "12022025" em milissegundos (meia-noite no fuso [zoneId]).
     * Retorna null para data incompleta ou inexistente (ex.: 31/02/2025).
     */
    fun parse(texto: String, zoneId: ZoneId = ZoneId.systemDefault()): Long? {
        val digitos = texto.filter(Char::isDigit)
        if (digitos.length != TOTAL_DIGITOS) return null
        return try {
            LocalDate.parse(digitos, formatoDigitos).atStartOfDay(zoneId).toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) {
            null
        }
    }

    fun formatar(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate().format(formatoExibicao)
}
