package com.nutridisco.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutridisco.app.domain.model.NivelRisco
import com.nutridisco.app.presentation.theme.StatusGreen
import com.nutridisco.app.presentation.theme.StatusGreenContainer
import com.nutridisco.app.presentation.theme.StatusRed
import com.nutridisco.app.presentation.theme.StatusRedContainer
import com.nutridisco.app.presentation.theme.StatusYellow
import com.nutridisco.app.presentation.theme.StatusYellowContainer

@Composable
fun StatusBadge(
    texto: String,
    nivelRisco: NivelRisco,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (nivelRisco) {
        NivelRisco.VERDE -> Triple(StatusGreenContainer, StatusGreen, StatusGreen)
        NivelRisco.AMARELO -> Triple(StatusYellowContainer, StatusYellow, StatusYellow)
        NivelRisco.VERMELHO -> Triple(StatusRedContainer, StatusRed, StatusRed)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = texto,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
