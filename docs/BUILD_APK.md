# Δημιουργία APK

## 1. Προετοιμασία
- Android Studio Ladybug (2024.2.1) ή νεότερο, με JDK 17 (το περιλαμβάνει το Android Studio).
- Από το SDK Manager: Android SDK 35, **NDK (Side by side)** και **CMake 3.22.1**.
- Το αρχείο `app/google-services.json` από το Firebase Console (βλ. README, «Γρήγορη εκκίνηση»). Χωρίς αυτό το build αποτυγχάνει. Μην δημιουργήσεις δικό σου αρχείο.

## Εκδόσεις εργαλείων
| Εργαλείο | Έκδοση |
|---|---|
| Firebase BoM | 34.19.0 |
| Google Services plugin | 4.5.0 |
| Android Gradle Plugin | 8.7.3 |
| Gradle | 8.9 |
| Kotlin (και Compose compiler plugin) | 2.1.21 |
| compileSdk / targetSdk / minSdk | 35 / 34 / 26 |

Το Firebase BoM 34 απαιτεί νεότερα εργαλεία από την προηγούμενη έκδοση του project (AGP 8.5, Kotlin 1.9), γι' αυτό αναβαθμίστηκαν. Οι εκδόσεις αυτές δεν έχουν δοκιμαστεί με πραγματικό build.

## Ρύθμιση Google Sign-In
1. Στο Firebase Console ενεργοποίησε τον πάροχο *Google* (Authentication → Sign-in method).
2. Πρόσθεσε στο Android app τα SHA-1 και SHA-256 (`signingReport`). Χρειάζεται ξεχωριστό ζεύγος για το debug και για το release keystore.
3. Κατέβασε ξανά το `google-services.json`. Πρέπει να περιέχει web client (`client_type: 3`), από τον οποίο δημιουργείται το `default_web_client_id`.
4. Το κουμπί «Συνέχεια με Google» χρειάζεται συσκευή ή emulator με Google Play και τουλάχιστον έναν λογαριασμό Google.

## Ρύθμιση σύνδεσης με σύνδεσμο email
1. Ενεργοποίησε το *Email link (passwordless sign-in)* μέσα στον πάροχο Email/Password.
2. Ο σύνδεσμος ανοίγει στη διεύθυνση `waterreminder-palou.firebaseapp.com`. Για να ανοίξει απευθείας στην εφαρμογή, πρέπει να έχουν προστεθεί τα SHA-256 στο Firebase (βλ. παραπάνω).
3. Τα Firebase Dynamic Links δεν χρησιμοποιούνται (έχουν καταργηθεί).

## 2. Άνοιγμα και Sync
1. File → Open → διάλεξε τον φάκελο `WaterReminder`.
2. Περίμενε το Gradle Sync. Το πρώτο sync κατεβάζει εξαρτήσεις και μπορεί να κρατήσει μερικά λεπτά.
3. Το `gradle-wrapper.jar` δεν περιλαμβάνεται (βλ. `gradle/wrapper-note.txt`). Το Android Studio το δημιουργεί αυτόματα.

## 3. Debug APK (για δική σου χρήση)
Build → Build Bundle(s) / APK(s) → Build APK(s).
Το αρχείο βρίσκεται στο `app/build/outputs/apk/debug/app-debug.apk`.

## 4. Signed release APK (για το GitHub release)
1. Build → Generate Signed Bundle / APK → APK.
2. Create new keystore: διάλεξε αρχείο, κωδικούς και alias. **Κράτα το keystore και τους κωδικούς σε ασφαλές μέρος, εκτός repository.** Αν το χάσεις, δεν μπορείς να δώσεις ενημερώσεις με την ίδια υπογραφή.
3. Επίλεξε το variant `release` και τελείωσε τον οδηγό.
4. Το αρχείο βρίσκεται στον φάκελο που επέλεξες στον οδηγό (συνήθως `app/release/app-release.apk`).

Το επόμενο βήμα είναι το release στο GitHub, όπως περιγράφεται στο README.

## 5. Εγκατάσταση στο κινητό
- **Από το Android Studio:** ενεργοποίησε Developer options και USB debugging, σύνδεσε το κινητό και πάτα Run.
- **Χειροκίνητα:** στείλε το APK στο κινητό, άνοιξέ το και επίτρεψε την εγκατάσταση από άγνωστη πηγή.
- **Με adb:** `adb install -r app-debug.apk`

## 6. Από τη γραμμή εντολών
Μετά το πρώτο άνοιγμα στο Android Studio (που δημιουργεί το wrapper):
```
./gradlew assembleDebug
./gradlew assembleRelease
```
Το `assembleRelease` χωρίς ρυθμισμένο signing δίνει ανυπόγραφο APK, που δεν εγκαθίσταται. Για εγκατάσταση χρησιμοποίησε το debug APK ή τον οδηγό του βήματος 4.

## Συχνά σφάλματα build
| Σφάλμα | Λύση |
|---|---|
| `File google-services.json is missing` | Βάλε το αρχείο στον φάκελο `app/` |
| `No version of NDK matched` / `CMake not found` | Εγκατάστησε NDK και CMake 3.22.1 από το SDK Manager |
| `Unsupported Java version` | Άλλαξε το Gradle JDK σε 17 (Settings → Build Tools → Gradle) |
| `Minimum supported Gradle version is 8.9` | Άφησε το Android Studio να χρησιμοποιήσει το wrapper του project |
| `Plugin with id 'com.google.gms.google-services' ... google-services.json` | Λείπει το `app/google-services.json` |
| Σφάλμα για Kotlin ή Compose compiler | Έλεγξε ότι χρησιμοποιείς Kotlin 2.1.21 με το plugin `org.jetbrains.kotlin.plugin.compose` |
| Αποτυχία λήψης εξαρτήσεων | Έλεγξε σύνδεση στο internet ή proxy |
