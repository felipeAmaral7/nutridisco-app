package com.nutridisco.app.presentation.avaliacaorapida

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.nutridisco.app.presentation.components.StatusBadge
import com.nutridisco.app.presentation.theme.PrimaryGreen
import com.nutridisco.app.presentation.theme.StatusRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvaliacaoRapidaScreen(
    viewModel: AvaliacaoRapidaViewModel,
    onSalvarRegistro: (sexo: String, idadeMeses: Int, peso: Double, estatura: Double) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Avaliação Rápida (Disco OMS)", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = viewModel::limpar) {
                        Icon(Icons.Default.Refresh, contentDescription = "Limpar Campos", tint = Color.White)
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
            // Seletor de Sexo
            Text(
                text = "Sexo da Criança",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = state.sexo == "M",
                    onClick = { viewModel.onSexoChange("M") },
                    label = { Text("Menino (Masc)", fontWeight = FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryGreen,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = state.sexo == "F",
                    onClick = { viewModel.onSexoChange("F") },
                    label = { Text("Menina (Fem)", fontWeight = FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryGreen,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Idade em Meses
            OutlinedTextField(
                value = state.idadeMeses,
                onValueChange = viewModel::onIdadeChange,
                label = { Text("Idade (meses completos) *") },
                placeholder = { Text("Ex: 18 (0 a 59 meses)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.peso,
                    onValueChange = viewModel::onPesoChange,
                    label = { Text("Peso (kg) *") },
                    placeholder = { Text("Ex: 11.2") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.estatura,
                    onValueChange = viewModel::onEstaturaChange,
                    label = { Text("Estatura (cm) *") },
                    placeholder = { Text("Ex: 82.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

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

            Button(
                onClick = viewModel::calcular,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("Calcular Escore-Z", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card de Resultado
            state.resultado?.let { res ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resultado da Avaliação",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                        Text(
                            text = "Parâmetros OMS / SISVAN para ${res.idadeMeses} meses (${if (res.sexo == "M") "Menino" else "Menina"})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Estatura para Idade
                        Text(
                            text = "ESTATURA PARA A IDADE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        StatusBadge(
                            texto = res.classificacaoEstatura.descricao,
                            nivelRisco = res.classificacaoEstatura.nivelRisco
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Peso para Idade
                        Text(
                            text = "PESO PARA A IDADE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        StatusBadge(
                            texto = res.classificacaoPeso.descricao,
                            nivelRisco = res.classificacaoPeso.nivelRisco
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedButton(
                            onClick = {
                                onSalvarRegistro(
                                    res.sexo,
                                    res.idadeMeses,
                                    res.peso,
                                    res.estatura
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text("Salvar no Histórico da Criança", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
