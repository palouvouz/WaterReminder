# Water Reminder (Android)

Εφαρμογή Android που υπενθυμίζει στον χρήστη να πίνει νερό, με βάση τον ημερήσιο στόχο του και τις ώρες που είναι διαθέσιμος.

> **Δημιουργία:** Η ιδέα είναι του δημιουργού της εφαρμογής. Το project έγινε *vibe coded* με την **Anthropic (Claude)** και το *debug* έγινε με την **OpenAI**.

## Χαρακτηριστικά
- Σύνδεση και εγγραφή με email και κωδικό, επαναφορά κωδικού με email, σύνδεση με Google, σύνδεση χωρίς κωδικό (σύνδεσμος στο email) και αποσύνδεση, όλα με Firebase Authentication. Η σύνδεση μένει ενεργή μεταξύ εκκινήσεων.
- Έλεγχος ισχύος κωδικού, προσωρινό κλείδωμα μετά από αποτυχημένες προσπάθειες και αναμονή μεταξύ αποστολών email.
- Ημερήσιος στόχος σε ποτήρια (250 ml) ή λίτρα.
- Ώρα έναρξης και λήξης ειδοποιήσεων.
- Λειτουργία μαθητή: καμία ειδοποίηση στις σχολικές ώρες (προεπιλογή 08:15–14:14, Δευτέρα–Παρασκευή).
- Οι υπενθυμίσεις μοιράζονται ισόποσα στον διαθέσιμο χρόνο.
- Τρόπος ειδοποίησης: δόνηση, ήχος ή μόνο push.
- Κουμπιά «Ήπια» και «Skip» πάνω στην ειδοποίηση, με ημερήσιο όριο skip.
- Κυκλικός δείκτης προόδου της ημέρας.
- Οι ειδοποιήσεις σταματούν όταν συμπληρωθεί ο στόχος και επανέρχονται μετά από επανεκκίνηση, ενημέρωση ή αλλαγή ώρας.
- Σχεδιασμός στο στυλ της Revolut: σκούρα φόντα που αλλάζουν ανά οθόνη, κουμπιά και κάρτες με εφέ glass, μεγάλη τυπογραφία.

## Γρήγορη εκκίνηση
Το project χρησιμοποιεί το Firebase project `waterreminder-palou` και το package `com.example.waterreminder`.

1. **Firebase Console** → project `waterreminder-palou` → Project settings → Your apps. Αν δεν υπάρχει Android app, πρόσθεσέ το με package `com.example.waterreminder`.
2. Κατέβασε το `google-services.json` και βάλ' το στον φάκελο `app/`. Το αρχείο **δεν περιλαμβάνεται** στο ZIP και δεν ανεβαίνει στο GitHub (είναι στο `.gitignore`).
3. **Authentication → Sign-in method:** ενεργοποίησε *Email/Password* (και μέσα σε αυτό το *Email link (passwordless sign-in)*) και *Google*.
4. Πρόσθεσε τα **SHA-1 και SHA-256** του κλειδιού υπογραφής στο Android app (Android Studio: Gradle → `app` → Tasks → android → `signingReport`). Μετά **κατέβασε ξανά** το `google-services.json`. Χωρίς αυτό δεν δουλεύουν η σύνδεση με Google και το άνοιγμα των συνδέσμων email.
5. **Authentication → Settings → Password policy:** όρισε ελάχιστο μήκος 10 και απαιτήσεις χαρακτήρων. Άφησε ενεργή την προστασία από εντοπισμό email (email enumeration protection).
6. Άνοιξε το project στο Android Studio (Ladybug 2024.2.1 ή νεότερο) και κάνε Gradle Sync.
7. Run σε συσκευή ή emulator με Google Play (Android 8.0+).

Λεπτομέρειες στο [docs/BUILD_APK.md](docs/BUILD_APK.md).

## Ανέβασμα στο GitHub (κώδικας και release στο ίδιο repository)

