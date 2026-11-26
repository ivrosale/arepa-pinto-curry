# Arepa, Pinto & Curry

Aplicación Android para hacer pedidos en un restaurante que reúne la comida de Venezuela, Costa Rica y la India. Es el proyecto final del curso **BISV-15 Desarrollo de Aplicaciones Móviles** de la Universidad San Marcos (Costa Rica), noviembre de 2026.

**Autor:** Ivan Andrés Murillo Rosales · **Profesora:** Ing. Jacqueline Méndez Montero

## Qué hace

| Pantalla | Funciones |
| --- | --- |
| Menú | 13 platillos en colones, filtros por país y combos, etiquetas de picante y vegetariano |
| Detalle | Descripción del platillo y selector de cantidad |
| Carrito | Sumar, restar y quitar productos, vaciar con confirmación, subtotal y total |
| Ubicación | Permiso y GPS real, mapa de OpenStreetMap, pin ajustable tocando el mapa, dirección, señas y costo de envío por distancia |
| Pago | Pago simulado con tarjeta (Visa o Mastercard, con validaciones) o SINPE Móvil |
| Comprobante | Número de orden, fecha, código de autorización, detalle, total, dirección y botón para compartir |

**Costo de envío:** ₡1 000 hasta 2 km y ₡350 por cada km adicional. No se entrega a más de 8 km del restaurante (Barrio Escalante, San José).

El pago es **simulado**: no se cobra nada ni se guardan los datos de la tarjeta.

## Tecnología

- Kotlin con Jetpack Compose (Material 3)
- Navigation Compose y ViewModel
- osmdroid (mapas de OpenStreetMap, sin clave de API)
- Google Play Services Location (GPS)
- Android Studio, `minSdk` 26 y `targetSdk` 37

## Cómo correrla

1. Abrir el proyecto en Android Studio y esperar el *Gradle sync*.
2. Elegir un emulador o un teléfono y presionar **Run**.
3. En el emulador, poner una ubicación en San José (Extended controls → Location); si no, la dirección sale fuera de la zona de entrega.

## Datos de prueba

| Para qué | Dato |
| --- | --- |
| Tarjeta aprobada | 4111 1111 1111 1111 (o el botón «Usar tarjeta de prueba») |
| Tarjeta rechazada | 4000 0000 0000 0002 |
| SINPE Móvil | Cualquier celular de 8 dígitos que empiece con 6, 7 u 8 |

## Estructura del código

```
app/src/main/java/com/ivanmurillo/arepapintocurry/
├── MainActivity.kt          navegación entre pantallas
├── data/                    menú, entrega, pago y ubicación
└── ui/
    ├── CartViewModel.kt     carrito compartido y pedido
    ├── Order.kt             pedido pagado (comprobante)
    ├── components/          mapa y elementos reutilizables
    ├── screens/             una pantalla por archivo
    └── theme/               colores de la marca
```

## Modelo de negocio

La app sale del Lienzo de Modelo de Negocio (Business Model Canvas) del restaurante. La propuesta de valor es probar tres cocinas en un solo pedido, y cada pantalla responde a un bloque del lienzo: el menú y su detalle a la propuesta de valor; el carrito, el envío y el pago a las fuentes de ingresos; y el comprobante a la relación con el cliente.
