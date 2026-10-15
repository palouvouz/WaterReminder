package com.example.waterreminder

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

typealias Done = (Result<Unit>) -> Unit

/**
 * Όλες οι κλήσεις προς το Firebase Authentication περνούν από εδώ.
 *
 * Ασφάλεια:
 *  - Δεν διαβάζονται ούτε αποθηκεύονται ID tokens ή refresh tokens. Τη συνεδρία τη διαχειρίζεται το SDK.
 *  - Δεν υπάρχουν έλεγχοι διαχειριστή (admin) στην εφαρμογή. Αν χρειαστούν, θα γίνονται από
 *    custom claims που ελέγχονται σε Security Rules ή server, ποτέ από τον κώδικα του πελάτη.
 */
object AuthRepository {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private fun <T> Task<T>.report(done: Done, onSuccess: () -> Unit = {}) {
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
                done(Result.success(Unit))
            } else {
                done(Result.failure(task.exception ?: IllegalStateException("Άγνωστο σφάλμα")))
            }
        }
    }

    fun signInWithPassword(email: String, password: String, done: Done) {
        auth.signInWithEmailAndPassword(email.trim(), password).report(done)
    }

    /** Δημιουργεί λογαριασμό και στέλνει email επιβεβαίωσης. */
    fun signUp(email: String, password: String, done: Done) {
        auth.createUserWithEmailAndPassword(email.trim(), password).report(done) {
            auth.currentUser?.sendEmailVerification()
        }
    }

    fun sendPasswordReset(email: String, done: Done) {
        auth.sendPasswordResetEmail(email.trim()).report(done)
    }

    // ---- Σύνδεση με σύνδεσμο email (χωρίς κωδικό) ----

    fun sendSignInLink(ctx: Context, email: String, done: Done) {
        val settings = ActionCodeSettings.newBuilder()
            .setUrl(AuthConfig.CONTINUE_URL)
            .setHandleCodeInApp(true)
            .setAndroidPackageName(ctx.packageName, true, AuthConfig.MIN_APP_VERSION)
            .build()
        auth.sendSignInLinkToEmail(email.trim(), settings).report(done) {
            // Απαραίτητο ώστε ο σύνδεσμος να ολοκληρωθεί στην ίδια συσκευή χωρίς νέα πληκτρολόγηση.
            AuthStore.savePendingEmail(ctx, email.trim())
        }
    }

    fun isSignInLink(link: String): Boolean = auth.isSignInWithEmailLink(link)

    fun completeSignInLink(ctx: Context, email: String, link: String, done: Done) {
        auth.signInWithEmailLink(email.trim(), link).report(done) {
            AuthStore.clearPendingEmail(ctx)
        }
    }

    // ---- Google ----

    fun signInWithGoogle(idToken: String, done: Done) {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).report(done)
    }

    // ---- Αποσύνδεση ----

    suspend fun signOut(ctx: Context) {
        auth.signOut()
        AuthStore.clearPendingEmail(ctx)
        try {
            // Ώστε την επόμενη φορά να ζητηθεί ξανά επιλογή λογαριασμού Google.
            CredentialManager.create(ctx).clearCredentialState(ClearCredentialStateRequest())
        } catch (e: ClearCredentialException) {
            // Δεν επηρεάζει την αποσύνδεση από το Firebase.
        }
    }
}
