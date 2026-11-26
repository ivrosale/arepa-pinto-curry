package com.ivanmurillo.arepapintocurry.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivanmurillo.arepapintocurry.data.Dish

/** Cuadro de color del país con el emoji del platillo (sustituye a la foto). */
@Composable
fun DishImage(dish: Dish, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 5))
            .background(dish.country.color.copy(alpha = 0.22f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(dish.emoji, fontSize = (size.value * 0.5f).sp)
    }
}

/** Etiquetas de picante y vegetariano. */
@Composable
fun DishTags(dish: Dish, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (dish.spicy > 0) Tag("🌶".repeat(dish.spicy) + " Picante")
        if (dish.vegetarian) Tag("🌱 Vegetariano")
    }
}

@Composable
private fun Tag(text: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(text, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}

/** Selector de cantidad: −  2  + */
@Composable
fun QuantityStepper(quantity: Int, onDecrease: () -> Unit, onIncrease: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(onClick = onDecrease, modifier = Modifier.size(36.dp)) {
            Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            quantity.toString(),
            modifier = Modifier.widthIn(min = 32.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
        FilledTonalIconButton(onClick = onIncrease, modifier = Modifier.size(36.dp)) {
            Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
