package com.ivanmurillo.arepapintocurry.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ivanmurillo.arepapintocurry.data.Delivery
import com.ivanmurillo.arepapintocurry.data.Dish
import com.ivanmurillo.arepapintocurry.data.MenuData

data class CartLine(val dish: Dish, val quantity: Int) {
    val total: Int get() = dish.price * quantity
}

/** Carrito compartido por todas las pantallas. Guarda id del platillo -> cantidad y la entrega elegida. */
class CartViewModel : ViewModel() {
    private val quantities = mutableStateMapOf<Int, Int>()

    /** Ubicación de entrega; null hasta que el cliente la confirma. */
    var delivery by mutableStateOf<Delivery?>(null)

    val lines: List<CartLine>
        get() = quantities.mapNotNull { (id, qty) -> MenuData.byId(id)?.let { CartLine(it, qty) } }
            .sortedBy { it.dish.id }

    val itemCount: Int get() = quantities.values.sum()
    val subtotal: Int get() = lines.sumOf { it.total }
    val total: Int get() = subtotal + (delivery?.fee ?: 0)

    fun quantityOf(dishId: Int): Int = quantities[dishId] ?: 0

    fun add(dishId: Int, amount: Int = 1) {
        quantities[dishId] = quantityOf(dishId) + amount
    }

    fun decrease(dishId: Int) {
        val qty = quantityOf(dishId)
        if (qty <= 1) quantities.remove(dishId) else quantities[dishId] = qty - 1
    }

    fun remove(dishId: Int) {
        quantities.remove(dishId)
    }

    fun clear() = quantities.clear()

    /** Último pedido pagado, para el comprobante. */
    var lastOrder by mutableStateOf<Order?>(null)
        private set

    /** Registra el pedido pagado y deja el carrito listo para uno nuevo. */
    fun placeOrder(paymentMethod: String): Order? {
        val delivery = delivery ?: return null
        val order = Order.create(lines, subtotal, delivery, paymentMethod)
        lastOrder = order
        quantities.clear()
        this.delivery = null
        return order
    }
}
