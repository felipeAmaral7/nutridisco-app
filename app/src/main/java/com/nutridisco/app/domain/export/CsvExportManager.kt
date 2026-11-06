package com.nutridisco.app.domain.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.nutridisco.app.data.local.model.AvaliacaoDetalhada
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())

    suspend fun gerarCsvAvaliacoes(avaliacoes: List<AvaliacaoDetalhada>): File =
        withContext(Dispatchers.IO) {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val timestamp = fileDateFormat.format(Date())
            val file = File(exportDir, "nutridisco_avaliacoes_$timestamp.csv")

            FileOutputStream(file).use { fos ->
                // Escreve UTF-8 BOM para garantir compatibilidade com Microsoft Excel
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    writer.write(gerarConteudoCsv(avaliacoes))
                }
            }

            file
        }

    fun criarIntentCompartilhamento(file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Relatório de Avaliações NutriDisco - DSEI Leste")
            putExtra(
                Intent.EXTRA_TEXT,
                "Segue em anexo o relatório consolidado de avaliações antropométricas do NutriDisco."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return Intent.createChooser(intent, "Compartilhar Relatório NutriDisco via:")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    companion object {
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        private val birthDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val CABECALHO = listOf(
            "Polo Base",
            "Comunidade",
            "Profissional",
            "COREN",
            "ID Crianca",
            "Nome Crianca",
            "Sexo",
            "Data Nascimento",
            "Data Avaliacao",
            "Idade (Meses)",
            "Peso (kg)",
            "Estatura (cm)",
            "Classificacao Estatura",
            "Classificacao Peso",
            "Observacoes"
        )

        fun gerarConteudoCsv(avaliacoes: List<AvaliacaoDetalhada>): String {
            val sb = java.lang.StringBuilder()
            sb.append(CABECALHO.joinToString(";") { escapeCsv(it) }).append("\n")

            for (item in avaliacoes) {
                val row = listOf(
                    item.poloBaseNome,
                    item.comunidadeNome,
                    item.nomeProfissional,
                    item.registroCoren,
                    item.criancaId.toString(),
                    item.criancaNome,
                    item.sexo,
                    birthDateFormat.format(Date(item.dataNascimento)),
                    dateFormat.format(Date(item.dataAtendimento)),
                    item.idadeMesesMomento.toString(),
                    String.format(Locale.US, "%.2f", item.peso),
                    String.format(Locale.US, "%.1f", item.estatura),
                    item.classificacaoEstatura,
                    item.classificacaoPeso,
                    item.observacoes.orEmpty()
                )
                sb.append(row.joinToString(";") { escapeCsv(it) }).append("\n")
            }

            return sb.toString()
        }

        fun escapeCsv(value: String): String {
            var str = value.replace("\"", "\"\"")
            if (str.contains(";") || str.contains("\n") || str.contains("\"")) {
                str = "\"$str\""
            }
            return str
        }
    }
}
