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
open class CsvExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())

    open suspend fun gerarCsvAvaliacoes(
        avaliacoes: List<AvaliacaoDetalhada>,
        campos: Set<CampoExportacao> = CampoExportacao.PADRAO
    ): File =
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
                    writer.write(gerarConteudoCsv(avaliacoes, campos))
                }
            }

            file
        }

    open fun criarIntentCompartilhamento(file: File): Intent {
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

        fun gerarConteudoCsv(
            avaliacoes: List<AvaliacaoDetalhada>,
            campos: Set<CampoExportacao> = CampoExportacao.PADRAO
        ): String {
            val camposAtivos = if (campos.isEmpty()) CampoExportacao.PADRAO else campos
            val camposOrdenados = CampoExportacao.entries.filter { it in camposAtivos }

            val cabecalhoLista = mutableListOf<String>()
            for (campo in camposOrdenados) {
                campo.cabecalhoCsv.split(";").forEach { cabecalhoLista.add(it) }
            }

            val sb = java.lang.StringBuilder()
            sb.append(cabecalhoLista.joinToString(";") { escapeCsv(it) }).append("\n")

            for (item in avaliacoes) {
                val row = mutableListOf<String>()
                for (campo in camposOrdenados) {
                    when (campo) {
                        CampoExportacao.POLO_BASE -> row.add(item.poloBaseNome)
                        CampoExportacao.COMUNIDADE -> row.add(item.comunidadeNome)
                        CampoExportacao.PROFISSIONAL -> {
                            row.add(item.nomeProfissional)
                            row.add(item.registroCoren)
                        }
                        CampoExportacao.ID_CRIANCA -> row.add(item.criancaId.toString())
                        CampoExportacao.NOME_CRIANCA -> row.add(item.criancaNome)
                        CampoExportacao.SEXO -> row.add(item.sexo)
                        CampoExportacao.DATA_NASCIMENTO -> row.add(birthDateFormat.format(Date(item.dataNascimento)))
                        CampoExportacao.DATA_AVALIACAO -> row.add(dateFormat.format(Date(item.dataAtendimento)))
                        CampoExportacao.IDADE_MESES -> row.add(item.idadeMesesMomento.toString())
                        CampoExportacao.PESO -> {
                            row.add(String.format(Locale.US, "%.2f", item.peso))
                            row.add(item.classificacaoPeso.descricao)
                        }
                        CampoExportacao.ESTATURA -> {
                            row.add(String.format(Locale.US, "%.1f", item.estatura))
                            row.add(item.classificacaoEstatura.descricao)
                        }
                        CampoExportacao.RESPONSAVEL -> row.add(item.nomeResponsavel)
                        CampoExportacao.CNS -> row.add(item.numeroCns.orEmpty())
                        CampoExportacao.OBSERVACOES -> row.add(item.observacoes.orEmpty())
                    }
                }
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
