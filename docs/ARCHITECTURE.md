# Αρχιτεκτονική

## Γλώσσες και αρχεία
Κάθε αρχείο περιέχει μία γλώσσα.

| Γλώσσα | Αρχεία | Ρόλος |
|---|---|---|
| Kotlin | `app/src/main/java/com/example/waterreminder/*.kt` | Οθόνες, θέμα, δεδομένα, ειδοποιήσεις, χρονοδιάγραμμα, σύνδεση χρήστη |
| C++ | `cpp/schedule.h`, `schedule.cpp`, `native_bridge.cpp` | Υπολογισμός ωρών υπενθύμισης και σύνδεση JNI |
| JavaScript | `assets/progress.js` | Ζωγραφίζει τον κυκλικό δείκτη προόδου |
| HTML / CSS | `assets/progress.html`, `progress.css` | Δομή και εμφάνιση του δείκτη |

## Επίπεδα
```
UI (Compose)        MainActivity, AuthScreen, SetupScreen, HomeScreen, Components, AuthComponents, Background, Theme
      │
Λογική / Δεδομένα   Prefs, Scheduler, Notifier, Format
Σύνδεση             AuthRepository, GoogleSignInHelper, AuthStore, PasswordPolicy, AuthErrors, AuthConfig
      │
Αποδέκτες           ReminderReceiver, ActionReceiver, SystemEventReceiver
      │
Native (C++)        schedule.cpp  ← native_bridge.cpp (JNI)  ← Native.kt
Web                 progress.html + .css + .js  ← ProgressView.kt (WebView)
Εξωτερικά           Firebase Authentication, AlarmManager, NotificationManager
```

## Οθόνες και ροή
Το `App()` στο `MainActivity.kt` διαλέγει οθόνη με βάση την κατάσταση, με Crossfade 220 ms:
1. Δεν υπάρχει σύνδεση → `AuthScreen` (φόντο «Welcome»).
2. Υπάρχει σύνδεση αλλά δεν έχουν οριστεί ρυθμίσεις, ή ο χρήστης πάτησε «Ρυθμίσεις» → `SetupScreen` (φόντο «Settings»).
3. Αλλιώς → `HomeScreen` (φόντο «Today»).

Την εναλλαγή μεταξύ οθονών την ορίζει ένας `FirebaseAuth.AuthStateListener` στο `App()`. Μετά από κάθε σύνδεση καλεί το `Prefs.adoptUser(uid)`, που δένει τις τοπικές ρυθμίσεις με τον λογαριασμό. Αν είναι άλλος λογαριασμός από τον προηγούμενο στη συσκευή, οι ρυθμίσεις σβήνονται. Όταν ο χρήστης αποσυνδεθεί, ακυρώνονται οι alarms και η ειδοποίηση.

## Σύνδεση χρήστη (Firebase Authentication)
| Λειτουργία | Υλοποίηση |
|---|---|
| Email και κωδικός | `signInWithEmailAndPassword`, `createUserWithEmailAndPassword` (+ `sendEmailVerification`) |
| Επαναφορά κωδικού | `sendPasswordResetEmail`. Ίδιο μήνυμα για υπαρκτό και ανύπαρκτο email |
| Google | Credential Manager (`GetGoogleIdOption`) → `GoogleAuthProvider.getCredential` → `signInWithCredential`. Το web client id προέρχεται από το `default_web_client_id` του `google-services.json` |
| Σύνδεσμος email | `sendSignInLinkToEmail` με `ActionCodeSettings` (Hosting domain του project, `handleCodeInApp`). Το `MainActivity` δέχεται μόνο `https` σύνδεσμο του δικού μας domain και τον ελέγχει με `isSignInWithEmailLink`. Ολοκλήρωση με `signInWithEmailLink` |
| Αποσύνδεση | `signOut` και `clearCredentialState` |
| Παραμονή σύνδεσης | Το Firebase SDK κρατά τη συνεδρία. Η εφαρμογή δεν την αποθηκεύει |

Ο σύνδεσμος email ολοκληρώνεται αυτόματα όταν ανοίγει στη συσκευή που τον ζήτησε (το email μένει προσωρινά στο `AuthStore`, όπως απαιτεί το Firebase). Αλλιώς ζητείται το email από τον χρήστη.

