package com.ivanmurillo.arepapintocurry.ui

import com.ivanmurillo.arepapintocurry.data.Delivery
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/** Pedido ya pagado; es lo que se muestra en el comprobante. */
data class Order(
    val number: String,
    val dateTime: LocalDateTime,
    val lines: List<CartLine>,
    val subtotal: Int,
    val delivery: Delivery,
    val total: Int,
    val paymentMethod: String,
    val authCode: String,
) {
    /** Tiempo estimado: 25 min de cocina más 5 min por km de distancia. */
    val etaMinutes: IntRange
        get() {
            val base = 25 + (delivery.distanceKm * 5).toInt()
            return base..(base + 10)
        }

    companion object {
        fun create(lines: List<CartLine>, subtotal: Int, delivery: Delivery, paymentMethod: String): Order {
            val now = LocalDateTime.now()
            val number = "APC-" + now.format(DateTimeFormatter.ofPattern("yyMMdd")) + "-" + Random.nextInt(1000, 10000)
            return Order(
                number = number,
                dateTime = now,
                lines = lines,
                subtotal = subtotal,
                delivery = delivery,
                total = subtotal + delivery.fee,
                paymentMethod = paymentMethod,
                authCode = Random.nextInt(100000, 1000000).toString(),
            )
        }
    }
}
