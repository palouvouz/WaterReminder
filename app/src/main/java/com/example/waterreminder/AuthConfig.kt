package com.example.waterreminder

/** Σταθερές του Firebase project. Δεν περιέχουν μυστικά (τα κλειδιά είναι στο google-services.json). */
object AuthConfig {
    const val PROJECT_ID = "waterreminder-palou"

    /** Προεπιλεγμένο Firebase Hosting domain, από το οποίο ανοίγουν οι σύνδεσμοι email. */
    const val LINK_HOST = "$PROJECT_ID.firebaseapp.com"
    const val LINK_PATH_PREFIX = "/__/auth/links"

    /** Σελίδα στην οποία οδηγείται ο χρήστης αν η εφαρμογή δεν είναι εγκατεστημένη. */
    const val CONTINUE_URL = "https://$LINK_HOST/finishSignIn"
    const val MIN_APP_VERSION = "1"
}