## Ασφάλεια σύνδεσης
| Θέμα | Τι ισχύει στην εφαρμογή |
|---|---|
| **Session token (αποθήκευση στο LocalStorage)** | Δεν ισχύει όπως στο web: η εφαρμογή είναι native και δεν χρησιμοποιεί LocalStorage για σύνδεση. Δεν διαβάζει ούτε αποθηκεύει ID tokens ή refresh tokens. Το Firebase SDK κρατά τη συνεδρία στον ιδιωτικό χώρο της εφαρμογής. Το `allowBackup` είναι απενεργοποιημένο, οπότε δεν γίνεται αντίγραφο στο cloud. Το WebView του δείκτη προόδου φορτώνει μόνο τοπικά αρχεία και δεν έχει πρόσβαση σε δεδομένα σύνδεσης |
| **Έλεγχοι διαχειριστή στον πελάτη** | Η εφαρμογή δεν έχει λειτουργίες διαχειριστή και δεν κάνει τέτοιους ελέγχους. Αν προστεθούν, πρέπει να βασίζονται σε custom claims που ελέγχονται από Security Rules ή server |
| **2FA / OTP** | **Δεν έχει υλοποιηθεί.** Η πολυπαραγοντική πιστοποίηση του Firebase (SMS ή TOTP) απαιτεί αναβάθμιση του project σε Identity Platform, με χρέωση πέρα από το δωρεάν όριο, και επαληθευμένο email. Η σύνδεση με Google κληρονομεί την επαλήθευση δύο βημάτων του λογαριασμού Google. Αν θες 2FA, είναι ξεχωριστό βήμα που χρειάζεται δική σου απόφαση για το κόστος |
| **Rate limiting** | Ο κύριος περιορισμός γίνεται από τους servers του Firebase (σφάλμα `too-many-requests`, που η εφαρμογή χειρίζεται με κλείδωμα 5 λεπτών). Προστίθεται κλείδωμα στον πελάτη: 5 δωρεάν προσπάθειες, μετά 30 s που διπλασιάζονται έως 15 λεπτά, και αναμονή 60 s μεταξύ αποστολών email. Το τοπικό κλείδωμα παρακάμπτεται με εκκαθάριση δεδομένων ή αλλαγή ώρας, γι' αυτό δεν είναι ουσιαστική προστασία. Προτείνεται Firebase App Check (Play Integrity), που ρυθμίζεται από το Console |
| **Ισχύς κωδικού** | Στον πελάτη: τουλάχιστον 10 χαρακτήρες, 3 από 4 κατηγορίες, όχι συνηθισμένος κωδικός, χωρίς το όνομα του email. Επιβολή στον server: *Authentication → Settings → Password policy* στο Console. Ο έλεγχος στον πελάτη δεν αρκεί από μόνος του |
| **Εντοπισμός λογαριασμών** | Το μήνυμα για λάθος email ή κωδικό είναι κοινό και η επαναφορά κωδικού απαντά πάντα το ίδιο. Η προστασία email enumeration του Firebase πρέπει να μείνει ενεργή |

## Ροή μίας υπενθύμισης
1. `Scheduler.scheduleNext()` βρίσκει την επόμενη ώρα (`nextTrigger`) και θέτει alarm στο AlarmManager.
2. Ο alarm ενεργοποιεί το `ReminderReceiver`. Αν ο χρήστης έχει αποσυνδεθεί (τοπική σημαία `Prefs.signedIn`, που ορίζει ο `AuthStateListener`), δεν γίνεται τίποτα.
3. Αν ο στόχος δεν έχει συμπληρωθεί, το `Notifier.show()` εμφανίζει την ειδοποίηση με «Ήπια» και «Skip».
4. Ο αποδέκτης καλεί ξανά `scheduleNext()` για την επόμενη υπενθύμιση.
5. Οι ενέργειες πηγαίνουν στο `ActionReceiver`, που ενημερώνει τα `Prefs`.

## Υπολογισμός ωρών (C++)
`water::computeSlots(start, end, schoolStart, schoolEnd, useSchool, count)` στο `schedule.cpp`:
- Φτιάχνει τη λίστα των ελεύθερων λεπτών (αφαιρώντας το σχολείο όταν χρειάζεται).
- Επιλέγει `count` λεπτά στο μέσο ίσων τμημάτων αυτής της λίστας.
- `count = ceil(στόχος / 250 ml)`.

