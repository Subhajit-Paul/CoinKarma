package com.coinkarma.app.sms

import java.util.regex.Pattern

/**
 * Heuristic Indian-bank SMS parser.
 * Works on HDFC / ICICI / SBI / Axis / Kotak / PayTM / GPay formats seen in prod.
 *
 * Returns null when we don't think the text is a debit notification.
 * Claude Code: expand the [merchantPatterns] + [bankIdentifiers] as you meet new templates,
 * and add unit tests in app/src/test/java/com/coinkarma/app/sms/SmsParserTest.kt.
 */
object SmsParser {

    private val amountRx = Pattern.compile(
        "(?:rs\\.?|inr|₹)\\s?([0-9,]+\\.?[0-9]*)",
        Pattern.CASE_INSENSITIVE
    )

    // Words that indicate a *debit* (we ignore credits).
    private val debitHints = listOf(
        "debited", "spent", "paid", "purchase", "txn of", "withdrawn", "sent to", "transferred"
    )
    private val creditHints = listOf("credited", "received from", "refund")

    // Merchant / VPA capture — rough; refine per format.
    private val merchantRx = Pattern.compile(
        "(?:to|at|info[:\\-]|vpa[:\\-]?)\\s?([A-Z0-9._ ]{3,40})",
        Pattern.CASE_INSENSITIVE
    )

    data class Parsed(
        val amount: Double,
        val merchant: String?,
        val raw: String,
        val guessedCategory: String,
    )

    fun parse(body: String): Parsed? {
        val lower = body.lowercase()
        if (creditHints.any { lower.contains(it) }) return null
        if (debitHints.none { lower.contains(it) }) return null

        val amt = amountRx.matcher(body).let {
            if (it.find()) it.group(1)?.replace(",", "")?.toDoubleOrNull() else null
        } ?: return null

        val merchant = merchantRx.matcher(body).let {
            if (it.find()) it.group(1)?.trim()?.take(40) else null
        }

        return Parsed(
            amount = amt,
            merchant = merchant,
            raw = body,
            guessedCategory = guessCategory(merchant, lower),
        )
    }

    private fun guessCategory(merchant: String?, text: String): String {
        val m = (merchant ?: "") + " " + text
        return when {
            m.containsAny("swiggy", "zomato", "dominos", "restaurant", "cafe", "kfc", "mcd") -> "food"
            m.containsAny("uber", "ola", "rapido", "metro", "irctc", "petrol", "fuel") -> "transport"
            m.containsAny("amazon", "flipkart", "myntra", "ajio", "nykaa") -> "shopping"
            m.containsAny("bookmyshow", "netflix", "spotify", "hotstar", "prime") -> "entertainment"
            m.containsAny("apollo", "pharmacy", "hospital", "clinic", "medplus") -> "health"
            m.containsAny("electricity", "bill", "recharge", "broadband", "gas") -> "utilities"
            else -> "other"
        }
    }

    private fun String.containsAny(vararg needles: String): Boolean =
        needles.any { this.contains(it) }
}
