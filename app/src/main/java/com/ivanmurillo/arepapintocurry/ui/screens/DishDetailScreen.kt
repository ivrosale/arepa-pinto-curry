package com.ivanmurillo.arepapintocurry.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivanmurillo.arepapintocurry.data.MenuData
import com.ivanmurillo.arepapintocurry.data.colones
import com.ivanmurillo.arepapintocurry.ui.CartViewModel
import com.ivanmurillo.arepapintocurry.ui.components.DishImage
import com.ivanmurillo.arepapintocurry.ui.components.DishTags
import com.ivanmurillo.arepapintocurry.ui.components.QuantityStepper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDetailScreen(dishId: Int, cart: CartViewModel, onBack: () -> Unit) {
    val dish = MenuData.byId(dishId)
    if (dish == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    var quantity by rememberSaveable { mutableIntStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dish.country.label) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    QuantityStepper(
                        quantity,
                        onDecrease = { if (quantity > 1) quantity-- },
                        onIncrease = { quantity++ },
                    )
                    Button(
                        onClick = { cart.add(dish.id, quantity); onBack() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Agregar · ${colones(dish.price * quantity)}")
                    }
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DishImage(dish, 180.dp)
            Text(dish.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${dish.country.flag}  ${dish.country.label}", style = MaterialTheme.typography.titleSmall)
            DishTags(dish)
            Text(dish.description, style = MaterialTheme.typography.bodyLarge)
            Text(
                colones(dish.price),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            val inCart = cart.quantityOf(dish.id)
            if (inCart > 0) {
                Text(
                    "Ya tienes $inCart en el carrito",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