Το `native_bridge.cpp` είναι ο μόνος κώδικας που γνωρίζει το JNI. Η λογική δοκιμάζεται χωριστά (`native-tests/schedule_test.cpp`).

Η `Scheduler.slotsFor()` δίνει `useSchool = μαθητής && Δευτέρα–Παρασκευή`. Αν δεν υπάρχει άλλη υπενθύμιση σήμερα, ψάχνει στις επόμενες 7 ημέρες.

## Ακρίβεια χρόνου
- Με την άδεια `SCHEDULE_EXACT_ALARM`: `setExactAndAllowWhileIdle`.
- Χωρίς αυτήν (Android 12+): `setAndAllowWhileIdle`, που μπορεί να καθυστερεί λίγο.
- Ένας μόνο alarm είναι ενεργός κάθε στιγμή (ίδιο `PendingIntent`).
- Το `SystemEventReceiver` ξαναβάζει τον alarm μετά από επανεκκίνηση, ενημέρωση της εφαρμογής, αλλαγή ώρας και αλλαγή ζώνης ώρας.

## Δείκτης προόδου (HTML, CSS, JavaScript)
Το `ProgressView.kt` φορτώνει `file:///android_asset/progress.html#drunk=…&goal=…`. Το Kotlin αλλάζει μόνο το URL. Το `progress.js` διαβάζει το hash και ζωγραφίζει τον κύκλο, οπότε δεν υπάρχει JavaScript μέσα σε αρχεία Kotlin.

Ασφάλεια του WebView: απενεργοποιημένη η πρόσβαση σε αρχεία και content providers, και επιτρέπονται μόνο σελίδες από τα assets της εφαρμογής. Δεν φορτώνεται τίποτα από το internet.

## Κανάλια ειδοποιήσεων
Τα κανάλια στο Android δεν αλλάζουν μετά τη δημιουργία, γι' αυτό υπάρχουν τρία: `water_vibrate`, `water_sound`, `water_push`. Η επιλογή του χρήστη διαλέγει ποιο θα χρησιμοποιηθεί.

## Δεδομένα
Στα `Prefs` (SharedPreferences) αποθηκεύονται οι ρυθμίσεις και τα ημερήσια στοιχεία (`drunkMl`, `skipsUsed`). Η `ensureToday()` τα μηδενίζει όταν αλλάξει η ημερομηνία. Το `allowBackup` είναι απενεργοποιημένο, οπότε δεν γίνεται αντίγραφο στο cloud.

## Σχεδιασμός (design system)
Το στυλ είναι εμπνευσμένο από τη Revolut, χωρίς λογότυπα ή assets της.
- **Χρώματα** (`Theme.kt`): σκούρο φόντο `#0A0A0F`, λευκό κείμενο, γκρι δευτερεύον κείμενο, μπλε έμφαση `#0666EB` και teal `#00B3A4` για τις ρυθμίσεις.
- **Φόντα** (`Background.kt`): κάθε οθόνη έχει δικό της φόντο, δηλαδή κάθετη διαβάθμιση και απαλές ακτινωτές λάμψεις σε διαφορετικές θέσεις και χρώματα.
- **Glass** (`Modifier.glass`): ημιδιάφανη διαβάθμιση και λεπτό φωτεινό περίγραμμα. Το Compose δεν θολώνει το φόντο πίσω από ένα στοιχείο, γι' αυτό το blur προσομοιώνεται.
- **Τυπογραφία:** μεγάλοι έντονοι αριθμοί και τίτλοι με αρνητικό letter-spacing.
- **Κίνηση:** Crossfade μεταξύ οθονών και ήπιο transition στον δείκτη προόδου.
- **Σχήματα:** κάρτες με γωνίες 24dp, κουμπιά σε σχήμα «χάπι».

## Ιδέες για επέκταση
- Συγχρονισμός ρυθμίσεων και ιστορικού στο Firestore.
- Ιστορικό ημερών και στατιστικά.
- Μεταφορά του skip σε επόμενη υπενθύμιση.
- Ρυθμιζόμενο μέγεθος ποτηριού.
