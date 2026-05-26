package com.coinkarma.app.platform.receivers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SmsParserTest {

    // ── HDFC ─────────────────────────────────────────────────────────────────

    @Test fun `hdfc debit card purchase`() {
        val sms = "HDFC Bank: Rs.450.00 spent on HDFC Bank Debit Card ending XXXX on 22-Apr-26 at SWIGGY. " +
                "Avl Bal: Rs.12345.50. Not you? Call 18002586161."
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)
        assertEquals(450.0, parsed!!.amount, 0.01)
        assertEquals("food", parsed.guessedCategory)
    }

    @Test fun `hdfc upi debit`() {
        val sms = "Rs.200 debited from HDFC A/c XXXX1234 on 22-APR-26 via UPI. Ref no 12345678. " +
                "Info: UPI-ZOMATO-zomato@icici. If not done by you call 18002586161."
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)
        assertEquals(200.0, parsed!!.amount, 0.01)
        assertEquals("food", parsed.guessedCategory)
    }

    @Test fun `hdfc credit is ignored`() {
        val sms = "HDFC Bank: Rs.5000.00 credited to your HDFC Bank Account ending XXXX on 22-Apr-26."
        assertNull(SmsParser.parse(sms))
    }

    // ── ICICI ─────────────────────────────────────────────────────────────────

    @Test fun `icici upi sent`() {
        val sms = "ICICI Bank Acct XX1234 debited with INR 350.00 on 22-Apr-26; " +
                "UPI Ref 4123456789. Sent to UBER INDIA."
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)
        assertEquals(350.0, parsed!!.amount, 0.01)
        assertEquals("transport", parsed.guessedCategory)
    }

    @Test fun `icici shopping purchase`() {
        val sms = "Dear Customer, INR 1,299.00 has been debited from your ICICI Bank Account XX5678 " +
                "for purchase at AMAZON on 22-Apr-26."
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)
        assertEquals(1299.0, parsed!!.amount, 0.01)
        assertEquals("shopping", parsed.guessedCategory)
    }

    // ── SBI ───────────────────────────────────────────────────────────────────

    @Test fun `sbi debit`() {
        val sms = "Your A/C XXXX1234 is debited with Rs.750 on 22-04-26. " +
                "UPI Ref:123456789. Transferred to PETROL STATION. -SBI"
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)
        assertEquals(750.0, parsed!!.amount, 0.01)
        assertEquals("transport", parsed.guessedCategory)
    }

    @Test fun `sbi received is ignored`() {
        val sms = "Your SBI A/C XXXX1234 credited with Rs.10000.00 on 22-04-26. UPI Ref:987654321."
        assertNull(SmsParser.parse(sms))
    }

    // ── Axis ──────────────────────────────────────────────────────────────────

    @Test fun `axis debit card entertainment`() {
        val sms = "Axis Bank: Txn of Rs.299.00 done using Debit Card XXXX1234 at NETFLIX on 22-Apr-26. " +
                "Avl bal: Rs.8500."
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)
        assertEquals(299.0, parsed!!.amount, 0.01)
        assertEquals("entertainment", parsed.guessedCategory)
    }

    @Test fun `axis upi transfer`() {
        val sms = "Rs.500 transferred via UPI from Axis Bank A/c XXXX5678 to rapido@ybl on 22-Apr-26. " +
                "UPI Ref: 423456789012."
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)
        assertEquals(500.0, parsed!!.amount, 0.01)
        assertEquals("transport", parsed.guessedCategory)
    }

    // ── Kotak ─────────────────────────────────────────────────────────────────

    @Test fun `kotak spend`() {
        val sms = "Rs.1,500.00 spent on your Kotak Debit Card XXXX1234 at APOLLO PHARMACY on 22-Apr-26. " +
                "Avl Bal:Rs.23000."
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)
        assertEquals(1500.0, parsed!!.amount, 0.01)
        assertEquals("health", parsed.guessedCategory)
    }

    // ── PayTM ─────────────────────────────────────────────────────────────────

    @Test fun `paytm paid`() {
        val sms = "Payment of Rs.120 is successful. Paid to BOOKMYSHOW via Paytm UPI ID paytm@paytm. " +
                "Transaction ID TXN123456."
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)
        assertEquals(120.0, parsed!!.amount, 0.01)
        assertEquals("entertainment", parsed.guessedCategory)
    }

    @Test fun `paytm refund is ignored`() {
        val sms = "Refund of Rs.250 received from SWIGGY to your Paytm Wallet."
        assertNull(SmsParser.parse(sms))
    }

    // ── GPay ──────────────────────────────────────────────────────────────────

    @Test fun `gpay sent`() {
        val sms = "You have sent Rs.80 to MEDPLUS via Google Pay on 22-Apr-26. " +
                "UPI Ref ID: 401234567890."
        // "sent rs" matches the updated debit hint
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)
        assertEquals(80.0, parsed!!.amount, 0.01)
        assertEquals("health", parsed.guessedCategory)
    }

    @Test fun `gpay electricity bill`() {
        val sms = "Rs.1,250.00 paid to ELECTRICITY BOARD via Google Pay. " +
                "Transaction ID GPAY001234. 22-Apr-26."
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)
        assertEquals(1250.0, parsed!!.amount, 0.01)
        assertEquals("utilities", parsed.guessedCategory)
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test fun `amount with comma separators`() {
        val sms = "Rs.1,00,000 withdrawn from your account XXXX9999 via UPI."
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)
        assertEquals(100000.0, parsed!!.amount, 0.01)
    }

    @Test fun `rupee symbol variant`() {
        val sms = "₹350.50 debited from your account XXXX1234 at OLA."
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)
        assertEquals(350.50, parsed!!.amount, 0.01)
        assertEquals("transport", parsed.guessedCategory)
    }

    @Test fun `non-transaction sms is ignored`() {
        val sms = "Your OTP for login is 482910. Valid for 10 minutes. Do not share with anyone."
        assertNull(SmsParser.parse(sms))
    }

    @Test fun `promotional sms is ignored`() {
        // No debit hint keywords — parser should return null
        val sms = "Congratulations! You've won Rs.50 cashback on your next order. Use code SAVE50 to redeem."
        assertNull(SmsParser.parse(sms))
    }

    @Test fun `category falls back to other`() {
        val sms = "Rs.200 debited from your account XXXX1234 at RANDOM STORE."
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)
        assertEquals("other", parsed!!.guessedCategory)
    }
}
