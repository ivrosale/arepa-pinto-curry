package com.ivanmurillo.arepapintocurry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ivanmurillo.arepapintocurry.ui.CartViewModel
import com.ivanmurillo.arepapintocurry.ui.screens.CartScreen
import com.ivanmurillo.arepapintocurry.ui.screens.DishDetailScreen
import com.ivanmurillo.arepapintocurry.ui.screens.LocationScreen
import com.ivanmurillo.arepapintocurry.ui.screens.MenuScreen
import com.ivanmurillo.arepapintocurry.ui.screens.PaymentScreen
import com.ivanmurillo.arepapintocurry.ui.screens.ReceiptScreen
import com.ivanmurillo.arepapintocurry.ui.theme.ArepaPintoCurryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArepaPintoCurryTheme {
                AppNavigation()
            }
        }
    }
}

/** Rutas de la app: menú -> detalle -> carrito -> ubicación -> pago -> comprobante. */
@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    // Un solo carrito para toda la actividad
    val cart: CartViewModel = viewModel()

    NavHost(navController = nav, startDestination = "menu") {
        composable("menu") {
            MenuScreen(
                cart = cart,
                onDishClick = { id -> nav.navigate("dish/$id") },
                onCartClick = { nav.navigate("cart") },
            )
        }
        composable("dish/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
            DishDetailScreen(
                dishId = entry.arguments?.getInt("id") ?: 0,
                cart = cart,
                onBack = { nav.popBackStack() },
            )
        }
        composable("cart") {
            CartScreen(
                cart = cart,
                onBack = { nav.popBackStack() },
                onContinue = { nav.navigate("location") },
            )
        }
        composable("location") {
            LocationScreen(
                cart = cart,
                onBack = { nav.popBackStack() },
                onContinue = { nav.navigate("payment") },
            )
        }
        composable("payment") {
            PaymentScreen(
                cart = cart,
                onBack = { nav.popBackStack() },
                // Después de pagar no se puede volver al pago: se limpia el historial hasta el menú
                onPaid = { nav.navigate("receipt") { popUpTo("menu") } },
            )
        }
        composable("receipt") {
            ReceiptScreen(
                order = cart.lastOrder,
                onNewOrder = { nav.popBackStack("menu", inclusive = false) },
            )
        }
    }
}
