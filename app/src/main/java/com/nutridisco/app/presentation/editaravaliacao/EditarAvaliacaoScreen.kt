package com.nutridisco.app.presentation.editaravaliacao

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutridisco.app.presentation.components.TopBarPilula
import com.nutridisco.app.presentation.theme.PrimaryGreen
import com.nutridisco.app.presentation.theme.StatusRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val VerdeTopBar = Color(0xFF2E7D32)
private val VerdeBotao = Color(0xFF2E7D32)
private val FundoBege = Color(0xFFF4F0E8)
private val TextoPrimario = Color(0xFF1F1F1F)
private val TextoSecundario = Color(0xFF6B6B6B)
private val TextoPlaceholder = Color(0xFF9E9E9E)

@Composable
fun EditarAvaliacaoScreen(
    viewModel: EditarAvaliacaoViewModel,
    onVoltar: () -> Unit,
    onSalvoComSucesso: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    if (state.carregando) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FundoBege),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = VerdeBotao)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoBege)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        TopBarPilula(titulo = "Editar Avaliação", onVoltar = onVoltar)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            // Card resumo do contexto da avaliação
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = state.criancaNome,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Idade no atendimento: ${state.idadeMesesMomento} meses (${if (state.criancaSexo == "F") "Menina" else "Menino"})",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Data original: ${dateFormat.format(Date(state.dataAtendimento))}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (state.nomeProfissional.isNotBlank()) {
                        Text(
                            text = "Avaliador: ${state.nomeProfissional} (${state.registroCoren})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Rotulo("Peso (kg)")
            CampoNumerico(
                valor = state.peso,
                onChange = viewModel::onPesoChange,
                placeholder = "10,0",
                sufixo = "kg"
            )

            Spacer(Modifier.height(18.dp))

            Rotulo("Estatura (cm)")
            CampoNumerico(
                valor = state.estatura,
                onChange = viewModel::onEstaturaChange,
                placeholder = "82,0",
                sufixo = "cm"
            )

            Spacer(Modifier.height(18.dp))

            Rotulo("Observações", opcional = true)
            CampoTexto(
                valor = state.observacoes,
                onChange = viewModel::onObservacoesChange,
                placeholder = "Adicione observações relevantes",
                capitalizacao = KeyboardCapitalization.Sentences,
                linhaUnica = false,
                modifier = Modifier.heightIn(min = 90.dp)
            )

            state.erro?.let { erro ->
                Spacer(Modifier.height(16.dp))
                Text(
                    text = erro,
                    color = StatusRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        Button(
            onClick = { viewModel.salvar(onSucesso = onSalvoComSucesso) },
            enabled = !state.salvando,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeBotao,
                contentColor = Color.White,
                disabledContainerColor = VerdeBotao.copy(alpha = 0.5f),
                disabledContentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(52.dp)
        ) {
            if (state.salvando) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                Text("Salvar Alterações", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun Rotulo(texto: String, opcional: Boolean = false) {
    Text(
        text = buildAnnotatedString {
            append(texto)
            if (opcional) {
                withStyle(SpanStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal, color = TextoSecundario)) {
                    append(" (opcional)")
                }
            }
        },
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextoPrimario,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

@Composable
private fun coresCampo() = TextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    cursorColor = VerdeBotao,
)

@Composable
private fun CampoNumerico(
    valor: String,
    onChange: (String) -> Unit,
    placeholder: String,
    sufixo: String
) {
    TextField(
        value = valor,
        onValueChange = onChange,
        placeholder = { Text(placeholder, color = TextoPlaceholder) },
        suffix = { Text(sufixo, fontSize = 12.sp, color = TextoSecundario) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(12.dp),
        colors = coresCampo(),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
    )
}

@Composable
private fun CampoTexto(
    valor: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    capitalizacao: KeyboardCapitalization = KeyboardCapitalization.None,
    linhaUnica: Boolean = true
) {
    TextField(
        value = valor,
        onValueChange = onChange,
        placeholder = { Text(placeholder, fontSize = 13.sp, color = TextoPlaceholder) },
        singleLine = linhaUnica,
        keyboardOptions = KeyboardOptions(capitalization = capitalizacao),
        shape = RoundedCornerShape(12.dp),
        colors = coresCampo(),
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
    )
}
