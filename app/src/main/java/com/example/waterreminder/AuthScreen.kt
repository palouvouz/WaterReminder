package com.example.waterreminder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseTooManyRequestsException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AuthMode { SignIn, SignUp, Reset, EmailLink, ConfirmLink }

private const val RESET_INFO = "Αν υπάρχει λογαριασμός με αυτό το email, θα λάβεις σύνδεσμο επαναφοράς."

/**
 * Οθόνη σύνδεσης. `pendingLink` είναι ο σύνδεσμος email που άνοιξε την εφαρμογή (αν υπάρχει).
 * Μετά την επιτυχή σύνδεση, την αλλαγή οθόνης την κάνει ο AuthStateListener στο MainActivity.kt.
 */
@Composable
fun AuthScreen(pendingLink: String?, onLinkConsumed: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var mode by remember { mutableStateOf(AuthMode.SignIn) }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    var lockSeconds by remember { mutableIntStateOf(AuthStore.lockRemainingSeconds(ctx)) }
    var emailWait by remember { mutableIntStateOf(AuthStore.emailCooldownSeconds(ctx)) }

    LaunchedEffect(lockSeconds) {
        if (lockSeconds > 0) {
            delay(1000)
            lockSeconds = AuthStore.lockRemainingSeconds(ctx)
        }
    }
    LaunchedEffect(emailWait) {
        if (emailWait > 0) {
            delay(1000)
            emailWait = AuthStore.emailCooldownSeconds(ctx)
        }
    }

    fun switchTo(next: AuthMode) {
        mode = next; error = null; info = null; pass = ""
    }

    fun fail(t: Throwable) {
        error = authErrorMessage(t)
        if (t is FirebaseTooManyRequestsException) {
            AuthStore.lockFor(ctx, 300)
            lockSeconds = AuthStore.lockRemainingSeconds(ctx)
        }
    }

    fun emailSent(message: String) {
        AuthStore.markEmailSent(ctx)
        emailWait = AuthStore.emailCooldownSeconds(ctx)
        info = message
    }

    fun completeLink(link: String, mail: String) {
        busy = true; error = null
        AuthRepository.completeSignInLink(ctx, mail, link) { r ->
            busy = false
            r.onSuccess { onLinkConsumed() }
            r.onFailure { fail(it); mode = AuthMode.ConfirmLink }
        }
    }

    LaunchedEffect(pendingLink) {
        if (pendingLink != null) {
            val saved = AuthStore.pendingEmail(ctx)
            if (saved != null) completeLink(pendingLink, saved) else switchTo(AuthMode.ConfirmLink)
        }
    }

    fun signIn() {
        if (lockSeconds > 0) return
        busy = true; error = null
        AuthRepository.signInWithPassword(email, pass) { r ->
            busy = false
            r.onSuccess { AuthStore.registerSuccess(ctx) }
            r.onFailure {
                if (isCredentialError(it)) {
                    AuthStore.registerFailure(ctx)
                    lockSeconds = AuthStore.lockRemainingSeconds(ctx)
                }
                fail(it)
            }
        }
    }

    fun signUp() {
        if (!PasswordPolicy.check(pass, email).acceptable) return
        busy = true; error = null
        AuthRepository.signUp(email, pass) { r ->
            busy = false
            r.onFailure { fail(it) }
        }
    }

    fun sendReset() {
        busy = true; error = null; info = null
        AuthRepository.sendPasswordReset(email) { r ->
            busy = false
            r.onSuccess { emailSent(RESET_INFO) }
            r.onFailure {
                // Ίδιο μήνυμα και για email που δεν υπάρχει, ώστε να μη διαρρέουν λογαριασμοί.
                if (isTransientError(it)) fail(it) else emailSent(RESET_INFO)
            }
        }
    }

    fun sendLink() {
        busy = true; error = null; info = null
        AuthRepository.sendSignInLink(ctx, email) { r ->
            busy = false
            r.onSuccess { emailSent("Στείλαμε σύνδεσμο στο email σου. Άνοιξέ τον σε αυτή τη συσκευή.") }
            r.onFailure { fail(it) }
        }
    }

    fun handleGoogle(outcome: GoogleOutcome) {
        when (outcome) {
            is GoogleOutcome.Token -> AuthRepository.signInWithGoogle(outcome.idToken) { r ->
                busy = false
                r.onFailure { fail(it) }
            }
            GoogleOutcome.Cancelled -> busy = false
            GoogleOutcome.NoAccount -> {
                busy = false
                error = "Δεν βρέθηκε λογαριασμός Google σε αυτή τη συσκευή."
            }
            GoogleOutcome.NotConfigured -> {
                busy = false
                error = "Η σύνδεση με Google δεν έχει ρυθμιστεί ακόμα (δες docs/BUILD_APK.md)."
            }
            GoogleOutcome.Failed -> {
                busy = false
                error = "Η σύνδεση με Google απέτυχε. Δοκίμασε ξανά."
            }
        }
    }

    fun google() {
        busy = true; error = null; info = null
        scope.launch { handleGoogle(GoogleSignInHelper.requestIdToken(ctx)) }
    }

    val title = when (mode) {
        AuthMode.SignIn -> "Καλώς ήρθες"
        AuthMode.SignUp -> "Δημιούργησε λογαριασμό"
        AuthMode.Reset -> "Επαναφορά κωδικού"
        AuthMode.EmailLink -> "Σύνδεση χωρίς κωδικό"
        AuthMode.ConfirmLink -> "Επιβεβαίωσε το email σου"
    }
    val subtitle = when (mode) {
        AuthMode.SignIn -> "Συνδέσου για να ρυθμίσεις τις υπενθυμίσεις σου."
        AuthMode.SignUp -> "Θα σου στείλουμε email για να επιβεβαιώσεις τη διεύθυνση."
        AuthMode.Reset -> "Γράψε το email σου και θα λάβεις σύνδεσμο επαναφοράς."
        AuthMode.EmailLink -> "Θα λάβεις στο email σου σύνδεσμο που σε συνδέει με ένα πάτημα."
        AuthMode.ConfirmLink -> "Άνοιξες τον σύνδεσμο από άλλη συσκευή. Γράψε το email στο οποίο τον έλαβες."
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Spacer(Modifier.height(48.dp))
        Text("Water", color = BlueSoft, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(title, color = Ink, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = Muted, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(28.dp))

        AppField(email, { email = it }, "Email", KeyboardType.Email)
        if (mode == AuthMode.SignIn || mode == AuthMode.SignUp) {
            Spacer(Modifier.height(12.dp))
            AppField(pass, { pass = it }, "Κωδικός", password = true)
            if (mode == AuthMode.SignUp) PasswordMeter(PasswordPolicy.check(pass, email))
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = Danger, style = MaterialTheme.typography.bodyMedium)
        }
        info?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = Teal, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(24.dp))

        when (mode) {
            AuthMode.SignIn -> {
                GlassButton(
                    if (lockSeconds > 0) "Δοκίμασε ξανά σε ${lockSeconds}s" else "Σύνδεση",
                    Modifier.fillMaxWidth(), primary = true,
                    enabled = !busy && lockSeconds == 0 && looksLikeEmail(email) && pass.isNotEmpty()
                ) { signIn() }
                LinkText("Ξέχασα τον κωδικό") { switchTo(AuthMode.Reset) }
                OrDivider()
                GlassButton("Συνέχεια με Google", Modifier.fillMaxWidth(), enabled = !busy) { google() }
                Spacer(Modifier.height(12.dp))
                GlassButton("Σύνδεσμος στο email, χωρίς κωδικό", Modifier.fillMaxWidth(), enabled = !busy) {
                    switchTo(AuthMode.EmailLink)
                }
                Spacer(Modifier.height(8.dp))
                LinkText("Νέος λογαριασμός") { switchTo(AuthMode.SignUp) }
            }
            AuthMode.SignUp -> {
                GlassButton(
                    "Εγγραφή", Modifier.fillMaxWidth(), primary = true,
                    enabled = !busy && looksLikeEmail(email) && PasswordPolicy.check(pass, email).acceptable
                ) { signUp() }
                OrDivider()
                GlassButton("Συνέχεια με Google", Modifier.fillMaxWidth(), enabled = !busy) { google() }
                Spacer(Modifier.height(8.dp))
                LinkText("Έχω ήδη λογαριασμό") { switchTo(AuthMode.SignIn) }
            }
            AuthMode.Reset, AuthMode.EmailLink -> {
                GlassButton(
                    if (emailWait > 0) "Περίμενε ${emailWait}s" else "Στείλε σύνδεσμο",
                    Modifier.fillMaxWidth(), primary = true,
                    enabled = !busy && emailWait == 0 && looksLikeEmail(email)
                ) { if (mode == AuthMode.Reset) sendReset() else sendLink() }
                Spacer(Modifier.height(8.dp))
                LinkText("Πίσω στη σύνδεση") { switchTo(AuthMode.SignIn) }
            }
            AuthMode.ConfirmLink -> {
                GlassButton(
                    "Ολοκλήρωση σύνδεσης", Modifier.fillMaxWidth(), primary = true,
                    enabled = !busy && looksLikeEmail(email) && pendingLink != null
                ) { pendingLink?.let { completeLink(it, email) } }
                Spacer(Modifier.height(8.dp))
                LinkText("Ακύρωση") {
                    onLinkConsumed()
                    switchTo(AuthMode.SignIn)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        CreditsText()
        Spacer(Modifier.height(16.dp))
    }
}
