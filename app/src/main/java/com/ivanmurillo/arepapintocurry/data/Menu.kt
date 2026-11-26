package com.ivanmurillo.arepapintocurry.data

import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.util.Locale

/** País de origen de cada platillo. COMBO agrupa los platos que mezclan los tres. */
enum class Country(val label: String, val flag: String, val color: Color) {
    VENEZUELA("Venezuela", "🇻🇪", Color(0xFFFFCC00)),
    COSTA_RICA("Costa Rica", "🇨🇷", Color(0xFF2E5AAC)),
    INDIA("India", "🇮🇳", Color(0xFFFF9933)),
    COMBO("Los tres países", "🇻🇪🇨🇷🇮🇳", Color(0xFFC62828)),
}

data class Dish(
    val id: Int,
    val name: String,
    val description: String,
    val price: Int, // en colones
    val country: Country,
    val emoji: String,
    val spicy: Int = 0, // 0 = no pica, 3 = muy picante
    val vegetarian: Boolean = false,
)

object MenuData {
    val dishes = listOf(
        // Venezuela
        Dish(1, "Arepa reina pepiada", "Arepa de maíz rellena de pollo desmechado, aguacate y mayonesa.", 3800, Country.VENEZUELA, "🫓"),
        Dish(2, "Cachapa con queso de mano", "Tortilla dulce de maíz tierno, doblada con queso de mano derretido.", 4200, Country.VENEZUELA, "🌽", vegetarian = true),
        Dish(3, "Tequeños (6 unidades)", "Palitos de queso blanco envueltos en masa y fritos, con salsa guasacaca.", 3200, Country.VENEZUELA, "🧀", vegetarian = true),
        Dish(4, "Pabellón criollo", "Carne mechada, caraotas negras, arroz blanco y tajadas de plátano maduro.", 5900, Country.VENEZUELA, "🥩"),
        // Costa Rica
        Dish(5, "Gallo pinto con huevo", "Arroz y frijoles con natilla, huevo frito, queso y plátano maduro.", 3500, Country.COSTA_RICA, "🍳", vegetarian = true),
        Dish(6, "Casado con pollo", "Arroz, frijoles, ensalada, plátano maduro, picadillo y pollo en salsa.", 4800, Country.COSTA_RICA, "🍗"),
        Dish(7, "Olla de carne", "Sopa de res con yuca, chayote, elote, tiquizque y arroz aparte.", 5200, Country.COSTA_RICA, "🍲"),
        Dish(8, "Chifrijo", "Arroz, frijoles, chicharrón, pico de gallo y tortilla tostada.", 4300, Country.COSTA_RICA, "🥘", spicy = 1),
        // India
        Dish(9, "Chicken tikka masala", "Pollo marinado en salsa de tomate, crema y especias, con arroz basmati.", 5800, Country.INDIA, "🍛", spicy = 2),
        Dish(10, "Samosas (3 unidades)", "Empanaditas de papa y arvejas con especias, con chutney de menta.", 3400, Country.INDIA, "🥟", spicy = 1, vegetarian = true),
        Dish(11, "Chana masala", "Garbanzos en salsa de tomate, cebolla y especias, con arroz basmati.", 4500, Country.INDIA, "🫘", spicy = 2, vegetarian = true),
        Dish(12, "Biryani de vegetales", "Arroz basmati con azafrán, vegetales salteados y yogur de pepino.", 5000, Country.INDIA, "🍚", spicy = 1, vegetarian = true),
        // Combo
        Dish(13, "Plato de los tres países", "Una arepa pequeña, gallo pinto y chana masala en un solo plato, para probar de todo.", 6500, Country.COMBO, "🌎", spicy = 1, vegetarian = true),
    )

    fun byId(id: Int): Dish? = dishes.firstOrNull { it.id == id }
}

/** Formato de colones: 3500 -> "₡3 500". */
fun colones(amount: Int): String =
    "₡" + NumberFormat.getIntegerInstance(Locale.US).format(amount).replace(',', ' ')
