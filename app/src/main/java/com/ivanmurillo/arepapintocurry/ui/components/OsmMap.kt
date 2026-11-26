package com.ivanmurillo.arepapintocurry.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File

/**
 * Mapa de OpenStreetMap (osmdroid). Muestra el restaurante y el punto de entrega.
 * Tocar el mapa mueve el punto de entrega. Cada vez que cambia [centerKey] la cámara va a [centerOn].
 */
@Composable
fun OsmMap(
    selected: GeoPoint,
    restaurant: GeoPoint,
    centerOn: GeoPoint,
    centerKey: Int,
    onTap: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val currentOnTap by rememberUpdatedState(onTap)

    val mapView = remember {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(15.5)
            controller.setCenter(centerOn)
        }
    }
    val deliveryMarker = remember {
        Marker(mapView).apply {
            icon = emojiDrawable(context, "📍", 110)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            setOnMarkerClickListener { _, _ -> true }
        }
    }
    remember {
        val restaurantMarker = Marker(mapView).apply {
            position = restaurant
            icon = emojiDrawable(context, "🍽️", 90)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            setOnMarkerClickListener { _, _ -> true }
        }
        val taps = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                currentOnTap(p); return true
            }
            override fun longPressHelper(p: GeoPoint): Boolean = false
        })
        mapView.overlays.add(taps)
        mapView.overlays.add(restaurantMarker)
        mapView.overlays.add(deliveryMarker)
        true
    }

    // El mapa necesita saber cuándo la pantalla se pausa o se reanuda
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    LaunchedEffect(centerKey) {
        if (centerKey > 0) mapView.controller.animateTo(centerOn, 17.0, 800L)
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = {
            deliveryMarker.position = selected
            it.invalidate()
        },
    )
}

/** Convierte un emoji en un ícono para los marcadores del mapa. */
private fun emojiDrawable(context: Context, emoji: String, sizePx: Int): BitmapDrawable {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sizePx * 0.8f
        textAlign = Paint.Align.CENTER
    }
    val y = sizePx / 2f - (paint.descent() + paint.ascent()) / 2f
    Canvas(bitmap).drawText(emoji, sizePx / 2f, y, paint)
    return BitmapDrawable(context.resources, bitmap)
}
