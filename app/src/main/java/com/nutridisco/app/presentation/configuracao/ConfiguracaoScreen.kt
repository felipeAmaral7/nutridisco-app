package com.nutridisco.app.presentation.configuracao

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutridisco.app.presentation.components.TopBarPilula
import com.nutridisco.app.presentation.theme.PrimaryGreen
import com.nutridisco.app.presentation.theme.StatusRed

private val CorFundoBege = Color(0xFFF7F5EE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracaoScreen(
    viewModel: ConfiguracaoViewModel,
    isInicial: Boolean = false,
    onSalvo: () -> Unit,
    onVoltar: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    val polos by viewModel.polosBase.collectAsState()
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CorFundoBege)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        TopBarPilula(
            titulo = if (isInicial) "Identificação do Profissional" else "Configuração do Profissional",
            onVoltar = onVoltar
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isInicial) "Identificação Inicial" else "Identificação Local",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isInicial) {
                            "Bem-vindo ao NutriDisco! Como o aplicativo opera 100% offline, esses dados identificam seus atendimentos e definem o Polo Base padrão para as comunidades de atuação."
                        } else {
                            "Como o NutriDisco opera 100% offline, esses dados serão gravados em cada atendimento para auditoria territorial."
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = state.nomeProfissional,
                onValueChange = viewModel::onNomeProfissionalChange,
                label = { Text("Nome do Profissional / Avaliador *") },
                placeholder = { Text("Ex: Enf. Maria Silva") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.registroCoren,
                onValueChange = viewModel::onRegistroCorenChange,
                label = { Text("Registro Profissional (COREN / CRM) *") },
                placeholder = { Text("Ex: COREN-RR 123456") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (polos.isNotEmpty()) {
                val poloSelecionado = polos.firstOrNull { it.id == state.poloBaseIdPadrao }
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = poloSelecionado?.let { "${it.nome} (${it.etnorregiao})" } ?: "Selecione o Polo Base Padrão",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Polo Base Padrão *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        polos.forEach { polo ->
                            DropdownMenuItem(
                                text = { Text("${polo.nome} - ${polo.etnorregiao}") },
                                onClick = {
                                    viewModel.onPoloBaseSelecionado(polo.id)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            state.erro?.let { erro ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = erro,
                    color = StatusRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (state.salvoComSucesso && !isInicial) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "✓ Configuração salva com sucesso!",
                    color = PrimaryGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.salvarConfiguracao(onSalvo) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (isInicial) "Concluir e Começar" else "Salvar Alterações",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
