package com.nutridisco.app.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CorFundoBege = Color(0xFFF7F5EE)
private val CorVerdeBarra = Color(0xFF26732B)
private val CorVerdeAcao = Color(0xFF2D7A32)
private val CorTextoEscuro = Color(0xFF1F1F1F)
private val CorTextoCard = Color(0xFF2C2C2C)
private val CorPontoAtivo = Color(0xFF1F1F1F)
private val CorPontoInativo = Color(0xFFBDBDBD)

enum class SexoOpcao(val valorDb: String, val label: String, val emoji: String) {
    FEMININO("F", "Menina", "👧"),
    MASCULINO("M", "Menino", "👦")
}

@Composable
fun SelecaoSexoScreen(
    onVoltarClick: () -> Unit,
    onProximoClick: (SexoOpcao) -> Unit,
    modifier: Modifier = Modifier
) {
    var sexoSelecionado by remember { mutableStateOf<SexoOpcao?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CorFundoBege)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = CorFundoBege,
        topBar = {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CorVerdeBarra),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onVoltarClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Nova Avaliação",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicador de Passos (Etapa 1 de 2)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(CorPontoAtivo)
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(CorPontoInativo)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { sexoSelecionado?.let { onProximoClick(it) } },
                    enabled = sexoSelecionado != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CorVerdeAcao,
                        disabledContainerColor = CorVerdeAcao.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Próximo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sexo da Criança",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = CorTextoEscuro,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Cards de Seleção com Emojis Nativos (136.dp, 18.dp radius, borda verde de 2.dp quando selecionado)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CardOpcaoSexo(
                    label = SexoOpcao.FEMININO.label,
                    emoji = SexoOpcao.FEMININO.emoji,
                    selecionado = sexoSelecionado == SexoOpcao.FEMININO,
                    onClick = { sexoSelecionado = SexoOpcao.FEMININO }
                )

                Spacer(modifier = Modifier.width(20.dp))

                CardOpcaoSexo(
                    label = SexoOpcao.MASCULINO.label,
                    emoji = SexoOpcao.MASCULINO.emoji,
                    selecionado = sexoSelecionado == SexoOpcao.MASCULINO,
                    onClick = { sexoSelecionado = SexoOpcao.MASCULINO }
                )
            }
        }
    }
}

@Composable
private fun CardOpcaoSexo(
    label: String,
    emoji: String,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selecionado) 4.dp else 2.dp
        ),
        border = if (selecionado) BorderStroke(2.dp, CorVerdeAcao) else null,
        modifier = modifier
            .size(136.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 46.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Medium,
                color = if (selecionado) CorVerdeAcao else CorTextoCard
            )
        }
    }
}
