package com.ivanmurillo.arepapintocurry.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivanmurillo.arepapintocurry.data.colones
import com.ivanmurillo.arepapintocurry.ui.CartLine
import com.ivanmurillo.arepapintocurry.ui.CartViewModel
import com.ivanmurillo.arepapintocurry.ui.components.DishImage
import com.ivanmurillo.arepapintocurry.ui.components.QuantityStepper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(cart: CartViewModel, onBack: () -> Unit, onContinue: () -> Unit) {
    var confirmClear by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tu carrito") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (cart.itemCount > 0) TextButton(onClick = { confirmClear = true }) { Text("Vaciar") }
                },
            )
        },
        bottomBar = {
            if (cart.itemCount > 0) {
                Surface(shadowElevation = 8.dp) {
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                    ) { Text("Continuar con la entrega") }
                }
            }
        },
    ) { padding ->
        if (cart.itemCount == 0) {
            EmptyCart(Modifier.padding(padding), onBack)
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(cart.lines, key = { it.dish.id }) { line ->
                    CartRow(
                        line,
                        onIncrease = { cart.add(line.dish.id) },
                        onDecrease = { cart.decrease(line.dish.id) },
                        onRemove = { cart.remove(line.dish.id) },
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                }
                item { Summary(cart) }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("¿Vaciar el carrito?") },
            text = { Text("Se quitarán todos los productos.") },
            confirmButton = { TextButton(onClick = { cart.clear(); confirmClear = false }) { Text("Vaciar") } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun CartRow(line: CartLine, onIncrease: () -> Unit, onDecrease: () -> Unit, onRemove: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        DishImage(line.dish, 56.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(line.dish.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "${colones(line.dish.price)} c/u",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            QuantityStepper(line.quantity, onDecrease = onDecrease, onIncrease = onIncrease)
        }
        Column(horizontalAlignment = Alignment.End) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "Quitar ${line.dish.name}")
            }
            Text(colones(line.total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Summary(cart: CartViewModel) {
    Card(
        Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryRow("Productos", cart.itemCount.toString())
            SummaryRow("Subtotal", colones(cart.subtotal))
            val delivery = cart.delivery
            SummaryRow("Envío", if (delivery != null) colones(delivery.fee) else "Se calcula con tu ubicación")
            HorizontalDivider()
            SummaryRow(if (delivery != null) "Total" else "Total parcial", colones(cart.total), bold = true)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false) {
    val weight = if (bold) FontWeight.Bold else FontWeight.Normal
    Row(Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f), fontWeight = weight)
        Text(value, fontWeight = weight)
    }
}

@Composable
private fun EmptyCart(modifier: Modifier, onBack: () -> Unit) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🛒", fontSize = 64.sp)
        Spacer(Modifier.height(12.dp))
        Text("Tu carrito está vacío", style = MaterialTheme.typography.titleLarge)
        Text(
            "Agrega una arepa, un gallo pinto o un curry para empezar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack) { Text("Ver el menú") }
    }
}
