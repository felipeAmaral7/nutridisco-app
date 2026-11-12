package com.nutridisco.app.presentation.editarcrianca

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutridisco.app.data.local.entity.ComunidadeEntity
import com.nutridisco.app.data.local.entity.PoloBaseEntity
import com.nutridisco.app.presentation.components.MascaraDataVisualTransformation
import com.nutridisco.app.presentation.theme.StatusRed

private val VerdeTopBar = Color(0xFF2E7D32)
private val VerdeBotao = Color(0xFF2E7D32)
private val FundoBege = Color(0xFFF4F0E8)
private val TextoPrimario = Color(0xFF1F1F1F)
private val TextoSecundario = Color(0xFF6B6B6B)
private val TextoPlaceholder = Color(0xFF9E9E9E)

@Composable
fun EditarCriancaScreen(
    viewModel: EditarCriancaViewModel,
    onVoltar: () -> Unit,
    onSalvoComSucesso: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val polos by viewModel.polosBase.collectAsState()
    val comunidades by viewModel.comunidadesDoPolo.collectAsState()

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
        TopBarPilula(titulo = "Editar Cadastro", onVoltar = onVoltar)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            Rotulo("Nome da criança")
            CampoTexto(
                valor = state.nome,
                onChange = viewModel::onNomeChange,
                placeholder = "Digite o nome da criança",
                capitalizacao = KeyboardCapitalization.Words
            )

            Spacer(Modifier.height(18.dp))

            Rotulo("Sexo biológico")
            SeletorSexo(
                sexoSelecionado = state.sexo,
                onSexoSelecionado = viewModel::onSexoChange
            )

            Spacer(Modifier.height(18.dp))

            Rotulo("Data de nascimento")
            CampoTexto(
                valor = state.dataNascimentoTexto,
                onChange = viewModel::onDataNascimentoChange,
                placeholder = "dd/mm/aaaa",
                tipoTeclado = KeyboardType.Number,
                visualTransformation = MascaraDataVisualTransformation
            )

            Spacer(Modifier.height(18.dp))

            Rotulo("Polo Base")
            CampoPoloBase(
                poloSelecionadoId = state.poloBaseId,
                polos = polos,
                onPoloSelecionado = viewModel::onPoloBaseSelecionado
            )

            Spacer(Modifier.height(18.dp))

            Rotulo("Comunidade / Aldeia")
            CampoComunidade(
                valor = state.comunidadeNome,
                comunidades = comunidades,
                onChange = viewModel::onComunidadeNomeChange
            )

            Spacer(Modifier.height(18.dp))

            Rotulo("Nome do responsável")
            CampoTexto(
                valor = state.nomeResponsavel,
                onChange = viewModel::onNomeResponsavelChange,
                placeholder = "Digite o nome do responsável",
                capitalizacao = KeyboardCapitalization.Words
            )

            Spacer(Modifier.height(18.dp))

            Rotulo("CNS", opcional = true)
            CampoTexto(
                valor = state.numeroCns,
                onChange = viewModel::onNumeroCnsChange,
                placeholder = "Cartão Nacional de Saúde (15 dígitos)",
                tipoTeclado = KeyboardType.Number
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
        modifier = modifier.height(68.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(text = emoji, fontSize = 28.sp)
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
private fun CampoTexto(
    valor: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    capitalizacao: KeyboardCapitalization = KeyboardCapitalization.None,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
) {
    TextField(
        value = valor,
        onValueChange = onChange,
        placeholder = { Text(placeholder, fontSize = 13.sp, color = TextoPlaceholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = capitalizacao, keyboardType = tipoTeclado),
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(12.dp),
        colors = coresCampo(),
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampoPoloBase(
    poloSelecionadoId: Long?,
    polos: List<PoloBaseEntity>,
    onPoloSelecionado: (Long) -> Unit,
) {
    var expandido by remember { mutableStateOf(false) }
    val poloAtual = remember(poloSelecionadoId, polos) {
        polos.firstOrNull { it.id == poloSelecionadoId }
    }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
    ) {
        TextField(
            value = poloAtual?.let { "${it.nome} (${it.etnorregiao})" } ?: "",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Selecione o Polo Base", fontSize = 13.sp, color = TextoPlaceholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = coresCampo(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(12.dp)),
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false },
        ) {
            polos.forEach { polo ->
                DropdownMenuItem(
                    text = { Text("${polo.nome} (${polo.etnorregiao})") },
                    onClick = {
                        onPoloSelecionado(polo.id)
                        expandido = false
                    },
                )
            }
        }
    }
}

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
