package com.nutridisco.app.presentation.resultado

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutridisco.app.data.local.entity.ReferenciaAntropometricaEntity
import com.nutridisco.app.domain.model.ClassificacaoEstatura
import com.nutridisco.app.domain.model.ClassificacaoPeso
import com.nutridisco.app.domain.model.ResultadoClassificacao
import com.nutridisco.app.domain.util.DataUtils
import com.nutridisco.app.presentation.avaliacaorapida.AvaliacaoRapidaViewModel
import com.nutridisco.app.presentation.ui.SexoOpcao
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val VerdeTopBar = Color(0xFF26732B)
private val VerdeBotao = Color(0xFF2D7A32)
private val FundoBege = Color(0xFFF7F5EE)
private val TextoPrimario = Color(0xFF1F1F1F)
private val TextoSecundario = Color(0xFF4A4A4A)

/**
 * Faixas visuais da legenda do disco. A cor do card segue a faixa (e não o [NivelRisco]),
 * para que "Peso Elevado" apareça em azul, como na legenda do Figma.
 */
private enum class FaixaClassificacao(val rotulo: String, val cor: Color) {
    MUITO_BAIXO("Muito Baixo", Color(0xFFD32F2F)),
    BAIXO("Baixo", Color(0xFFF39C2B)),
    ADEQUADO("Adequado", Color(0xFF43A047)),
    ELEVADO("Elevado", Color(0xFF3F7FD6)),
}

private data class CardClassificacao(
    val titulo: String,
    val criterio: String,
    val orientacao: String,
    val faixa: FaixaClassificacao,
)

private fun ClassificacaoEstatura.toCard(): CardClassificacao = when (this) {
    ClassificacaoEstatura.MUITO_BAIXA_ESTATURA -> CardClassificacao(
        titulo = "Muito Baixa Estatura",
        criterio = "Estatura/Idade: Escore-z < -3",
        orientacao = "Déficit grave de crescimento. Encaminhamento imediato recomendado.",
        faixa = FaixaClassificacao.MUITO_BAIXO,
    )
    ClassificacaoEstatura.BAIXA_ESTATURA -> CardClassificacao(
        titulo = "Baixa Estatura",
        criterio = "Estatura/Idade: Escore-z >= -3 e < -2",
        orientacao = "Déficit de crescimento. Acompanhamento nutricional recomendado.",
        faixa = FaixaClassificacao.BAIXO,
    )
    ClassificacaoEstatura.ESTATURA_ADEQUADA -> CardClassificacao(
        titulo = "Estatura Adequada",
        criterio = "Estatura/Idade: Escore-z >= -2",
        orientacao = "Dentro do padrão esperado.",
        faixa = FaixaClassificacao.ADEQUADO,
    )
}

private fun ClassificacaoPeso.toCard(): CardClassificacao = when (this) {
    ClassificacaoPeso.MUITO_BAIXO_PESO -> CardClassificacao(
        titulo = "Muito Baixo Peso",
        criterio = "Peso/Idade: Escore-z < -3",
        orientacao = "Déficit grave de peso. Encaminhamento imediato recomendado.",
        faixa = FaixaClassificacao.MUITO_BAIXO,
    )
    ClassificacaoPeso.BAIXO_PESO -> CardClassificacao(
        titulo = "Baixo Peso",
        criterio = "Peso/Idade: Escore-z >= -3 e < -2",
        orientacao = "Déficit de peso. Acompanhamento nutricional recomendado.",
        faixa = FaixaClassificacao.BAIXO,
    )
    ClassificacaoPeso.PESO_ADEQUADO -> CardClassificacao(
        titulo = "Peso Adequado",
        criterio = "Peso/Idade: Escore-z >= -2 e <= +2",
        orientacao = "Dentro do padrão esperado.",
        faixa = FaixaClassificacao.ADEQUADO,
    )
    ClassificacaoPeso.PESO_ELEVADO -> CardClassificacao(
        titulo = "Peso Elevado",
        criterio = "Peso/Idade: Escore-z > +2",
        orientacao = "Peso acima do esperado para a idade. Avaliar outros índices.",
        faixa = FaixaClassificacao.ELEVADO,
    )
}

private val formatoDecimal = DecimalFormat("0.##", DecimalFormatSymbols(Locale("pt", "BR")))

/**
 * Tela exibida após "Calcular" na Avaliação Rápida. Compartilha o [AvaliacaoRapidaViewModel]
 * da etapa anterior, de onde lê o [ResultadoClassificacao] já calculado.
 */
