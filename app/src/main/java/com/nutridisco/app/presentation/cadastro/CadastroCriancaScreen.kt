package com.nutridisco.app.presentation.cadastro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutridisco.app.presentation.theme.PrimaryGreen
import com.nutridisco.app.presentation.theme.StatusRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroCriancaScreen(
    viewModel: CadastroCriancaViewModel,
    dadosAvaliacaoPendente: Pair<Double, Double>? = null,
    onVoltar: () -> Unit,
    onSalvoComSucesso: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val polos by viewModel.polosBase.collectAsState()
    val comunidades by viewModel.comunidadesDoPolo.collectAsState()

    var poloDropdownExpanded by remember { mutableStateOf(false) }
    var comunidadeDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cadastrar Criança", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = state.nome,
                onValueChange = viewModel::onNomeChange,
                label = { Text("Nome Completo da Criança *") },
                placeholder = { Text("Ex: Davi Kopenawa") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sexo
            Text("Sexo Biológico *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = state.sexo == "M",
                    onClick = { viewModel.onSexoChange("M") },
                    enabled = !state.dadosDaAvaliacao,
                    label = { Text("Masculino (Menino)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryGreen,
                        selectedLabelColor = Color.White,
                        disabledSelectedContainerColor = PrimaryGreen.copy(alpha = 0.6f),
                        disabledLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = state.sexo == "F",
                    onClick = { viewModel.onSexoChange("F") },
                    enabled = !state.dadosDaAvaliacao,
                    label = { Text("Feminino (Menina)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryGreen,
                        selectedLabelColor = Color.White,
                        disabledSelectedContainerColor = PrimaryGreen.copy(alpha = 0.6f),
                        disabledLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = state.dataNascimentoTexto,
                onValueChange = viewModel::onDataNascimentoChange,
                label = { Text("Data de Nascimento (DD/MM/AAAA) *") },
                placeholder = { Text("Ex: 15/04/2023") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                readOnly = state.dadosDaAvaliacao,
                supportingText = if (state.dadosDaAvaliacao) {
                    { Text("Informada na avaliação. Para alterar, volte e refaça o cálculo.") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = state.nomeResponsavel,
                onValueChange = viewModel::onNomeResponsavelChange,
                label = { Text("Nome da Mãe ou Responsável *") },
                placeholder = { Text("Ex: Maria Yanomami") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = state.numeroCns,
                onValueChange = viewModel::onNumeroCnsChange,
                label = { Text("Cartão Nacional de Saúde (CNS) - Opcional") },
                placeholder = { Text("Ex: 700000000000000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Polo Base Dropdown
            if (polos.isNotEmpty()) {
                val poloSelecionado = polos.firstOrNull { it.id == state.poloBaseId }
                ExposedDropdownMenuBox(
                    expanded = poloDropdownExpanded,
                    onExpandedChange = { poloDropdownExpanded = !poloDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = poloSelecionado?.let { "${it.nome} (${it.etnorregiao})" } ?: "Selecione o Polo Base",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Polo Base *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = poloDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = poloDropdownExpanded,
                        onDismissRequest = { poloDropdownExpanded = false }
                    ) {
                        polos.forEach { polo ->
                            DropdownMenuItem(
                                text = { Text("${polo.nome} - ${polo.etnorregiao}") },
                                onClick = {
                                    viewModel.onPoloBaseSelecionado(polo.id)
                                    poloDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Comunidade com Autocomplete e criação on-the-fly
            ExposedDropdownMenuBox(
                expanded = comunidadeDropdownExpanded && comunidades.isNotEmpty(),
                onExpandedChange = { comunidadeDropdownExpanded = !comunidadeDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.comunidadeNome,
                    onValueChange = {
                        viewModel.onComunidadeNomeChange(it)
                        comunidadeDropdownExpanded = true
                    },
                    label = { Text("Comunidade / Aldeia *") },
                    placeholder = { Text("Digite para buscar ou criar nova") },
                    trailingIcon = {
                        if (comunidades.isNotEmpty()) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = comunidadeDropdownExpanded)
                        }
                    },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    singleLine = true
                )

                val filtradas = comunidades.filter {
                    it.nome.contains(state.comunidadeNome, ignoreCase = true)
                }

                if (filtradas.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = comunidadeDropdownExpanded,
                        onDismissRequest = { comunidadeDropdownExpanded = false }
                    ) {
                        filtradas.forEach { comunidade ->
                            DropdownMenuItem(
                                text = { Text(comunidade.nome) },
                                onClick = {
                                    viewModel.onComunidadeNomeChange(comunidade.nome)
                                    comunidadeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            state.erro?.let { erro ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = erro,
                    color = StatusRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    viewModel.salvar(
                        dadosAvaliacaoPendente = dadosAvaliacaoPendente,
                        onSucesso = onSalvoComSucesso
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(8.dp),
                enabled = !state.salvando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (state.salvando) "Salvando..." else "Salvar Cadastro",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
