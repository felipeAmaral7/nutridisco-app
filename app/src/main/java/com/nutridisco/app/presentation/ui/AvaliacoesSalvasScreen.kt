package com.nutridisco.app.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.nutridisco.app.presentation.avaliacoessalvas.AvaliacoesSalvasViewModel
import com.nutridisco.app.presentation.avaliacoessalvas.TipoOrdenacao
import com.nutridisco.app.presentation.components.StatusBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CorFundoBege = Color(0xFFF7F5EE)
private val CorVerdeBanner = Color(0xFF26732B)
private val CorVerdeBotao = Color(0xFF2D7A32)
private val CorTextoEscuro = Color(0xFF1F1F1F)
private val CorTextoMudo = Color(0xFF757575)
private val CorBordaDivisoria = Color(0xFFDCD8CF)
private val CorCirculoVazio = Color(0xFFE8EEF5)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AvaliacoesSalvasScreen(
    onIniciarNovaAvaliacaoClick: () -> Unit,
    onCriancaClick: (Long) -> Unit = {},
    viewModel: AvaliacoesSalvasViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val avaliacoes by viewModel.avaliacoes.collectAsState()
    val searchQuery by viewModel.termoBusca.collectAsState()
    val ordenacao by viewModel.ordenacao.collectAsState()
    val totalGeral by viewModel.totalGeral.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    val listState = rememberLazyListState()

    // Fração de encolhimento de 0f (totalmente expandido) a 1f (totalmente recolhido)
    val collapseFraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (listState.firstVisibleItemScrollOffset.toFloat() / 180f).coerceIn(0f, 1f)
            }
        }
    }

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
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = onIniciarNovaAvaliacaoClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CorVerdeBotao),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "Iniciar Nova Avaliação",
                            fontSize = 17.sp,
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
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Banner Superior Retrátil (encolhe conforme o scroll)
                item(key = "banner") {
                    BannerSuperiorRetratil(collapseFraction = collapseFraction)
                }

                // 2. Cabeçalho da Seção com Contador
                item(key = "contador") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 2.dp),
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
                }

                // 3. Barra de Pesquisa e Ordenação (Doca no topo ao rolar para acesso rápido)
                stickyHeader(key = "busca_ordenacao") {
                    BarraPesquisaEOrdenacao(
                        searchQuery = searchQuery,
                        ordenacao = ordenacao,
                        onTermoBuscaChange = viewModel::onTermoBuscaChange,
                        onLimparBusca = viewModel::limparTermoBusca,
                        onAlternarOrdenacao = viewModel::alternarOrdenacao
                    )
                }

                // 4. Conteúdo: Estado Vazio vs. Lista de Cards
                if (avaliacoes.isEmpty()) {
                    item(key = "vazio") {
                        EstadoVazio(searchQuery = searchQuery)
                    }
                } else {
                    items(avaliacoes, key = { it.avaliacaoId }) { item ->
                        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                            CardAvaliacao(
                                item = item,
                                onCriancaClick = onCriancaClick,
                                dateFormat = dateFormat
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerSuperiorRetratil(collapseFraction: Float) {
    val avatarBoxSize = (64 - (24 * collapseFraction)).dp
    val avatarImgSize = (56 - (22 * collapseFraction)).dp
    val titleSize = (22 - (5 * collapseFraction)).sp
    val verticalPadding = (18 - (12 * collapseFraction)).dp
    val subtitleAlpha = (1f - (collapseFraction * 2.0f)).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CorVerdeBanner),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = (8 - (6 * collapseFraction)).dp, bottom = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(avatarBoxSize)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_personagem_abertura),
                    contentDescription = "Avatar NutriDisco",
                    modifier = Modifier.size(avatarImgSize)
                )
            }

            Spacer(modifier = Modifier.height((6 - (4 * collapseFraction)).dp))

            Text(
                text = "NutriDisco",
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (subtitleAlpha > 0.05f) {
                Text(
                    text = "Avaliação Nutricional Infantil",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.9f * subtitleAlpha)
                )
            }
        }
    }
}

@Composable
private fun BarraPesquisaEOrdenacao(
    searchQuery: String,
    ordenacao: TipoOrdenacao,
    onTermoBuscaChange: (String) -> Unit,
    onLimparBusca: () -> Unit,
    onAlternarOrdenacao: () -> Unit
) {
    Surface(
        color = CorFundoBege,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onTermoBuscaChange,
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
                            IconButton(onClick = onLimparBusca) {
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
                    modifier = Modifier.weight(1f)
                )

                // Botão de alternância de ordenação (Mais recentes vs A-Z)
                Surface(
                    onClick = onAlternarOrdenacao,
                    shape = RoundedCornerShape(12.dp),
                    color = CorVerdeBotao.copy(alpha = 0.12f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = CorVerdeBotao
                    ),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (ordenacao == TipoOrdenacao.RECENTES) Icons.Default.AccessTime else Icons.Default.SortByAlpha,
                            contentDescription = if (ordenacao == TipoOrdenacao.RECENTES) "Alternar para ordem alfabética (A–Z)" else "Alternar para mais recentes",
                            tint = CorVerdeBotao,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Indicador de ordenação ativa (sempre verde)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (ordenacao == TipoOrdenacao.RECENTES) "Ordenando por: Mais recentes" else "Ordenando por: Nome da criança (A–Z)",
                    fontSize = 11.sp,
                    color = CorVerdeBotao,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EstadoVazio(searchQuery: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
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
}

@Composable
private fun CardAvaliacao(
    item: AvaliacaoDetalhada,
    onCriancaClick: (Long) -> Unit,
    dateFormat: SimpleDateFormat
) {
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
                    texto = item.classificacaoEstatura.descricao,
                    nivelRisco = item.classificacaoEstatura.nivelRisco,
                    modifier = Modifier.weight(1f)
                )

                StatusBadge(
                    texto = item.classificacaoPeso.descricao,
                    nivelRisco = item.classificacaoPeso.nivelRisco,
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
