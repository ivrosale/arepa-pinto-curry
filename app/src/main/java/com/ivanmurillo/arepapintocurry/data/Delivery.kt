package com.ivanmurillo.arepapintocurry.data

import android.location.Location
import kotlin.math.ceil

/** Ubicación del restaurante (caso hipotético en Barrio Escalante, San José). */
object Restaurant {
    const val NAME = "Arepa, Pinto & Curry"
    const val LAT = 9.9346
    const val LON = -84.0619
    const val MAX_KM = 8.0
}

data class Delivery(
    val lat: Double,
    val lon: Double,
    val address: String,
    val details: String,
    val distanceKm: Double,
    val fee: Int,
)

/** Distancia en línea recta desde el restaurante, en km. */
fun distanceFromRestaurantKm(lat: Double, lon: Double): Double {
    val result = FloatArray(1)
    Location.distanceBetween(Restaurant.LAT, Restaurant.LON, lat, lon, result)
    return result[0] / 1000.0
}

/** Envío: ₡1 000 hasta 2 km y ₡350 por cada km adicional, redondeado a ₡50. */
fun deliveryFee(km: Double): Int {
    if (km <= 2.0) return 1000
    val raw = 1000 + (km - 2.0) * 350
    return (ceil(raw / 50.0) * 50).toInt()
}
