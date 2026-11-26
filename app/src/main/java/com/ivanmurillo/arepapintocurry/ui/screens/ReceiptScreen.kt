package com.ivanmurillo.arepapintocurry.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivanmurillo.arepapintocurry.data.Restaurant
import com.ivanmurillo.arepapintocurry.data.colones
import com.ivanmurillo.arepapintocurry.ui.Order
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormat = DateTimeFormatter.ofPattern("d 'de' MMMM yyyy, h:mm a", Locale.forLanguageTag("es-CR"))

@Composable
fun ReceiptScreen(order: Order?, onNewOrder: () -> Unit) {
    BackHandler(onBack = onNewOrder)
    if (order == null) return
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(
                    Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Comprobante ${order.number}")
                                putExtra(Intent.EXTRA_TEXT, receiptText(order))
                            }
                            context.startActivity(Intent.createChooser(send, "Compartir comprobante"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Text("  Compartir comprobante")
                    }
                    Button(onClick = onNewOrder, modifier = Modifier.fillMaxWidth()) { Text("Hacer otro pedido") }
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
            Text("✅", fontSize = 64.sp)
            Text("¡Pago confirmado!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Tu pedido ya está en la cocina. Llega en ${order.etaMinutes.first}–${order.etaMinutes.last} minutos.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )

            // El comprobante va en fondo blanco, como un recibo impreso
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White, contentColor = Color(0xFF1C1B1F)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        Restaurant.NAME,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                    Text("COMPROBANTE DE PAGO", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge)
                    HorizontalDivider()
                    ReceiptRow("N.º de orden", order.number, mono = true)
                    ReceiptRow("Fecha", order.dateTime.format(dateFormat))
                    ReceiptRow("Método de pago", order.paymentMethod)
                    ReceiptRow("Código de autorización", order.authCode, mono = true)
                    ReceiptRow("Estado", "APROBADO ✔")
                    HorizontalDivider()
                    order.lines.forEach { ReceiptRow("${it.quantity} × ${it.dish.name}", colones(it.total)) }
                    HorizontalDivider()
                    ReceiptRow("Subtotal", colones(order.subtotal))
                    ReceiptRow(String.format(Locale.US, "Envío (%.1f km)", order.delivery.distanceKm), colones(order.delivery.fee))
                    ReceiptRow("TOTAL PAGADO", colones(order.total), bold = true)
                    HorizontalDivider()
                    Text("Entregar en", style = MaterialTheme.typography.labelLarge)
                    Text(order.delivery.address, style = MaterialTheme.typography.bodyMedium)
                    if (order.delivery.details.isNotBlank()) {
                        Text("Señas: ${order.delivery.details}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        String.format(Locale.US, "GPS: %.5f, %.5f", order.delivery.lat, order.delivery.lon),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    HorizontalDivider()
                    Text(
                        "¡Gracias por probar los sabores de Venezuela, Costa Rica e India!",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, bold: Boolean = false, mono: Boolean = false) {
    val weight = if (bold) FontWeight.Bold else FontWeight.Normal
    Row(Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f), fontWeight = weight, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            fontWeight = weight,
            fontFamily = if (mono) FontFamily.Monospace else null,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
        )
    }
}

/** Versión en texto para compartir por WhatsApp o correo. */
private fun receiptText(order: Order): String = buildString {
    appendLine("${Restaurant.NAME} — Comprobante de pago")
    appendLine("Orden: ${order.number}")
    appendLine("Fecha: ${order.dateTime.format(dateFormat)}")
    appendLine("Pago: ${order.paymentMethod} (autorización ${order.authCode})")
    appendLine()
    order.lines.forEach { appendLine("${it.quantity} × ${it.dish.name}: ${colones(it.total)}") }
    appendLine("Subtotal: ${colones(order.subtotal)}")
    appendLine("Envío: ${colones(order.delivery.fee)}")
    appendLine("TOTAL: ${colones(order.total)}")
    appendLine()
    appendLine("Entregar en: ${order.delivery.address}")
    if (order.delivery.details.isNotBlank()) appendLine("Señas: ${order.delivery.details}")
}