### Α. Ο κώδικας
1. Στο github.com: **New repository** → όνομα π.χ. `water-reminder` → δεν βάζεις README ή .gitignore (υπάρχουν ήδη) → **Create repository**.
2. Εγκατάστησε το [Git](https://git-scm.com/downloads) αν δεν το έχεις.
3. Άνοιξε τερματικό μέσα στον φάκελο `WaterReminder` και τρέξε (άλλαξε το `ΟΝΟΜΑ` με το όνομά σου στο GitHub):
   ```
   git init
   git add .
   git commit -m "Αρχική έκδοση"
   git branch -M main
   git remote add origin https://github.com/ΟΝΟΜΑ/water-reminder.git
   git push -u origin main
   ```
4. Το `.gitignore` εξαιρεί το `google-services.json` και τα keystore (`*.jks`, `*.keystore`). **Μην τα ανεβάσεις ποτέ** στο repository.

### Β. Το release (στο ίδιο repository)
1. Φτιάξε το APK ακολουθώντας το [docs/BUILD_APK.md](docs/BUILD_APK.md) (προτίμησε το signed release).
2. Στο repository: **Releases** → **Draft a new release**.
3. **Choose a tag** → γράψε `v1.0.0` → **Create new tag on publish**.
4. Τίτλος `Water Reminder 1.0.0` και μια σύντομη περιγραφή αλλαγών.
5. Σύρε το αρχείο `.apk` στο πλαίσιο **Attach binaries**.
6. **Publish release**.

Με το GitHub CLI (`gh`), τα βήματα Β.2–Β.6 γίνονται με μία εντολή:
```
gh release create v1.0.0 path/προς/το/app-release.apk --title "Water Reminder 1.0.0" --notes "Πρώτη έκδοση"
```

Για νέες εκδόσεις: άλλαξε τα `versionCode` και `versionName` στο `app/build.gradle.kts`, κάνε `git commit` και `git push`, και δημιούργησε νέο release με νέο tag (π.χ. `v1.0.1`).

## Βίντεο διαφήμισης
Το `marketing/water-reminder-ad.mp4` είναι βίντεο 16,5 δευτερολέπτων (1080×1920, κάθετο) με τίτλους που εμφανίζονται με εφέ bounce και ομαλές μεταβάσεις. Χρησιμοποιεί τα χρώματα της εφαρμογής: σκούρο φόντο, μπλε `#0666EB` και teal `#00B3A4`. Δεν έχει ήχο.

## Ιστοσελίδα της εφαρμογής
Ο φάκελος [website/](website/) έχει μια σελίδα που περιγράφει την εφαρμογή, για το site σου:
- `website/index.html`: μόνο δομή, χωρίς CSS ή JavaScript μέσα του.
- `website/css/style.css`: κενό. Το styling το φορτώνεις από το δικό σου repository με το `<link>` που υπάρχει σε σχόλιο στο `index.html`.
- `website/js/app.js`: μικρό script (γράφει το έτος στο υποσέλιδο).

## Τα υπόλοιπα έγγραφα
| Αρχείο | Περιεχόμενο |
|---|---|
| [docs/BUILD_APK.md](docs/BUILD_APK.md) | Πώς φτιάχνεις και εγκαθιστάς το APK |
| [docs/USER_GUIDE.md](docs/USER_GUIDE.md) | Οδηγός χρήσης για τον χρήστη |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Τεχνική περιγραφή και ροές |
| [docs/TESTING.md](docs/TESTING.md) | Τι ελέγχθηκε και λίστα δοκιμών σε συσκευή |
| [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) | Συχνά προβλήματα και λύσεις |

## Δομή φακέλων
```
WaterReminder/
├── README.md
├── docs/                              τεκμηρίωση (5 αρχεία)
├── gradle/                            wrapper properties και wrapper-note.txt
├── marketing/water-reminder-ad.mp4    βίντεο διαφήμισης
├── native-tests/schedule_test.cpp     δοκιμές C++ που τρέχουν στον υπολογιστή
├── website/                           σελίδα περιγραφής για το site (HTML, κενό CSS, JS)
└── app/
    ├── google-services.json           (το προσθέτεις εσύ, δεν ανεβαίνει στο GitHub)
    └── src/main/
        ├── java/com/example/waterreminder/   Kotlin: οθόνες, θέμα, ειδοποιήσεις, χρονοδιάγραμμα
        ├── cpp/                              C++: schedule.h/.cpp (λογική) και native_bridge.cpp (JNI)
        ├── assets/                           progress.html, progress.css, progress.js
        └── AndroidManifest.xml
```
Κάθε αρχείο περιέχει μία μόνο γλώσσα.

## Τεχνολογίες
Kotlin 2.1, Jetpack Compose (Material 3), C++ (NDK, CMake), JavaScript (WebView), Firebase Authentication (BoM 34.19.0), Credential Manager (Google), AlarmManager, SharedPreferences.

## Γνωστοί περιορισμοί
- Οι ρυθμίσεις αποθηκεύονται μόνο τοπικά και δεν συγχρονίζονται με τον λογαριασμό. Αν συνδεθεί άλλος λογαριασμός στην ίδια συσκευή, οι τοπικές ρυθμίσεις μηδενίζονται.
- Το skip δεν μεταφέρει το ποτήρι σε επόμενη υπενθύμιση.
- Το εφέ glass προσομοιώνεται με διαφάνεια και περίγραμμα, χωρίς πραγματικό blur του φόντου.
- Δεν υπάρχει σύνδεση δύο παραγόντων (2FA/OTP). Βλ. docs/ARCHITECTURE.md, ενότητα «Ασφάλεια σύνδεσης».
