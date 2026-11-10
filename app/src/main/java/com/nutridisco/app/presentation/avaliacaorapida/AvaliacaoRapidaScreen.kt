package com.nutridisco.app.presentation.avaliacaorapida

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutridisco.app.domain.model.ResultadoClassificacao
import com.nutridisco.app.presentation.components.StatusBadge
import com.nutridisco.app.presentation.theme.StatusRed
import com.nutridisco.app.presentation.ui.SexoOpcao

private val VerdeTopBar = Color(0xFF26732B)
private val VerdeBotao = Color(0xFF2D7A32)
private val FundoBege = Color(0xFFF7F5EE)
private val TextoPrimario = Color(0xFF1F1F1F)
private val TextoSecundario = Color(0xFF6B6B6B)
private val PontoInativo = Color(0xFFBDBDBD)

/**
 * Etapa 2 da avaliação: dados da criança (idade, peso e estatura).
 * O sexo vem da Etapa 1 (SelecaoSexoScreen) via argumento de navegação.
 */
@Composable
fun AvaliacaoRapidaScreen(
    viewModel: AvaliacaoRapidaViewModel,
    onVoltar: () -> Unit,
    onSalvarRegistro: (sexo: String, idadeMeses: Int, peso: Double, estatura: Double) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    AvaliacaoRapidaConteudo(
        state = state,
        onVoltar = onVoltar,
        onAnosChange = viewModel::onAnosChange,
        onMesesChange = viewModel::onMesesChange,
        onPesoChange = viewModel::onPesoChange,
        onEstaturaChange = viewModel::onEstaturaChange,
        onCalcular = viewModel::calcular,
    )

    state.resultado?.let { res ->
        ResultadoBottomSheet(
            resultado = res,
            onFechar = viewModel::fecharResultado,
            onSalvar = {
                viewModel.fecharResultado()
                onSalvarRegistro(res.sexo, res.idadeMeses, res.peso, res.estatura)
            },
        )
    }
}

@Composable
private fun AvaliacaoRapidaConteudo(
    state: AvaliacaoRapidaUiState,
    onVoltar: () -> Unit,
    onAnosChange: (Int) -> Unit,
    onMesesChange: (Int) -> Unit,
    onPesoChange: (String) -> Unit,
    onEstaturaChange: (String) -> Unit,
    onCalcular: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sexoOpcao = SexoOpcao.entries.firstOrNull { it.valorDb == state.sexo } ?: SexoOpcao.MASCULINO

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FundoBege)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        TopBarPilula(titulo = "Dados da criança", onVoltar = onVoltar)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            ChipSexo(emoji = sexoOpcao.emoji, label = sexoOpcao.label)

            Spacer(Modifier.height(24.dp))
            TituloCampo("Idade")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Stepper(
                    rotulo = "Anos",
                    valor = state.anos,
                    minimo = 0,
                    maximo = AvaliacaoRapidaViewModel.ANOS_MAX,
                    onChange = onAnosChange,
                    modifier = Modifier.weight(1f),
                )
                Stepper(
                    rotulo = "Meses",
                    valor = state.meses,
                    minimo = 0,
                    maximo = AvaliacaoRapidaViewModel.MESES_MAX,
                    onChange = onMesesChange,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Ex: 2 anos e 3 meses",
                fontSize = 11.sp,
                color = TextoSecundario,
            )

            Spacer(Modifier.height(24.dp))
            TituloCampo("Peso (kg)")
            Spacer(Modifier.height(10.dp))
            CampoNumerico(
                valor = state.peso,
                onChange = onPesoChange,
                placeholder = "10,0",
                sufixo = "kg",
            )

            Spacer(Modifier.height(24.dp))
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

        IndicadorEtapas(total = 2, atual = 1)
        Spacer(Modifier.height(16.dp))

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
                .padding(horizontal = 24.dp)
                .height(52.dp),
        ) {
            if (state.calculando) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Text("Calcular", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultadoBottomSheet(
    resultado: ResultadoClassificacao,
    onFechar: () -> Unit,
    onSalvar: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val anos = resultado.idadeMeses / 12
    val meses = resultado.idadeMeses % 12
    val sexoLabel = if (resultado.sexo == "F") "Menina" else "Menino"

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = sheetState,
        containerColor = FundoBege,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = "Resultado da avaliação",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = VerdeTopBar,
            )
            Text(
                text = "$sexoLabel · $anos ano(s) e $meses mes(es) (${resultado.idadeMeses} meses) · " +
                    "Referência OMS/SISVAN",
                fontSize = 12.sp,
                color = TextoSecundario,
            )

            Spacer(Modifier.height(20.dp))
            TituloCampo("Estatura para a idade")
            Spacer(Modifier.height(6.dp))
            StatusBadge(
                texto = resultado.classificacaoEstatura.descricao,
                nivelRisco = resultado.classificacaoEstatura.nivelRisco,
            )

            Spacer(Modifier.height(16.dp))
            TituloCampo("Peso para a idade")
            Spacer(Modifier.height(6.dp))
            StatusBadge(
                texto = resultado.classificacaoPeso.descricao,
                nivelRisco = resultado.classificacaoPeso.nivelRisco,
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onSalvar,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeBotao),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Salvar no histórico da criança", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            TextButton(
                onClick = onFechar,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Fechar", color = VerdeBotao)
            }
        }
    }
}

@Composable
private fun TopBarPilula(titulo: String, onVoltar: () -> Unit) {
    Surface(
        color = VerdeTopBar,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
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
                text = titulo,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun ChipSexo(emoji: String, label: String) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(50),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Text(text = label, fontSize = 12.sp, color = TextoPrimario)
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
private fun Stepper(
    rotulo: String,
    valor: Int,
    minimo: Int,
    maximo: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(text = rotulo, fontSize = 12.sp, color = TextoSecundario)
        Spacer(Modifier.height(6.dp))
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = { onChange((valor - 1).coerceAtLeast(minimo)) },
                    enabled = valor > minimo,
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Diminuir $rotulo")
                }
                Text(
                    text = valor.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextoPrimario,
                    textAlign = TextAlign.Center,
                )
                IconButton(
                    onClick = { onChange((valor + 1).coerceAtMost(maximo)) },
                    enabled = valor < maximo,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Aumentar $rotulo")
                }
            }
        }
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

@Composable
private fun IndicadorEtapas(total: Int, atual: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { indice ->
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(
                        color = if (indice == atual) TextoPrimario else PontoInativo,
                    ),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun AvaliacaoRapidaScreenPreview() {
    MaterialTheme {
        AvaliacaoRapidaConteudo(
            state = AvaliacaoRapidaUiState(sexo = "F", anos = 2, meses = 3, peso = "12,4"),
            onVoltar = {},
            onAnosChange = {},
            onMesesChange = {},
            onPesoChange = {},
            onEstaturaChange = {},
            onCalcular = {},
        )
    }
}
