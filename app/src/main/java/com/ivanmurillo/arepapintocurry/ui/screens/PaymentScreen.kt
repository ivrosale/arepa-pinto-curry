package com.ivanmurillo.arepapintocurry.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ivanmurillo.arepapintocurry.data.CardNumberTransformation
import com.ivanmurillo.arepapintocurry.data.ExpiryTransformation
import com.ivanmurillo.arepapintocurry.data.PhoneTransformation
import com.ivanmurillo.arepapintocurry.data.TEST_CARD
import com.ivanmurillo.arepapintocurry.data.cardBrand
import com.ivanmurillo.arepapintocurry.data.cardNumberError
import com.ivanmurillo.arepapintocurry.data.colones
import com.ivanmurillo.arepapintocurry.data.cvvError
import com.ivanmurillo.arepapintocurry.data.expiryError
import com.ivanmurillo.arepapintocurry.data.formatPhone
import com.ivanmurillo.arepapintocurry.data.isDeclinedTestCard
import com.ivanmurillo.arepapintocurry.data.nameError
import com.ivanmurillo.arepapintocurry.data.phoneError
import com.ivanmurillo.arepapintocurry.ui.CartViewModel
import kotlinx.coroutines.delay
import java.util.Locale

private enum class Method { CARD, SINPE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(cart: CartViewModel, onBack: () -> Unit, onPaid: () -> Unit) {
    val delivery = cart.delivery
    // Al pagar se vacía el carrito y la app ya va hacia el comprobante: no hay nada que dibujar
    if (delivery == null || cart.itemCount == 0) return
    val focusManager = LocalFocusManager.current

    var method by rememberSaveable { mutableStateOf(Method.CARD) }
    var cardNumber by rememberSaveable { mutableStateOf("") }
    var cardName by rememberSaveable { mutableStateOf("") }
    var expiry by rememberSaveable { mutableStateOf("") }
    var cvv by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    // Los errores se muestran solo después de intentar pagar, para no regañar mientras se escribe
    var showErrors by rememberSaveable { mutableStateOf(false) }
    var processing by remember { mutableStateOf(false) }
    var declined by remember { mutableStateOf(false) }

    val cardErrors = listOf(cardNumberError(cardNumber), nameError(cardName), expiryError(expiry), cvvError(cvv))
    val formValid = when (method) {
        Method.CARD -> cardErrors.all { it == null }
        Method.SINPE -> phoneError(phone) == null
    }
    val methodLabel = when (method) {
        Method.CARD -> "${cardBrand(cardNumber).label} terminada en ${cardNumber.takeLast(4)}"
        Method.SINPE -> "SINPE Móvil ${formatPhone(phone)}"
    }

    // Simula el tiempo de respuesta del banco
    LaunchedEffect(processing) {
        if (!processing) return@LaunchedEffect
        delay(2200)
        processing = false
        if (method == Method.CARD && isDeclinedTestCard(cardNumber)) {
            declined = true
        } else if (cart.placeOrder(methodLabel) != null) {
            onPaid()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pago") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        showErrors = true
                        if (formValid) processing = true
                    },
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(16.dp),
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pagar ${colones(cart.total)}")
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Resumen del pedido
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Resumen del pedido", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    cart.lines.forEach { line ->
                        AmountRow("${line.quantity} × ${line.dish.name}", colones(line.total))
                    }
                    HorizontalDivider()
                    AmountRow("Subtotal", colones(cart.subtotal))
                    AmountRow(String.format(Locale.US, "Envío (%.1f km)", delivery.distanceKm), colones(delivery.fee))
                    AmountRow("Total a pagar", colones(cart.total), bold = true)
                    Text(
                        "📍 ${delivery.address}" + if (delivery.details.isNotBlank()) " · ${delivery.details}" else "",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Text("Método de pago", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            MethodOption("💳", "Tarjeta de crédito o débito", "Visa o Mastercard", method == Method.CARD) { method = Method.CARD }
            MethodOption("📱", "SINPE Móvil", "Te llega la solicitud de pago a tu celular", method == Method.SINPE) { method = Method.SINPE }

            when (method) {
                Method.CARD -> {
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it.filter(Char::isDigit).take(16) },
                        label = { Text("Número de tarjeta") },
                        supportingText = { Text(if (showErrors) cardNumberError(cardNumber) ?: cardBrand(cardNumber).label else "16 dígitos") },
                        isError = showErrors && cardNumberError(cardNumber) != null,
                        visualTransformation = CardNumberTransformation,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = cardName,
                        onValueChange = { cardName = it.take(40) },
                        label = { Text("Nombre en la tarjeta") },
                        supportingText = { if (showErrors) nameError(cardName)?.let { Text(it) } },
                        isError = showErrors && nameError(cardName) != null,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = expiry,
                            onValueChange = { expiry = it.filter(Char::isDigit).take(4) },
                            label = { Text("Vence (MM/AA)") },
                            supportingText = { if (showErrors) expiryError(expiry)?.let { Text(it) } },
                            isError = showErrors && expiryError(expiry) != null,
                            visualTransformation = ExpiryTransformation,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { cvv = it.filter(Char::isDigit).take(3) },
                            label = { Text("CVV") },
                            supportingText = { if (showErrors) cvvError(cvv)?.let { Text(it) } },
                            isError = showErrors && cvvError(cvv) != null,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    TextButton(onClick = {
                        cardNumber = TEST_CARD
                        if (cardName.isBlank()) cardName = "CLIENTE DE PRUEBA"
                        expiry = "1230"
                        cvv = "123"
                    }) { Text("Usar tarjeta de prueba (4111 1111 1111 1111)") }
                }
                Method.SINPE -> {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it.filter(Char::isDigit).take(8) },
                        label = { Text("Tu número de celular") },
                        prefix = { Text("+506 ") },
                        supportingText = { Text(if (showErrors) phoneError(phone) ?: "Número válido" else "Ej.: 8888-8888") },
                        isError = showErrors && phoneError(phone) != null,
                        visualTransformation = PhoneTransformation,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Text(
                "🔒 Pago simulado para el proyecto del curso: no se hace ningún cobro real ni se guardan los datos de la tarjeta.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (processing) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = { Text("Procesando pago…") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(32.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(if (method == Method.CARD) "Conectando con el banco" else "Esperando la confirmación de SINPE Móvil")
                }
            },
        )
    }
    if (declined) {
        AlertDialog(
            onDismissRequest = { declined = false },
            title = { Text("Pago rechazado") },
            text = { Text("El banco rechazó la tarjeta terminada en ${cardNumber.takeLast(4)}. Revisa los datos o usa otro método de pago.") },
            confirmButton = { TextButton(onClick = { declined = false }) { Text("Entendido") } },
        )
    }
}

@Composable
private fun MethodOption(emoji: String, title: String, subtitle: String, selected: Boolean, onSelect: () -> Unit) {
    OutlinedCard(
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().selectable(selected = selected, onClick = onSelect, role = Role.RadioButton),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(8.dp))
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AmountRow(label: String, value: String, bold: Boolean = false) {
    val weight = if (bold) FontWeight.Bold else FontWeight.Normal
    Row(Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f), fontWeight = weight)
        Text(value, fontWeight = weight)
    }
}
