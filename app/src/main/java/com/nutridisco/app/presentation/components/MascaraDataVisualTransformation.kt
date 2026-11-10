package com.nutridisco.app.presentation.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.nutridisco.app.domain.util.DataUtils

/**
 * Exibe até 8 dígitos como dd/mm/aaaa sem alterar o valor real do campo (só dígitos).
 * Assim o cursor não "pula" quando as barras são inseridas.
 */
object MascaraDataVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text
        val mascarado = DataUtils.aplicarMascara(digitos)

        val mapeamento = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val barras = (if (offset > 2) 1 else 0) + (if (offset > 4) 1 else 0)
                return (offset + barras).coerceAtMost(mascarado.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val original = when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else -> offset - 2
                }
                return original.coerceIn(0, digitos.length)
            }
        }
        return TransformedText(AnnotatedString(mascarado), mapeamento)
    }
}
