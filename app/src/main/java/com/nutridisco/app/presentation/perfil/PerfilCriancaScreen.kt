package com.nutridisco.app.presentation.perfil

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutridisco.app.data.local.entity.AvaliacaoEntity
import com.nutridisco.app.domain.util.IdadeUtils
import com.nutridisco.app.presentation.components.StatusBadge
import com.nutridisco.app.presentation.components.TopBarPilula
import com.nutridisco.app.presentation.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CorFundoBege = Color(0xFFF7F5EE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilCriancaScreen(
    viewModel: PerfilCriancaViewModel,
    onVoltar: () -> Unit,
    onNovaAvaliacaoClick: (Long) -> Unit,
    onEditarClick: (Long) -> Unit,
    onEditarAvaliacaoClick: (Long) -> Unit
) {
    val crianca by viewModel.crianca.collectAsState()
    val avaliacoes by viewModel.avaliacoes.collectAsState()

    var avaliacaoParaExcluir by remember { mutableStateOf<AvaliacaoEntity?>(null) }
    var mostrarDialogExcluirCrianca by remember { mutableStateOf(false) }

    // Diálogo de confirmação para excluir o cadastro da criança
    if (mostrarDialogExcluirCrianca && crianca != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogExcluirCrianca = false },
            title = { Text("Excluir Cadastro", fontWeight = FontWeight.Bold) },
            text = {
                Text("Tem certeza de que deseja apagar o cadastro de \"${crianca!!.nome}\"? Todas as avaliações registradas desta criança também serão excluídas permanentemente.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogExcluirCrianca = false
                        viewModel.excluirCrianca(onSucesso = onVoltar)
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogExcluirCrianca = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmação para excluir uma avaliação individual
    if (avaliacaoParaExcluir != null) {
        val av = avaliacaoParaExcluir!!
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val isUnicaAvaliacao = avaliacoes.size <= 1

        AlertDialog(
            onDismissRequest = { avaliacaoParaExcluir = null },
            title = {
                Text(
                    text = if (isUnicaAvaliacao) "Excluir Única Avaliação" else "Excluir Avaliação",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isUnicaAvaliacao) {
                        "Esta é a única avaliação cadastrada para esta criança. Excluir este atendimento também removerá o cadastro de \"${crianca?.nome ?: "esta criança"}\". Deseja continuar?"
                    } else {
                        "Tem certeza de que deseja apagar a avaliação realizada em ${dateFormat.format(Date(av.dataAtendimento))}? Esta ação não pode ser desfeita."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = av
                        avaliacaoParaExcluir = null
                        if (isUnicaAvaliacao) {
                            viewModel.excluirCrianca(onSucesso = onVoltar)
                        } else {
                            viewModel.excluirAvaliacao(toDelete)
                        }
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { avaliacaoParaExcluir = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CorFundoBege)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        TopBarPilula(
            titulo = crianca?.nome ?: "Perfil da Criança",
            onVoltar = onVoltar,
            acoes = {
                if (crianca != null) {
                    IconButton(onClick = { onEditarClick(crianca!!.id) }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar Cadastro",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { mostrarDialogExcluirCrianca = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir Criança",
                            tint = Color.White
                        )
                    }
                }
            }
        )

        if (crianca == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Carregando perfil...")
            }
        } else {
            val c = crianca!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Header com dados cadastrais
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ChildCare, contentDescription = null, tint = PrimaryGreen)
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(
                                text = c.nome,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Idade Atual: ${IdadeUtils.formatarIdadeExtenso(c.dataNascimento)} (${if (c.sexo == "M") "Menino" else "Menina"})",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Território: ${c.comunidadeNome} • ${c.poloBaseNome}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Responsável: ${c.nomeResponsavel}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        c.numeroCns?.let { cns ->
                            Text(
                                text = "CNS: $cns",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { onNovaAvaliacaoClick(c.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text("Nova Avaliação Nutricional", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Linha do Tempo de Avaliações (${avaliacoes.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (avaliacoes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma avaliação registrada ainda para esta criança.\nToque em 'Nova Avaliação Nutricional' acima.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(avaliacoes, key = { it.id }) { avaliacao ->
                            AvaliacaoTimelineCard(
                                avaliacao = avaliacao,
                                onEditar = { onEditarAvaliacaoClick(avaliacao.id) },
                                onExcluir = { avaliacaoParaExcluir = avaliacao }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvaliacaoTimelineCard(
    avaliacao: AvaliacaoEntity,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(avaliacao.dataAtendimento)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                Text(
                    text = "${avaliacao.idadeMesesMomento} meses",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Peso: ${avaliacao.peso} kg",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Estatura: ${avaliacao.estatura} cm",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(
                    texto = avaliacao.classificacaoEstatura.descricao,
                    nivelRisco = avaliacao.classificacaoEstatura.nivelRisco,
                    modifier = Modifier.weight(1f)
                )

                StatusBadge(
                    texto = avaliacao.classificacaoPeso.descricao,
                    nivelRisco = avaliacao.classificacaoPeso.nivelRisco,
                    modifier = Modifier.weight(1f)
                )
            }

            if (!avaliacao.observacoes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Obs: ${avaliacao.observacoes}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Avaliador: ${avaliacao.nomeProfissional} (${avaliacao.registroCoren})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditar) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar Avaliação",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onExcluir) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir Avaliação",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
