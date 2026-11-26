package com.ivanmurillo.arepapintocurry.data

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.time.YearMonth

// Pago simulado: estas reglas imitan las validaciones de una pasarela real, pero no se cobra nada.

enum class CardBrand(val label: String) { VISA("Visa"), MASTERCARD("Mastercard"), OTHER("Tarjeta") }

fun cardBrand(digits: String): CardBrand = when {
    digits.startsWith("4") -> CardBrand.VISA
    digits.length >= 2 && digits.take(2).toInt() in 51..55 -> CardBrand.MASTERCARD
    digits.length >= 4 && digits.take(4).toInt() in 2221..2720 -> CardBrand.MASTERCARD
    else -> CardBrand.OTHER
}

/** Algoritmo de Luhn: detecta números de tarjeta mal digitados. */
fun luhnValid(digits: String): Boolean {
    if (digits.length < 13 || digits.any { !it.isDigit() }) return false
    var sum = 0
    digits.reversed().forEachIndexed { i, c ->
        var d = c.digitToInt()
        if (i % 2 == 1) {
            d *= 2
            if (d > 9) d -= 9
        }
        sum += d
    }
    return sum % 10 == 0
}

fun cardNumberError(digits: String): String? = when {
    digits.length < 16 -> "Faltan dígitos (${digits.length}/16)"
    cardBrand(digits) == CardBrand.OTHER -> "Solo se aceptan Visa o Mastercard"
    !luhnValid(digits) -> "El número de tarjeta no es válido"
    else -> null
}

/** [mmyy] son 4 dígitos, por ejemplo "0829" = agosto 2029. */
fun expiryError(mmyy: String, now: YearMonth = YearMonth.now()): String? {
    if (mmyy.length < 4) return "Usa el formato MM/AA"
    val month = mmyy.take(2).toInt()
    val year = 2000 + mmyy.takeLast(2).toInt()
    if (month !in 1..12) return "El mes debe estar entre 01 y 12"
    return if (YearMonth.of(year, month) < now) "La tarjeta está vencida" else null
}

fun cvvError(cvv: String): String? = if (cvv.length < 3) "El CVV tiene 3 dígitos" else null

fun nameError(name: String): String? = if (name.trim().length < 3) "Escribe el nombre como aparece en la tarjeta" else null

/** Números de celular en Costa Rica: 8 dígitos que empiezan con 6, 7 u 8. */
fun phoneError(digits: String): String? = when {
    digits.length < 8 -> "El número tiene 8 dígitos"
    digits.first() !in "678" -> "Debe ser un celular (empieza con 6, 7 u 8)"
    else -> null
}

/** Tarjeta de prueba pública de Visa (pasa la validación de Luhn). */
const val TEST_CARD = "4111111111111111"

/** Termina en 0002: la simulación la rechaza para mostrar el caso de pago fallido. */
fun isDeclinedTestCard(digits: String) = digits.endsWith("0002")

/** Muestra los dígitos con un separador cada [group] dígitos, sin guardarlo en el texto real. */
class GroupedDigitsTransformation(private val groups: List<Int>, private val separator: Char) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        // posiciones (en el texto original) antes de las cuales va un separador
        val breaks = groups.runningReduce(Int::plus).dropLast(1)
        val out = StringBuilder()
        raw.forEachIndexed { i, c ->
            if (i in breaks) out.append(separator)
            out.append(c)
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                offset + breaks.count { it < offset }

            override fun transformedToOriginal(offset: Int): Int {
                var seps = 0
                breaks.forEachIndexed { k, b -> if (b + k < offset) seps++ }
                return (offset - seps).coerceIn(0, raw.length)
            }
        }
        return TransformedText(AnnotatedString(out.toString()), mapping)
    }
}

val CardNumberTransformation = GroupedDigitsTransformation(listOf(4, 4, 4, 4), ' ')
val ExpiryTransformation = GroupedDigitsTransformation(listOf(2, 2), '/')
val PhoneTransformation = GroupedDigitsTransformation(listOf(4, 4), '-')

fun formatPhone(digits: String) = if (digits.length == 8) digits.take(4) + "-" + digits.drop(4) else digits
