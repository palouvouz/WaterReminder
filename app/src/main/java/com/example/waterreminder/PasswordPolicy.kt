package com.example.waterreminder

data class PasswordCheck(
    /** 0..4 */
    val score: Int,
    /** Τι λείπει, με σύντομες φράσεις. */
    val missing: List<String>
) {
    val acceptable: Boolean get() = missing.isEmpty()
}

/**
 * Έλεγχος ισχύος κωδικού στην πλευρά του πελάτη (για την εμπειρία χρήστη).
 * Η υποχρεωτική επιβολή γίνεται από το Firebase: Authentication → Settings → Password policy.
 */
object PasswordPolicy {
    const val MIN_LENGTH = 10

    private val COMMON = setOf(
        "password", "password1", "password123", "passw0rd", "12345678", "123456789", "1234567890",
        "qwertyuiop", "qwerty123", "iloveyou", "letmein", "welcome1", "admin123", "abc12345",
        "11111111", "00000000", "kodikos", "κωδικός"
    )

    fun check(password: String, email: String): PasswordCheck {
        val missing = mutableListOf<String>()
        if (password.length < MIN_LENGTH) missing += "$MIN_LENGTH+ χαρακτήρες"

        val classes = listOf(
            password.any { it.isLowerCase() },
            password.any { it.isUpperCase() },
            password.any { it.isDigit() },
            password.any { !it.isLetterOrDigit() }
        ).count { it }
        if (classes < 3) missing += "3 από: πεζά, κεφαλαία, αριθμοί, σύμβολα"

        val lower = password.lowercase()
        val emailName = email.substringBefore('@').lowercase()
        var weak = false
        if (password.isNotEmpty() && (lower in COMMON || password.toSet().size <= 2)) {
            missing += "όχι συνηθισμένος κωδικός"
            weak = true
        }
        if (emailName.length >= 3 && lower.contains(emailName)) {
            missing += "χωρίς το email σου μέσα"
            weak = true
        }

        var score = 0
        if (password.length >= MIN_LENGTH) score++
        if (password.length >= 14) score++
        if (classes >= 3) score++
        if (classes == 4) score++
        if (weak) score = minOf(score, 1)
        if (password.isEmpty()) score = 0
        return PasswordCheck(score, missing)
    }
}
