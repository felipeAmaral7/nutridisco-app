package com.nutridisco.app.presentation.avaliacaorapida

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutridisco.app.presentation.components.MascaraDataVisualTransformation
import com.nutridisco.app.presentation.components.TopBarPilula
import com.nutridisco.app.presentation.theme.StatusRed

private val VerdeTopBar = Color(0xFF26732B)
private val VerdeBotao = Color(0xFF2D7A32)
private val FundoBege = Color(0xFFF7F5EE)
private val TextoPrimario = Color(0xFF1F1F1F)
private val TextoSecundario = Color(0xFF6B6B6B)

enum class SexoOpcao(val valorDb: String, val label: String, val emoji: String) {
    FEMININO("F", "Menina", "👧"),
    MASCULINO("M", "Menino", "👦")
}

/**
 * Tela de Avaliação Nutricional unificada: sexo biológico, data de nascimento, peso e estatura.
 * Permite alternar o sexo a qualquer momento sem perder os valores preenchidos.
 */
@Composable
fun AvaliacaoRapidaScreen(
    viewModel: AvaliacaoRapidaViewModel,
    onVoltar: () -> Unit,
    onResultadoCalculado: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.navegarParaResultado) {
        if (state.navegarParaResultado) {
            viewModel.onNavegouParaResultado()
            onResultadoCalculado()
        }
    }

    AvaliacaoRapidaConteudo(
        state = state,
        onVoltar = onVoltar,
        onSexoChange = viewModel::onSexoChange,
        onDataNascimentoChange = viewModel::onDataNascimentoChange,
        onPesoChange = viewModel::onPesoChange,
        onEstaturaChange = viewModel::onEstaturaChange,
        onCalcular = viewModel::calcular,
    )
}

@Composable
private fun AvaliacaoRapidaConteudo(
    state: AvaliacaoRapidaUiState,
    onVoltar: () -> Unit,
    onSexoChange: (String) -> Unit,
    onDataNascimentoChange: (String) -> Unit,
    onPesoChange: (String) -> Unit,
    onEstaturaChange: (String) -> Unit,
    onCalcular: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FundoBege)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        TopBarPilula(titulo = "Nova Avaliação", onVoltar = onVoltar)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(20.dp))
            TituloCampo("Sexo biológico")
            Spacer(Modifier.height(10.dp))
            SeletorSexo(
                sexoSelecionado = state.sexo,
                onSexoSelecionado = onSexoChange,
            )

            Spacer(Modifier.height(22.dp))
            TituloCampo("Data de nascimento")
            Spacer(Modifier.height(10.dp))
            CampoDataNascimento(
                digitos = state.dataNascimento,
                status = state.dataNascimentoStatus,
                onChange = onDataNascimentoChange,
            )

            Spacer(Modifier.height(22.dp))
            TituloCampo("Peso (kg)")
            Spacer(Modifier.height(10.dp))
            CampoNumerico(
                valor = state.peso,
                onChange = onPesoChange,
                placeholder = "10,0",
                sufixo = "kg",
            )

            Spacer(Modifier.height(22.dp))
            TituloCampo("Estatura (cm)")
            Spacer(Modifier.height(10.dp))
            CampoNumerico(
                valor = state.estatura,
                onChange = onEstaturaChange,
                placeholder = "82,0",
                sufixo = "cm",
            )

            state.erro?.let { erro ->
                Spacer(Modifier.height(16.dp))
                Text(
                    text = erro,
                    color = StatusRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        Button(
            onClick = onCalcular,
            enabled = state.podeCalcular,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeBotao,
                contentColor = Color.White,
                disabledContainerColor = VerdeBotao.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.8f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .height(52.dp),
        ) {
            if (state.calculando) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Text("Calcular", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SeletorSexo(
    sexoSelecionado: String,
    onSexoSelecionado: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CardSexo(
            label = "Menina",
            emoji = "👧",
            selecionado = sexoSelecionado == "F",
            onClick = { onSexoSelecionado("F") },
            modifier = Modifier.weight(1f),
        )
        CardSexo(
            label = "Menino",
            emoji = "👦",
            selecionado = sexoSelecionado == "M",
            onClick = { onSexoSelecionado("M") },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CardSexo(
    label: String,
    emoji: String,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selecionado) Color.White else Color.White.copy(alpha = 0.65f),
        border = BorderStroke(
            width = if (selecionado) 2.dp else 1.dp,
            color = if (selecionado) VerdeBotao else Color(0xFFDCD8CF),
        ),
        shadowElevation = if (selecionado) 3.dp else 1.dp,
        modifier = modifier.height(72.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = emoji,
                fontSize = 30.sp,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Medium,
                color = if (selecionado) VerdeBotao else TextoSecundario,
            )
        }
    }
}

@Composable
private fun TituloCampo(texto: String) {
    Text(
        text = texto,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextoPrimario,
    )
}

@Composable
private fun CampoDataNascimento(
    digitos: String,
    status: DataNascimentoStatus,
    onChange: (String) -> Unit,
) {
    val erro = when (status) {
        DataNascimentoStatus.Invalida -> "Data inexistente. Confira dia, mês e ano."
        DataNascimentoStatus.Futura -> "A data de nascimento não pode ser no futuro."
        is DataNascimentoStatus.ForaDaFaixa -> AvaliacaoRapidaViewModel.mensagemForaDaFaixa(status.idadeMeses)
        else -> null
    }

    TextField(
        value = digitos,
        onValueChange = onChange,
        placeholder = { Text("dd/mm/aaaa", color = TextoPrimario.copy(alpha = 0.6f)) },
        visualTransformation = MascaraDataVisualTransformation,
        isError = erro != null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            errorContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = StatusRed,
            cursorColor = VerdeBotao,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
    )
    Spacer(Modifier.height(6.dp))
    when {
        erro != null -> Text(text = erro, fontSize = 12.sp, color = StatusRed, fontWeight = FontWeight.Medium)
        status is DataNascimentoStatus.Valida -> Text(
            text = "Idade: ${status.idadeExtenso} (${status.idadeMeses} meses)",
            fontSize = 13.sp,
            color = VerdeBotao,
            fontWeight = FontWeight.Medium,
        )
        else -> Text(text = "Ex: 15/04/2024", fontSize = 11.sp, color = TextoSecundario)
    }
}

@Composable
private fun CampoNumerico(
    valor: String,
    onChange: (String) -> Unit,
    placeholder: String,
    sufixo: String,
) {
    TextField(
        value = valor,
        onValueChange = onChange,
        placeholder = { Text(placeholder, color = TextoPrimario.copy(alpha = 0.6f)) },
        suffix = { Text(sufixo, fontSize = 12.sp, color = TextoSecundario) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = VerdeBotao,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun AvaliacaoRapidaScreenPreview() {
    MaterialTheme {
        AvaliacaoRapidaConteudo(
            state = AvaliacaoRapidaUiState(
                sexo = "F",
                dataNascimento = "10022025",
                dataNascimentoStatus = DataNascimentoStatus.Valida(0L, 21, "1 ano e 9 meses"),
                peso = "12,4",
                estatura = "82,0",
            ),
            onVoltar = {},
            onSexoChange = {},
            onDataNascimentoChange = {},
            onPesoChange = {},
            onEstaturaChange = {},
            onCalcular = {},
        )
    }
}
