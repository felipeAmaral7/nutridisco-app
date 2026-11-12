package com.nutridisco.app.presentation.exportacao

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nutridisco.app.domain.export.CampoExportacao
import com.nutridisco.app.presentation.theme.PrimaryGreen
import com.nutridisco.app.presentation.theme.StatusGreen
import com.nutridisco.app.presentation.theme.StatusRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportacaoScreen(
    viewModel: ExportacaoViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val avaliacoes by viewModel.totalAvaliacoes.collectAsState()
    val context = LocalContext.current

    // Caixa de diálogo para o usuário escolher quais informações deseja exportar
    if (state.mostrarDialogCampos) {
        Dialog(onDismissRequest = viewModel::fecharDialogCampos) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Campos do Relatório",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                        TextButton(
                            onClick = {
                                if (state.camposSelecionados.size == CampoExportacao.entries.size) {
                                    viewModel.desmarcarTodosCampos()
                                } else {
                                    viewModel.selecionarTodosCampos()
                                }
                            }
                        ) {
                            Text(
                                text = if (state.camposSelecionados.size == CampoExportacao.entries.size) "Desmarcar todos" else "Marcar todos",
                                fontSize = 12.sp,
                                color = PrimaryGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = "Selecione as colunas a incluir no ${state.formatoSelecionado.extensao.uppercase()}:",
                        fontSize = 12.sp,
                        color = Color(0xFF6B6B6B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                    ) {
                        items(CampoExportacao.entries) { campo ->
                            val isChecked = campo in state.camposSelecionados
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.alternarCampo(campo) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { viewModel.alternarCampo(campo) },
                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = campo.rotulo,
                                        fontSize = 13.sp,
                                        fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isChecked) Color(0xFF1F1F1F) else Color(0xFF757575)
                                    )
                                    Text(
                                        text = campo.descricao,
                                        fontSize = 11.sp,
                                        color = Color(0xFF9E9E9E)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = viewModel::fecharDialogCampos) {
                            Text("Cancelar", color = Color(0xFF757575))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.confirmarExportacao(context) },
                            enabled = state.camposSelecionados.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Exportar (${state.camposSelecionados.size})",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exportação de Dados", fontWeight = FontWeight.Bold) },
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Relatórios e Prestação de Contas",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Exporte o histórico de atendimentos antropométricos coletados em campo no DSEI Leste de Roraima.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Operação 100% offline: transfira imediatamente via Bluetooth, WhatsApp, pendrive ou e-mail.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Base Local de Atendimentos",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${avaliacoes.size} avaliações prontas para exportar",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryGreen.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${avaliacoes.size} reg.",
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Formato de Exportação:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            CardFormato(
                titulo = "Relatório em PDF (.pdf)",
                descricao = "Documento paginado e formatado. Ideal para impressão, arquivamento e visualização clínica.",
                icone = Icons.Default.PictureAsPdf,
                selecionado = state.formatoSelecionado == FormatoExportacao.PDF,
                onClick = { viewModel.selecionarFormato(FormatoExportacao.PDF) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            CardFormato(
                titulo = "Planilha Tabular CSV (.csv)",
                descricao = "Estrutura tabular com colunas separadas por ponto e vírgula. Compatível com Excel e SISVAN.",
                icone = Icons.Default.TableChart,
                selecionado = state.formatoSelecionado == FormatoExportacao.CSV,
                onClick = { viewModel.selecionarFormato(FormatoExportacao.CSV) }
            )

            state.mensagemSucesso?.let { msg ->
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = msg,
                    color = StatusGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            state.erro?.let { erro ->
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = erro,
                    color = StatusRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = viewModel::abrirDialogCampos,
                enabled = !state.exportando && avaliacoes.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (state.exportando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = "Gerar e Compartilhar ${state.formatoSelecionado.extensao.uppercase()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CardFormato(
    titulo: String,
    descricao: String,
    icone: ImageVector,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (selecionado) 2.dp else 1.dp,
            color = if (selecionado) PrimaryGreen else Color(0xFFE0DCD2)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selecionado) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = if (selecionado) PrimaryGreen else Color.Gray,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontSize = 15.sp,
                    fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Medium,
                    color = if (selecionado) PrimaryGreen else Color(0xFF1F1F1F)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = descricao,
                    fontSize = 12.sp,
                    color = Color(0xFF6B6B6B),
                    lineHeight = 16.sp
                )
            }

            RadioButton(
                selected = selecionado,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
            )
        }
    }
}
