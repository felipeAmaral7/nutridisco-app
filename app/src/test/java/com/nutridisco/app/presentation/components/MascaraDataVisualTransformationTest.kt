package com.nutridisco.app.presentation.components

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

class MascaraDataVisualTransformationTest {

    @Test
    fun `deve exibir os digitos com barras`() {
        val t = MascaraDataVisualTransformation.filter(AnnotatedString("12022025"))
        assertEquals("12/02/2025", t.text.text)
    }

    @Test
    fun `mapeamento de cursor deve ser consistente em todas as posicoes`() {
        for (tamanho in 0..8) {
            val digitos = "12022025".take(tamanho)
            val t = MascaraDataVisualTransformation.filter(AnnotatedString(digitos))
            val exibido = t.text.text.length
            for (o in 0..tamanho) {
                val transformado = t.offsetMapping.originalToTransformed(o)
                assert(transformado in 0..exibido) { "original $o -> $transformado fora de 0..$exibido" }
                assertEquals(o, t.offsetMapping.transformedToOriginal(transformado))
            }
            for (x in 0..exibido) {
                assert(t.offsetMapping.transformedToOriginal(x) in 0..tamanho)
            }
        }
    }
}
