package com.nutridisco.app.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutridisco.app.R

// Paleta extraída do ecrã do Figma
private val CorFundoBege = Color(0xFFF7F5EE)
private val CorVerdeBotao = Color(0xFF2D7A32)
private val CorTextoPrincipal = Color(0xFF2B2B2B)
private val CorTextoSecundario = Color(0xFF555555)
private val CorInstitucional = Color(0xFF1E6B24)

@Composable
fun AberturaScreen(
    onIniciarAvaliacaoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CorFundoBege)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // Conteúdo Central
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ilustração Circular da Personagem
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_personagem_abertura),
                    contentDescription = "Personagem NutriDisco",
                    modifier = Modifier.size(136.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Título com Ícone de Destaque
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "NutriDisco",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = CorTextoPrincipal
                )
                Box(
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(CorVerdeBotao),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtítulo
            Text(
                text = "Avaliação Nutricional para\nComunidades Tradicionais",
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                color = CorTextoSecundario,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Botão Principal de Ação
            Button(
                onClick = onIniciarAvaliacaoClick,
                colors = ButtonDefaults.buttonColors(containerColor = CorVerdeBotao),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(56.dp)
            ) {
                Text(
                    text = "Iniciar Avaliação",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        // Rodapé Institucional
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "UFAM",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CorInstitucional
                )
                Text(
                    text = "Universidade Federal do Amazonas",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = CorInstitucional
                )
            }
        }
    }
}
