package com.nutridisco.app.domain.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.nutridisco.app.data.local.model.AvaliacaoDetalhada
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class PdfExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())

    open suspend fun gerarPdfAvaliacoes(avaliacoes: List<AvaliacaoDetalhada>): File =
        withContext(Dispatchers.IO) {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val timestamp = fileDateFormat.format(Date())
            val file = File(exportDir, "nutridisco_relatorio_$timestamp.pdf")

            val document = PdfDocument()
            val totalPaginas = calcularTotalPaginas(avaliacoes.size)

            val paint = Paint().apply { isAntiAlias = true }
            var itemIndex = 0

            for (numPagina in 1..totalPaginas) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, numPagina).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                var currentY = MARGIN_TOP

                if (numPagina == 1) {
                    currentY = desenharCabecalhoPrincipal(canvas, paint, avaliacoes, currentY)
                } else {
                    currentY = desenharCabecalhoSecundario(canvas, paint, currentY)
                }

                currentY = desenharCabecalhoTabela(canvas, paint, currentY)

                val limiteItens = if (numPagina == 1) ITENS_PAGINA_1 else ITENS_DEMAIS_PAGINAS
                var itensDesenhadosNaPagina = 0

                while (itemIndex < avaliacoes.size && itensDesenhadosNaPagina < limiteItens) {
                    val item = avaliacoes[itemIndex]
                    val isPar = itensDesenhadosNaPagina % 2 == 1
                    currentY = desenharLinhaTabela(canvas, paint, item, currentY, isPar)
                    itemIndex++
                    itensDesenhadosNaPagina++
                }

                desenharRodape(canvas, paint, numPagina, totalPaginas)

                document.finishPage(page)
            }

            FileOutputStream(file).use { fos ->
                document.writeTo(fos)
            }
            document.close()

            file
        }

    open fun criarIntentCompartilhamento(file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Relatório de Avaliações NutriDisco - DSEI Leste")
            putExtra(
                Intent.EXTRA_TEXT,
                "Segue em anexo o relatório em PDF das avaliações antropométricas do NutriDisco."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return Intent.createChooser(intent, "Compartilhar Relatório PDF via:")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun desenharCabecalhoPrincipal(
        canvas: android.graphics.Canvas,
        paint: Paint,
        avaliacoes: List<AvaliacaoDetalhada>,
        startY: Float
    ): Float {
        // Banner verde superior
        paint.color = COLOR_PRIMARY
        canvas.drawRoundRect(MARGIN_LEFT, startY, PAGE_WIDTH - MARGIN_RIGHT, startY + 54f, 8f, 8f, paint)

        // Título e subtítulo no banner
        paint.color = COLOR_WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        canvas.drawText("NUTRIDISCO — AVALIAÇÃO NUTRICIONAL INDÍGENA", MARGIN_LEFT + 12f, startY + 22f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9.5f
        canvas.drawText("DSEI Leste de Roraima • Relatório Antropométrico Consolidado (0 a 59 meses)", MARGIN_LEFT + 12f, startY + 40f, paint)

        val emissionDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        paint.textSize = 8.5f
        canvas.drawText("Emitido em: $emissionDate", PAGE_WIDTH - MARGIN_RIGHT - 130f, startY + 22f, paint)

        // Resumo estatístico
        val resumoY = startY + 62f
        paint.color = COLOR_CARD_BG
        canvas.drawRoundRect(MARGIN_LEFT, resumoY, PAGE_WIDTH - MARGIN_RIGHT, resumoY + 26f, 6f, 6f, paint)

        val meninos = avaliacoes.count { it.sexo.equals("M", ignoreCase = true) }
        val meninas = avaliacoes.count { it.sexo.equals("F", ignoreCase = true) }

        paint.color = COLOR_TEXT_DARK
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Total de atendimentos: ${avaliacoes.size}", MARGIN_LEFT + 10f, resumoY + 16f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("•  Meninos: $meninos   •  Meninas: $meninas", MARGIN_LEFT + 150f, resumoY + 16f, paint)
        canvas.drawText("Operação 100% Offline", PAGE_WIDTH - MARGIN_RIGHT - 110f, resumoY + 16f, paint)

        return resumoY + 34f
    }

    private fun desenharCabecalhoSecundario(
        canvas: android.graphics.Canvas,
        paint: Paint,
        startY: Float
    ): Float {
        paint.color = COLOR_PRIMARY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("NutriDisco — Relatório Antropométrico (Continuação)", MARGIN_LEFT, startY + 12f, paint)

        paint.color = COLOR_BORDER
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN_LEFT, startY + 18f, PAGE_WIDTH - MARGIN_RIGHT, startY + 18f, paint)

        return startY + 24f
    }

    private fun desenharCabecalhoTabela(
        canvas: android.graphics.Canvas,
        paint: Paint,
        startY: Float
    ): Float {
        val rowHeight = 20f
        paint.color = COLOR_PRIMARY
        canvas.drawRect(MARGIN_LEFT, startY, PAGE_WIDTH - MARGIN_RIGHT, startY + rowHeight, paint)

        paint.color = COLOR_WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.5f

        canvas.drawText("DATA", COL_DATA, startY + 13f, paint)
        canvas.drawText("CRIANÇA (SEXO)", COL_NOME, startY + 13f, paint)
        canvas.drawText("IDADE", COL_IDADE, startY + 13f, paint)
        canvas.drawText("COMUNIDADE / POLO", COL_COMUNIDADE, startY + 13f, paint)
        canvas.drawText("PESO (KG)", COL_PESO, startY + 13f, paint)
        canvas.drawText("ESTATURA (CM)", COL_ESTATURA, startY + 13f, paint)

        return startY + rowHeight
    }

    private fun desenharLinhaTabela(
        canvas: android.graphics.Canvas,
        paint: Paint,
        item: AvaliacaoDetalhada,
        startY: Float,
        isPar: Boolean
    ): Float {
        val rowHeight = 24f

        if (isPar) {
            paint.color = COLOR_ROW_ALT
            canvas.drawRect(MARGIN_LEFT, startY, PAGE_WIDTH - MARGIN_RIGHT, startY + rowHeight, paint)
        }

        // Borda inferior da linha
        paint.color = COLOR_BORDER
        paint.strokeWidth = 0.5f
        canvas.drawLine(MARGIN_LEFT, startY + rowHeight, PAGE_WIDTH - MARGIN_RIGHT, startY + rowHeight, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        paint.color = COLOR_TEXT_DARK

        // 1. Data
        val dataStr = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(item.dataAtendimento))
        canvas.drawText(dataStr, COL_DATA, startY + 14f, paint)

        // 2. Criança (Sexo)
        val sexoSigla = if (item.sexo.equals("F", ignoreCase = true)) "F" else "M"
        val nomeFormatado = truncarTexto("${item.criancaNome} ($sexoSigla)", 22)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(nomeFormatado, COL_NOME, startY + 14f, paint)

        // 3. Idade
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("${item.idadeMesesMomento} m", COL_IDADE, startY + 14f, paint)

        // 4. Comunidade / Polo
        val comFormatada = truncarTexto("${item.comunidadeNome} - ${item.poloBaseNome}", 22)
        canvas.drawText(comFormatada, COL_COMUNIDADE, startY + 14f, paint)

        // 5. Peso + Classificação
        val pesoStr = String.format(Locale.US, "%.2f", item.peso).replace('.', ',')
        val statusPesoCurto = resumirStatus(item.classificacaoPeso.descricao)
        canvas.drawText("$pesoStr ($statusPesoCurto)", COL_PESO, startY + 14f, paint)

        // 6. Estatura + Classificação
        val estStr = String.format(Locale.US, "%.1f", item.estatura).replace('.', ',')
        val statusEstCurto = resumirStatus(item.classificacaoEstatura.descricao)
        canvas.drawText("$estStr ($statusEstCurto)", COL_ESTATURA, startY + 14f, paint)

        return startY + rowHeight
    }

    private fun desenharRodape(
        canvas: android.graphics.Canvas,
        paint: Paint,
        paginaAtual: Int,
        totalPaginas: Int
    ) {
        val footerY = PAGE_HEIGHT - 22f
        paint.color = COLOR_BORDER
        paint.strokeWidth = 0.5f
        canvas.drawLine(MARGIN_LEFT, footerY - 8f, PAGE_WIDTH - MARGIN_RIGHT, footerY - 8f, paint)

        paint.color = COLOR_TEXT_MUTED
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("NutriDisco • Aplicativo de Avaliação Nutricional (UFAM / DSEI Leste de Roraima)", MARGIN_LEFT, footerY + 4f, paint)

        val pagText = "Página $paginaAtual de $totalPaginas"
        canvas.drawText(pagText, PAGE_WIDTH - MARGIN_RIGHT - 60f, footerY + 4f, paint)
    }

    companion object {
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842

        const val MARGIN_LEFT = 28f
        const val MARGIN_RIGHT = 28f
        const val MARGIN_TOP = 28f

        const val ITENS_PAGINA_1 = 23
        const val ITENS_DEMAIS_PAGINAS = 28

        // Posições X das colunas
        const val COL_DATA = 34f
        const val COL_NOME = 86f
        const val COL_IDADE = 224f
        const val COL_COMUNIDADE = 260f
        const val COL_PESO = 380f
        const val COL_ESTATURA = 475f

        const val COLOR_PRIMARY = 0xFF26732B.toInt()
        const val COLOR_WHITE = 0xFFFFFFFF.toInt()
        const val COLOR_CARD_BG = 0xFFF4F0E8.toInt()
        const val COLOR_ROW_ALT = 0xFFFAF8F4.toInt()
        const val COLOR_BORDER = 0xFFE0DCD2.toInt()
        const val COLOR_TEXT_DARK = 0xFF1F1F1F.toInt()
        const val COLOR_TEXT_MUTED = 0xFF757575.toInt()

        fun calcularTotalPaginas(totalItens: Int): Int {
            if (totalItens <= 0) return 1
            if (totalItens <= ITENS_PAGINA_1) return 1
            val restantes = totalItens - ITENS_PAGINA_1
            return 1 + kotlin.math.ceil(restantes.toDouble() / ITENS_DEMAIS_PAGINAS).toInt()
        }

        fun truncarTexto(texto: String, maxCaracteres: Int): String {
            return if (texto.length > maxCaracteres) {
                texto.take(maxCaracteres - 1) + "…"
            } else {
                texto
            }
        }

        fun resumirStatus(descricao: String): String {
            return when {
                descricao.contains("Muito baixa", ignoreCase = true) -> "M. Baixa"
                descricao.contains("Baixa", ignoreCase = true) -> "Baixa"
                descricao.contains("Adequada", ignoreCase = true) || descricao.contains("Adequado", ignoreCase = true) -> "Adequado"
                descricao.contains("Muito baixo", ignoreCase = true) -> "M. Baixo"
                descricao.contains("Baixo peso", ignoreCase = true) -> "B. Peso"
                descricao.contains("Elevado", ignoreCase = true) -> "Elevado"
                else -> truncarTexto(descricao, 10)
            }
        }
    }
}
