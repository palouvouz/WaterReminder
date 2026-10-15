package com.example.waterreminder

import android.util.Patterns
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

fun looksLikeEmail(value: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches()

/** Αποτυχία λόγω λάθους στοιχείων (μετράει στο τοπικό κλείδωμα). */
fun isCredentialError(t: Throwable): Boolean =
    t is FirebaseAuthInvalidCredentialsException || t is FirebaseAuthInvalidUserException

/** Σφάλματα που δεν πρέπει να κρύβονται πίσω από «αν υπάρχει λογαριασμός...». */
fun isTransientError(t: Throwable): Boolean =
    t is FirebaseTooManyRequestsException || t is FirebaseNetworkException

/**
 * Ελληνικά μηνύματα. Για λάθος email ή κωδικό δίνεται ένα κοινό μήνυμα, ώστε να μη φαίνεται
 * αν υπάρχει λογαριασμός με το συγκεκριμένο email.
 */
fun authErrorMessage(t: Throwable): String = when (t) {
    is FirebaseTooManyRequestsException -> "Πάρα πολλές προσπάθειες. Δοκίμασε ξανά σε λίγα λεπτά."
    is FirebaseNetworkException -> "Δεν υπάρχει σύνδεση στο internet."
    is FirebaseAuthUserCollisionException -> "Υπάρχει ήδη λογαριασμός με αυτό το email. Δοκίμασε σύνδεση ή επαναφορά κωδικού."
    is FirebaseAuthWeakPasswordException -> "Ο κωδικός είναι πολύ αδύναμος."
    is FirebaseAuthInvalidUserException -> when (t.errorCode) {
        "ERROR_USER_DISABLED" -> "Ο λογαριασμός είναι απενεργοποιημένος."
        else -> "Λάθος email ή κωδικός."
    }
    is FirebaseAuthInvalidCredentialsException -> when (t.errorCode) {
        "ERROR_INVALID_EMAIL" -> "Το email δεν είναι σωστά γραμμένο."
        "ERROR_EXPIRED_ACTION_CODE" -> "Ο σύνδεσμος έχει λήξει. Ζήτησε νέο."
        "ERROR_INVALID_ACTION_CODE" -> "Ο σύνδεσμος δεν είναι έγκυρος ή έχει ήδη χρησιμοποιηθεί."
        else -> "Λάθος email ή κωδικός."
    }
    is FirebaseAuthException -> when (t.errorCode) {
        "ERROR_OPERATION_NOT_ALLOWED" -> "Αυτός ο τρόπος σύνδεσης δεν είναι ενεργοποιημένος στο Firebase."
        "ERROR_EXPIRED_ACTION_CODE" -> "Ο σύνδεσμος έχει λήξει. Ζήτησε νέο."
        "ERROR_INVALID_ACTION_CODE" -> "Ο σύνδεσμος δεν είναι έγκυρος ή έχει ήδη χρησιμοποιηθεί."
        else -> "Η σύνδεση απέτυχε. Δοκίμασε ξανά."
    }
    else -> "Κάτι πήγε στραβά. Δοκίμασε ξανά."
}