@Composable
fun ResultadoScreen(
    viewModel: AvaliacaoRapidaViewModel,
    onVoltar: () -> Unit,
    onNovaAvaliacao: () -> Unit,
    onSalvarRegistro: (resultado: ResultadoClassificacao, dataNascimentoMillis: Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val resultado = state.resultado
    val dataNascimento = state.dataNascimentoResultado

    if (resultado == null || dataNascimento == null) {
        // Só ocorre de forma transitória (ex.: animação de saída); não há o que exibir.
        Box(Modifier.fillMaxSize().background(FundoBege))
        return
    }

    ResultadoConteudo(
        resultado = resultado,
        dataNascimentoMillis = dataNascimento,
        onVoltar = onVoltar,
        onNovaAvaliacao = onNovaAvaliacao,
        onSalvarRegistro = { onSalvarRegistro(resultado, dataNascimento) },
    )
}

@Composable
private fun ResultadoConteudo(
    resultado: ResultadoClassificacao,
    dataNascimentoMillis: Long,
    onVoltar: () -> Unit,
    onNovaAvaliacao: () -> Unit,
    onSalvarRegistro: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoBege)
            .navigationBarsPadding(),
    ) {
        TopBarResultado(onVoltar = onVoltar)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(20.dp))
            CardCrianca(resultado, dataNascimentoMillis)

            Spacer(Modifier.height(28.dp))
            Text(
                text = "Classificação Nutricional",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextoPrimario,
            )

            Spacer(Modifier.height(12.dp))
            Subtitulo("Estatura para Idade")
            Spacer(Modifier.height(8.dp))
            CardResultado(resultado.classificacaoEstatura.toCard())

            Spacer(Modifier.height(16.dp))
            Subtitulo("Peso para Idade")
            Spacer(Modifier.height(8.dp))
            CardResultado(resultado.classificacaoPeso.toCard())

            Spacer(Modifier.height(24.dp))
            LegendaClassificacao()
            Spacer(Modifier.height(24.dp))
        }

        RodapeAcoes(onNovaAvaliacao = onNovaAvaliacao, onSalvarRegistro = onSalvarRegistro)
    }
}

@Composable
private fun TopBarResultado(onVoltar: () -> Unit) {
    Surface(
        color = VerdeTopBar,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onVoltar) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White,
                )
            }
            Text(
                text = "Resultado",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}

@Composable
private fun CardCrianca(resultado: ResultadoClassificacao, dataNascimentoMillis: Long) {
    val sexo = SexoOpcao.entries.firstOrNull { it.valorDb == resultado.sexo } ?: SexoOpcao.MASCULINO
    val anos = resultado.idadeMeses / 12
    val meses = resultado.idadeMeses % 12

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = sexo.emoji, fontSize = 52.sp)
            Spacer(Modifier.size(16.dp))
            Column {
                Text(
                    text = sexo.label,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoPrimario,
                )
                Text(
                    text = "Nascimento: ${DataUtils.formatar(dataNascimentoMillis)}",
                    fontSize = 13.sp,
                    color = TextoSecundario,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DadoCrianca("Idade", "${anos}a ${meses}m")
                    DadoCrianca("Peso", "${formatoDecimal.format(resultado.peso)} kg")
                    DadoCrianca("Estatura", "${formatoDecimal.format(resultado.estatura)} cm")
                }
            }
        }
    }
}

@Composable
private fun DadoCrianca(rotulo: String, valor: String) {
    Column {
        Text(text = rotulo, fontSize = 13.sp, color = TextoSecundario)
        Text(text = valor, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextoPrimario)
    }
}

@Composable
private fun Subtitulo(texto: String) {
    Text(text = texto, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextoPrimario)
}

@Composable
private fun CardResultado(card: CardClassificacao) {
    val icone: ImageVector =
        if (card.faixa == FaixaClassificacao.ADEQUADO) Icons.Filled.CheckCircle else Icons.Filled.Warning

    Surface(
        color = card.faixa.cor,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icone,
                contentDescription = card.faixa.rotulo,
                tint = Color.White,
                modifier = Modifier.size(44.dp),
            )
            Spacer(Modifier.size(16.dp))
            Column {
                Text(
                    text = card.titulo,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(text = card.criterio, color = Color.White, fontSize = 14.sp)
                Text(text = card.orientacao, color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun LegendaClassificacao() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Legenda de Classificação", fontSize = 14.sp, color = TextoPrimario)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50)),
        ) {
            FaixaClassificacao.entries.forEach { faixa ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(faixa.cor),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            FaixaClassificacao.entries.forEach { faixa ->
                Text(
                    text = faixa.rotulo,
                    fontSize = 12.sp,
                    color = TextoPrimario,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun RodapeAcoes(onNovaAvaliacao: () -> Unit, onSalvarRegistro: () -> Unit) {
    Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onNovaAvaliacao,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.5.dp, VerdeBotao),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeBotao),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
            ) {
                Text("Nova Avaliação", fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            }
            Button(
                onClick = onSalvarRegistro,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeBotao, contentColor = Color.White),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
            ) {
                Text("Salvar Registro", fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ResultadoScreenPreview() {
    val ref = ReferenciaAntropometricaEntity(
        id = 0,
        tipoParametro = "ESTATURA",
        sexo = "F",
        idadeMeses = 21,
        escoreMenos3 = 72.0,
        escoreMenos2 = 75.0,
        escoreMais2 = null,
    )
    MaterialTheme {
        ResultadoConteudo(
            resultado = ResultadoClassificacao(
                idadeMeses = 21,
                peso = 10.0,
                estatura = 40.0,
                sexo = "F",
                classificacaoEstatura = ClassificacaoEstatura.MUITO_BAIXA_ESTATURA,
                classificacaoPeso = ClassificacaoPeso.PESO_ADEQUADO,
                referenciaEstatura = ref,
                referenciaPeso = ref,
            ),
            dataNascimentoMillis = DataUtils.parse("10/02/2025")!!,
            onVoltar = {},
            onNovaAvaliacao = {},
            onSalvarRegistro = {},
        )
    }
}
