package com.nutridisco.app.presentation.salvarregistro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutridisco.app.data.local.entity.ComunidadeEntity
import com.nutridisco.app.presentation.cadastro.CadastroCriancaUiState
import com.nutridisco.app.presentation.cadastro.CadastroCriancaViewModel
import com.nutridisco.app.presentation.theme.StatusRed

private val VerdeTopBar = Color(0xFF2E7D32)
private val VerdeBotao = Color(0xFF2E7D32)
private val FundoBege = Color(0xFFF4F0E8)
private val TextoPrimario = Color(0xFF1F1F1F)
private val TextoSecundario = Color(0xFF6B6B6B)
private val TextoPlaceholder = Color(0xFF9E9E9E)
private val AvisoFundo = Color(0xFFFFF4E0)
private val AvisoTexto = Color(0xFF8A5300)

/** Dados da Avaliação Rápida que serão gravados junto com a criança. */
data class AvaliacaoPendente(val peso: Double, val estatura: Double)

/**
 * Salva a criança e a avaliação calculada na Avaliação Rápida.
 * Sexo e data de nascimento já vêm da avaliação ([CadastroCriancaViewModel.preencherDadosDaAvaliacao]);
 * o Polo Base é o padrão definido em Ajustes.
 */
@Composable
fun SalvarRegistroScreen(
    viewModel: CadastroCriancaViewModel,
    avaliacao: AvaliacaoPendente,
    onVoltar: () -> Unit,
    onSalvoComSucesso: (criancaId: Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val comunidades by viewModel.comunidadesDoPolo.collectAsState()

    SalvarRegistroConteudo(
        state = state,
        comunidades = comunidades,
        onVoltar = onVoltar,
        onNomeChange = viewModel::onNomeChange,
        onComunidadeChange = viewModel::onComunidadeNomeChange,
        onResponsavelChange = viewModel::onNomeResponsavelChange,
        onCnsChange = viewModel::onNumeroCnsChange,
        onObservacoesChange = viewModel::onObservacoesChange,
        onSalvar = {
            viewModel.salvar(
                dadosAvaliacaoPendente = avaliacao.peso to avaliacao.estatura,
                onSucesso = onSalvoComSucesso,
            )
        },
    )
}

@Composable
private fun SalvarRegistroConteudo(
    state: CadastroCriancaUiState,
    comunidades: List<ComunidadeEntity>,
    onVoltar: () -> Unit,
    onNomeChange: (String) -> Unit,
    onComunidadeChange: (String) -> Unit,
    onResponsavelChange: (String) -> Unit,
    onCnsChange: (String) -> Unit,
    onObservacoesChange: (String) -> Unit,
    onSalvar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoBege)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        TopBarPilula(titulo = "Salvar Registro", onVoltar = onVoltar)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
        ) {
            if (state.poloBasePadraoAusente) {
                Spacer(Modifier.height(16.dp))
                AvisoPoloBase()
            }

            Spacer(Modifier.height(20.dp))
            Rotulo("Nome da criança")
            CampoTexto(
                valor = state.nome,
                onChange = onNomeChange,
                placeholder = "Digite o nome",
                capitalizacao = KeyboardCapitalization.Words,
            )

            Spacer(Modifier.height(18.dp))
            Rotulo("Comunidade / Aldeia")
            CampoComunidade(
                valor = state.comunidadeNome,
                comunidades = comunidades,
                onChange = onComunidadeChange,
            )

            Spacer(Modifier.height(18.dp))
            Rotulo("Nome do responsável")
            CampoTexto(
                valor = state.nomeResponsavel,
                onChange = onResponsavelChange,
                placeholder = "Digite o nome do responsável",
                capitalizacao = KeyboardCapitalization.Words,
            )

            Spacer(Modifier.height(18.dp))
            Rotulo("CNS", opcional = true)
            CampoTexto(
                valor = state.numeroCns,
                onChange = onCnsChange,
                placeholder = "Cartão Nacional de Saúde (15 dígitos)",
                tipoTeclado = KeyboardType.Number,
            )

            Spacer(Modifier.height(18.dp))
            Rotulo("Observações", opcional = true)
            CampoTexto(
                valor = state.observacoes,
                onChange = onObservacoesChange,
                placeholder = "Adicione observações relevantes",
                capitalizacao = KeyboardCapitalization.Sentences,
                linhaUnica = false,
                modifier = Modifier.heightIn(min = 100.dp),
            )

            Spacer(Modifier.height(24.dp))
            AvisoOffline()

            state.erro?.let { erro ->
                Spacer(Modifier.height(16.dp))
                Text(text = erro, color = StatusRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(24.dp))
        }

        Button(
            onClick = onSalvar,
            enabled = !state.salvando,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeBotao,
                contentColor = Color.White,
                disabledContainerColor = VerdeBotao.copy(alpha = 0.5f),
                disabledContentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(52.dp),
        ) {
            if (state.salvando) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                Text("Salvar", fontSize = 18.sp, fontWeight = FontWeight.Medium)
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
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
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
        modifier = Modifier.padding(bottom = 8.dp),
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
private fun CampoTexto(
    valor: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    capitalizacao: KeyboardCapitalization = KeyboardCapitalization.None,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    linhaUnica: Boolean = true,
) {
    TextField(
        value = valor,
        onValueChange = onChange,
        placeholder = { Text(placeholder, fontSize = 13.sp, color = TextoPlaceholder) },
        singleLine = linhaUnica,
        keyboardOptions = KeyboardOptions(capitalization = capitalizacao, keyboardType = tipoTeclado),
        shape = RoundedCornerShape(12.dp),
        colors = coresCampo(),
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
    )
}

/** Campo de texto com sugestões das comunidades do Polo Base; um nome novo é criado ao salvar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampoComunidade(
    valor: String,
    comunidades: List<ComunidadeEntity>,
    onChange: (String) -> Unit,
) {
    var expandido by remember { mutableStateOf(false) }
    val sugestoes = remember(valor, comunidades) {
        if (valor.isBlank()) emptyList()
        else comunidades.filter { it.nome.contains(valor.trim(), ignoreCase = true) && !it.nome.equals(valor.trim(), ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expandido && sugestoes.isNotEmpty(),
        onExpandedChange = { expandido = it },
    ) {
        TextField(
            value = valor,
            onValueChange = {
                onChange(it)
                expandido = true
            },
            placeholder = { Text("Digite a comunidade", fontSize = 13.sp, color = TextoPlaceholder) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            shape = RoundedCornerShape(12.dp),
            colors = coresCampo(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(12.dp)),
        )
        ExposedDropdownMenu(
            expanded = expandido && sugestoes.isNotEmpty(),
            onDismissRequest = { expandido = false },
        ) {
            sugestoes.forEach { comunidade ->
                DropdownMenuItem(
                    text = { Text(comunidade.nome) },
                    onClick = {
                        onChange(comunidade.nome)
                        expandido = false
                    },
                )
            }
        }
    }
}

@Composable
private fun AvisoOffline() {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = null,
                tint = TextoSecundario,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "Salvo localmente no dispositivo. Sem necessidade de internet.",
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                color = TextoSecundario,
            )
        }
    }
}

@Composable
private fun AvisoPoloBase() {
    Surface(color = AvisoFundo, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = AvisoTexto)
            Text(
                text = CadastroCriancaViewModel.ERRO_SEM_POLO_PADRAO,
                fontSize = 13.sp,
                color = AvisoTexto,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun SalvarRegistroScreenPreview() {
    MaterialTheme {
        SalvarRegistroConteudo(
            state = CadastroCriancaUiState(),
            comunidades = emptyList(),
            onVoltar = {},
            onNomeChange = {},
            onComunidadeChange = {},
            onResponsavelChange = {},
            onCnsChange = {},
            onObservacoesChange = {},
            onSalvar = {},
        )
    }
}
