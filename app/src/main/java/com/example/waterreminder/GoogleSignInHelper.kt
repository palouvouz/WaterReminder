package com.example.waterreminder

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

sealed interface GoogleOutcome {
    data class Token(val idToken: String) : GoogleOutcome
    data object Cancelled : GoogleOutcome
    data object NoAccount : GoogleOutcome
    data object NotConfigured : GoogleOutcome
    data object Failed : GoogleOutcome
}

/** Σύνδεση με Google μέσω Credential Manager (σύγχρονο API, αντί του παλιού GoogleSignInClient). */
object GoogleSignInHelper {

    /** `activityContext` πρέπει να είναι Activity, ώστε να εμφανιστεί το παράθυρο επιλογής λογαριασμού. */
    suspend fun requestIdToken(activityContext: Context): GoogleOutcome {
        // Το default_web_client_id δημιουργείται από το google-services plugin όταν το google-services.json
        // περιέχει web client, δηλαδή αφού ενεργοποιηθεί το Google και προστεθούν τα SHA-1/SHA-256.
        val resId = activityContext.resources.getIdentifier("default_web_client_id", "string", activityContext.packageName)
        if (resId == 0) return GoogleOutcome.NotConfigured

        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(activityContext.getString(resId))
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        return try {
            val credential = CredentialManager.create(activityContext).getCredential(activityContext, request).credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                GoogleOutcome.Token(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } else {
                GoogleOutcome.Failed
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleOutcome.Cancelled
        } catch (e: NoCredentialException) {
            GoogleOutcome.NoAccount
        } catch (e: GetCredentialException) {
            GoogleOutcome.Failed
        } catch (e: GoogleIdTokenParsingException) {
            GoogleOutcome.Failed
        }
    }
}
