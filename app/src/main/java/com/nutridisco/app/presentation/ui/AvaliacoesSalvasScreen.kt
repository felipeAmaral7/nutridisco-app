package com.nutridisco.app.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nutridisco.app.R
import com.nutridisco.app.data.local.model.AvaliacaoDetalhada
import com.nutridisco.app.domain.model.ClassificacaoEstatura
import com.nutridisco.app.domain.model.ClassificacaoPeso
import com.nutridisco.app.domain.model.NivelRisco
import com.nutridisco.app.presentation.avaliacoessalvas.AvaliacoesSalvasViewModel
import com.nutridisco.app.presentation.components.StatusBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Paleta extraída do protótipo
private val CorFundoBege = Color(0xFFF7F5EE)
private val CorVerdeBanner = Color(0xFF26732B)
private val CorVerdeBotao = Color(0xFF2D7A32)
private val CorTextoEscuro = Color(0xFF2C2C2C)
private val CorTextoMudo = Color(0xFF757575)
private val CorBordaDivisoria = Color(0xFFDCD8CF)
private val CorCirculoVazio = Color(0xFFE8EEF5)

@Composable
fun AvaliacoesSalvasScreen(
    onIniciarNovaAvaliacaoClick: () -> Unit,
    onCriancaClick: (Long) -> Unit = {},
    viewModel: AvaliacoesSalvasViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val avaliacoes by viewModel.avaliacoes.collectAsState()
    val searchQuery by viewModel.termoBusca.collectAsState()
    val totalGeral by viewModel.totalGeral.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CorFundoBege)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = CorFundoBege,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CorFundoBege)
            ) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = CorBordaDivisoria
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = onIniciarNovaAvaliacaoClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CorVerdeBotao),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "Iniciar Nova Avaliação",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Banner Superior Institucional (#26732B)
            Card(
                shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp),
                colors = CardDefaults.cardColors(containerColor = CorVerdeBanner),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_personagem_abertura),
                            contentDescription = "Avatar NutriDisco",
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "NutriDisco",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "Avaliação Nutricional Infantil",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // 2. Cabeçalho da Seção com Contador
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Avaliações Salvas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CorTextoEscuro
                )
                Text(
                    text = if (searchQuery.isBlank()) "$totalGeral registros" else "${avaliacoes.size} de $totalGeral",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = CorTextoMudo
                )
            }

            // 3. Barra de Pesquisa Arredondada
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onTermoBuscaChange,
                placeholder = {
                    Text(
                        text = "Buscar por nome ou comunidade...",
                        color = CorTextoMudo,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = CorTextoMudo
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = viewModel::limparTermoBusca) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar busca",
                                tint = CorTextoMudo
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = CorVerdeBotao,
                    unfocusedBorderColor = CorBordaDivisoria,
                    focusedTextColor = CorTextoEscuro,
                    unfocusedTextColor = CorTextoEscuro
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            )

            // 4. Conteúdo: Estado Vazio vs. Lista
            if (avaliacoes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(CorCirculoVazio),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (searchQuery.isEmpty()) {
                                "Nenhuma avaliação registrada"
                            } else {
                                "Nenhum resultado para \"$searchQuery\""
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CorTextoEscuro,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (searchQuery.isEmpty()) {
                                "Inicie uma nova avaliação para começar"
                            } else {
                                "Verifique o nome digitado ou limpe a busca"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = CorTextoMudo,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(avaliacoes, key = { it.avaliacaoId }) { item ->
                        val riscoEstatura = ClassificacaoEstatura.fromDescricao(item.classificacaoEstatura)?.nivelRisco ?: NivelRisco.VERDE
                        val riscoPeso = ClassificacaoPeso.fromDescricao(item.classificacaoPeso)?.nivelRisco ?: NivelRisco.VERDE

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCriancaClick(item.criancaId) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.criancaNome,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CorTextoEscuro
                                    )
                                    Text(
                                        text = "${item.idadeMesesMomento} meses",
                                        fontSize = 12.sp,
                                        color = CorTextoMudo
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "${item.comunidadeNome} • ${item.poloBaseNome}",
                                    fontSize = 13.sp,
                                    color = CorTextoMudo
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Peso: ${item.peso} kg",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CorTextoEscuro
                                    )
                                    Text(
                                        text = "Estatura: ${item.estatura} cm",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CorTextoEscuro
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatusBadge(
                                        texto = item.classificacaoEstatura,
                                        nivelRisco = riscoEstatura,
                                        modifier = Modifier.weight(1f)
                                    )

                                    StatusBadge(
                                        texto = item.classificacaoPeso,
                                        nivelRisco = riscoPeso,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "${dateFormat.format(Date(item.dataAtendimento))} • Avaliador: ${item.nomeProfissional}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9E9E9E)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
