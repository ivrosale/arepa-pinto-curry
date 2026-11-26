package com.ivanmurillo.arepapintocurry.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ivanmurillo.arepapintocurry.data.Country
import com.ivanmurillo.arepapintocurry.data.Dish
import com.ivanmurillo.arepapintocurry.data.MenuData
import com.ivanmurillo.arepapintocurry.data.colones
import com.ivanmurillo.arepapintocurry.ui.CartViewModel
import com.ivanmurillo.arepapintocurry.ui.components.DishImage
import com.ivanmurillo.arepapintocurry.ui.components.DishTags
import com.ivanmurillo.arepapintocurry.ui.components.QuantityStepper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(cart: CartViewModel, onDishClick: (Int) -> Unit, onCartClick: () -> Unit) {
    // null = todos los países
    var filter by rememberSaveable { mutableStateOf<Country?>(null) }
    val visible = MenuData.dishes.filter { filter == null || it.country == filter }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Arepa, Pinto & Curry", fontWeight = FontWeight.Bold)
                        Text("Venezuela · Costa Rica · India", style = MaterialTheme.typography.labelMedium)
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick) {
                        BadgedBox(badge = { if (cart.itemCount > 0) Badge { Text(cart.itemCount.toString()) } }) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        bottomBar = {
            if (cart.itemCount > 0) {
                Surface(shadowElevation = 8.dp) {
                    Button(
                        onClick = onCartClick,
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                    ) {
                        val plural = if (cart.itemCount == 1) "producto" else "productos"
                        Text("Ver carrito · ${cart.itemCount} $plural · ${colones(cart.subtotal)}")
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                Text(
                    "¿Qué se te antoja hoy?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                )
                Text(
                    "Tres amigos, tres países, un solo menú.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("Todos") }) }
                    items(Country.entries) { c ->
                        val label = if (c == Country.COMBO) "🌎 Combos" else "${c.flag} ${c.label}"
                        FilterChip(selected = filter == c, onClick = { filter = c }, label = { Text(label) })
                    }
                }
            }
            visible.groupBy { it.country }.forEach { (country, dishes) ->
                item(key = "h-${country.name}") {
                    Text(
                        "${country.flag}  ${country.label}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                    )
                }
                items(dishes, key = { it.id }) { dish ->
                    DishCard(
                        dish = dish,
                        quantity = cart.quantityOf(dish.id),
                        onClick = { onDishClick(dish.id) },
                        onAdd = { cart.add(dish.id) },
                        onDecrease = { cart.decrease(dish.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DishCard(dish: Dish, quantity: Int, onClick: () -> Unit, onAdd: () -> Unit, onDecrease: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            DishImage(dish, 72.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(dish.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    dish.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                DishTags(dish)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        colones(dish.price),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    if (quantity == 0) {
                        FilledTonalButton(onClick = onAdd) { Text("Agregar") }
                    } else {
                        QuantityStepper(quantity, onDecrease = onDecrease, onIncrease = onAdd)
                    }
                }
            }
        }
    }
}
