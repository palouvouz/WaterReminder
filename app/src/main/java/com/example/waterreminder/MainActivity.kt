package com.example.waterreminder

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var tick by mutableIntStateOf(0)
    private var pendingLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Notifier.ensureChannels(this)
        acceptLink(intent)
        setContent {
            AppTheme {
                App(tick, pendingLink) {
                    pendingLink = null
                    intent.data = null
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        acceptLink(intent)
    }

    override fun onResume() {
        super.onResume()
        tick++
    }

    /** Δέχεται μόνο https σύνδεσμο του δικού μας Firebase Hosting domain και έγκυρο σύνδεσμο σύνδεσης. */
    private fun acceptLink(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != "https" || uri.host != AuthConfig.LINK_HOST) return
        val link = uri.toString()
        if (AuthRepository.isSignInLink(link)) pendingLink = link
    }
}

@Composable
fun App(tick: Int, pendingLink: String?, onLinkConsumed: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { Prefs(ctx) }
    val auth = remember { FirebaseAuth.getInstance() }

    // Η συνεδρία παραμένει ενεργή μεταξύ εκκινήσεων: την κρατά το Firebase SDK, όχι η εφαρμογή.
    // Η αρχική τιμή έρχεται από την τοπική σημαία (χωρίς αναλαμπή της οθόνης σύνδεσης) και ο
    // AuthStateListener τη διορθώνει μόλις το SDK φορτώσει τη συνεδρία.
    var loggedIn by remember { mutableStateOf(prefs.signedIn) }
    var setupDone by remember { mutableStateOf(prefs.setupDone) }
    var editing by remember { mutableStateOf(false) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { fa ->
            val user = fa.currentUser
            if (user != null) {
                prefs.adoptUser(user.uid)
                prefs.signedIn = true
                setupDone = prefs.setupDone
                Scheduler.scheduleNext(ctx)
            } else {
                prefs.signedIn = false
                Scheduler.cancel(ctx)
                Notifier.dismiss(ctx)
                editing = false
            }
            loggedIn = user != null
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    // Σύνδεσμος email που ανοίχτηκε ενώ ο χρήστης είναι ήδη συνδεδεμένος: αγνοείται.
    LaunchedEffect(loggedIn, pendingLink) {
        if (loggedIn && pendingLink != null) onLinkConsumed()
    }

    val mood = when {
        !loggedIn -> Mood.Welcome
        !setupDone || editing -> Mood.Settings
        else -> Mood.Today
    }

    Crossfade(targetState = mood, animationSpec = tween(220), label = "screen") { current ->
        AppBackground(current) {
            when (current) {
                Mood.Welcome -> AuthScreen(pendingLink, onLinkConsumed)
                Mood.Settings -> SetupScreen(prefs, canCancel = setupDone) { saved ->
                    if (saved) {
                        setupDone = true
                        Scheduler.scheduleNext(ctx)
                    }
                    editing = false
                }
                Mood.Today -> HomeScreen(
                    prefs, tick,
                    onSettings = { editing = true },
                    onLogout = { scope.launch { AuthRepository.signOut(ctx) } }
                )
            }
        }
    }
}
