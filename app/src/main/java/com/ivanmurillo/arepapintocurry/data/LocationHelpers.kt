package com.ivanmurillo.arepapintocurry.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.util.Locale
import kotlin.coroutines.resume

/** Pide la ubicación actual del GPS. Llamar solo con el permiso ya concedido. */
@SuppressLint("MissingPermission")
fun requestCurrentLocation(context: Context, onResult: (GeoPoint?) -> Unit) {
    LocationServices.getFusedLocationProviderClient(context)
        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
        .addOnSuccessListener { loc -> onResult(loc?.let { GeoPoint(it.latitude, it.longitude) }) }
        .addOnFailureListener { onResult(null) }
}

/** Convierte coordenadas en una dirección escrita. Devuelve null si no hay servicio. */
suspend fun reverseGeocode(context: Context, point: GeoPoint): String? = withContext(Dispatchers.IO) {
    if (!Geocoder.isPresent()) return@withContext null
    val geocoder = Geocoder(context, Locale.forLanguageTag("es-CR"))
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(point.latitude, point.longitude, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (cont.isActive) cont.resume(addresses.firstOrNull()?.toText())
                    }
                    override fun onError(errorMessage: String?) {
                        if (cont.isActive) cont.resume(null)
                    }
                })
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(point.latitude, point.longitude, 1)?.firstOrNull()?.toText()
        }
    } catch (e: Exception) {
        null
    }
}

/** Arma la dirección escrita; quita los "plus codes" tipo WWP6+7FC y el nombre del país. */
private fun Address.toText(): String =
    (0..maxAddressLineIndex)
        .flatMap { getAddressLine(it).split(",") }
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.contains('+') && it != countryName }
        .distinct()
        .joinToString(", ")
