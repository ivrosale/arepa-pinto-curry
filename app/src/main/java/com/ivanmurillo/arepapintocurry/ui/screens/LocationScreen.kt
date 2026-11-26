package com.ivanmurillo.arepapintocurry.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ivanmurillo.arepapintocurry.data.Delivery
import com.ivanmurillo.arepapintocurry.data.Restaurant
import com.ivanmurillo.arepapintocurry.data.colones
import com.ivanmurillo.arepapintocurry.data.deliveryFee
import com.ivanmurillo.arepapintocurry.data.distanceFromRestaurantKm
import com.ivanmurillo.arepapintocurry.data.requestCurrentLocation
import com.ivanmurillo.arepapintocurry.data.reverseGeocode
import com.ivanmurillo.arepapintocurry.ui.CartViewModel
import com.ivanmurillo.arepapintocurry.ui.components.OsmMap
import org.osmdroid.util.GeoPoint
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(cart: CartViewModel, onBack: () -> Unit, onContinue: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val restaurant = remember { GeoPoint(Restaurant.LAT, Restaurant.LON) }

    // Punto de entrega elegido (arranca en la entrega guardada o en el restaurante)
    var lat by rememberSaveable { mutableDoubleStateOf(cart.delivery?.lat ?: Restaurant.LAT) }
    var lon by rememberSaveable { mutableDoubleStateOf(cart.delivery?.lon ?: Restaurant.LON) }
    var details by rememberSaveable { mutableStateOf(cart.delivery?.details ?: "") }
    var hasPoint by rememberSaveable { mutableStateOf(cart.delivery != null) }
    var centerKey by remember { mutableIntStateOf(0) }
    var address by remember { mutableStateOf<String?>(null) }
    var searching by remember { mutableStateOf(false) }
    var gpsMessage by remember { mutableStateOf<String?>(null) }

    val point = GeoPoint(lat, lon)
    val km = distanceFromRestaurantKm(lat, lon)
    val inRange = km <= Restaurant.MAX_KM
    val fee = deliveryFee(km)

    fun useGps() {
        gpsMessage = "Buscando tu ubicación…"
        requestCurrentLocation(context) { p ->
            if (p == null) {
                gpsMessage = "No se pudo leer el GPS. Toca el mapa para marcar tu casa."
            } else {
                lat = p.latitude; lon = p.longitude; hasPoint = true
                gpsMessage = null
                centerKey++
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.any { it }) useGps()
        else gpsMessage = "Sin permiso de ubicación. Toca el mapa para marcar tu casa."
    }

    fun askGps() {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (fine == PackageManager.PERMISSION_GRANTED) useGps()
        else permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // La primera vez se pide el GPS de una vez
    LaunchedEffect(Unit) { if (!hasPoint) askGps() }

    // Cada vez que cambia el punto se busca la dirección escrita
    LaunchedEffect(lat, lon, hasPoint) {
        if (!hasPoint) return@LaunchedEffect
        searching = true
        address = reverseGeocode(context, point)
        searching = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ubicación de entrega") },
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
                        cart.delivery = Delivery(
                            lat = lat,
                            lon = lon,
                            address = address ?: coordinates(lat, lon),
                            details = details.trim(),
                            distanceKm = km,
                            fee = fee,
                        )
                        onContinue()
                    },
                    enabled = hasPoint && inRange,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(16.dp),
                ) {
                    Text(
                        when {
                            !hasPoint -> "Elige un punto de entrega"
                            !inRange -> "Fuera de la zona de entrega"
                            else -> "Continuar al pago · ${colones(cart.subtotal + fee)}"
                        }
                    )
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Box(Modifier.fillMaxWidth().weight(1f).clipToBounds()) {
                OsmMap(
                    selected = point,
                    restaurant = restaurant,
                    centerOn = if (hasPoint) point else restaurant,
                    centerKey = centerKey,
                    onTap = { p -> lat = p.latitude; lon = p.longitude; hasPoint = true },
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    modifier = Modifier.align(Alignment.TopCenter).padding(8.dp),
                ) {
                    Text(
                        gpsMessage ?: "Toca el mapa para ajustar el punto exacto",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
                ExtendedFloatingActionButton(
                    onClick = { askGps() },
                    icon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                    text = { Text("Mi ubicación") },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                )
            }
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("📍 Entregar en", style = MaterialTheme.typography.labelLarge)
                        Text(
                            when {
                                !hasPoint -> "Todavía no has elegido un punto"
                                searching -> "Buscando la dirección…"
                                else -> address ?: coordinates(lat, lon)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (hasPoint) {
                            Text(
                                "GPS: ${coordinates(lat, lon)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            InfoRow("Distancia del restaurante", String.format(Locale.US, "%.1f km", km))
                            if (inRange) InfoRow("Costo de envío", colones(fee))
                            else Text(
                                "Solo entregamos hasta ${Restaurant.MAX_KM.toInt()} km del restaurante.",
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Señas para el repartidor") },
                    placeholder = { Text("Ej.: casa verde de portón negro, 100 m sur de la iglesia") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

private fun coordinates(lat: Double, lon: Double) = String.format(Locale.US, "%.5f, %.5f", lat, lon)
