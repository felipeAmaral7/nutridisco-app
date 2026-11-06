package com.nutridisco.app.presentation.novaavaliacao

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutridisco.app.domain.util.IdadeUtils
import com.nutridisco.app.presentation.components.StatusBadge
import com.nutridisco.app.presentation.theme.PrimaryGreen
import com.nutridisco.app.presentation.theme.StatusRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaAvaliacaoScreen(
    viewModel: NovaAvaliacaoViewModel,
    onVoltar: () -> Unit,
    onSalvo: () -> Unit
) {
    val crianca by viewModel.crianca.collectAsState()
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova Avaliação", fontWeight = FontWeight.Bold) },
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
        if (crianca == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Carregando...")
            }
        } else {
            val c = crianca!!
            val idadeMeses = IdadeUtils.calcularIdadeEmMesesCompletos(c.dataNascimento)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header com dados pré-fixados
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = c.nome,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Idade Atual: ${IdadeUtils.formatarIdadeExtenso(c.dataNascimento)} ($idadeMeses meses completos)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Sexo: ${if (c.sexo == "M") "Masculino" else "Feminino"} • Comunidade: ${c.comunidadeNome}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.peso,
                        onValueChange = viewModel::onPesoChange,
                        label = { Text("Peso (kg) *") },
                        placeholder = { Text("Ex: 11.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state.estatura,
                        onValueChange = viewModel::onEstaturaChange,
                        label = { Text("Estatura (cm) *") },
                        placeholder = { Text("Ex: 82.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = state.observacoes,
                    onValueChange = viewModel::onObservacoesChange,
                    label = { Text("Observações Clínicas (Opcional)") },
                    placeholder = { Text("Ex: Criança em tratamento de anemia, presença de febre, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                state.erro?.let { erro ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = erro,
                        color = StatusRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card de resultado dinâmico
                state.resultado?.let { res ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Classificação em Tempo Real (OMS)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("ESTATURA PARA A IDADE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(2.dp))
                            StatusBadge(
                                texto = res.classificacaoEstatura.descricao,
                                nivelRisco = res.classificacaoEstatura.nivelRisco
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("PESO PARA A IDADE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(2.dp))
                            StatusBadge(
                                texto = res.classificacaoPeso.descricao,
                                nivelRisco = res.classificacaoPeso.nivelRisco
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (idadeMeses >= 60) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "⚠️ Atenção: Criança com 5 anos ou mais (≥ 60 meses)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "O disco de avaliação do NutriDisco atende estritamente a faixa de 0 a 59 meses completos. Crianças com 60 meses ou mais devem ser avaliadas pelos protocolos de idade escolar.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = { viewModel.salvarAvaliacao(onSalvo) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !state.salvando && idadeMeses < 60,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = if (state.salvando) "Salvando..." else "Salvar Avaliação no Perfil",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
