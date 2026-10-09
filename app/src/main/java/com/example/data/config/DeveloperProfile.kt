package com.example.data.config

/**
 * Centralized Developer Profile Configuration.
 * Edit your personal or organization details here in one place.
 */
object DeveloperProfile {
    const val appName: String = "Regional Voice AI"
    const val appTagline: String = "Your regional AI assistant for voice, text, images, documents, and creative tasks."

    // Developer / Creator Information
    const val developerName: String = "SHAMEEM KM"
    const val instagramUsername: String = "@ime3m"
    const val instagramUrl: String = "https://www.instagram.com/ime3m/"
    const val supportEmail: String = "support@regionalvoiceai.app"

    // Legal & Policy URLs
    const val privacyPolicyUrl: String = "https://regionalvoiceai.app/privacy"
    const val termsUrl: String = "https://regionalvoiceai.app/terms"

    /**
     * Opens the developer's Instagram profile.
     * Attempts to launch via the native Instagram app first; falls back to the browser URL.
     */
    fun openInstagram(context: android.content.Context) {
        val cleanUsername = instagramUsername.removePrefix("@")
        val appUri = android.net.Uri.parse("http://instagram.com/_u/$cleanUsername")
        val appIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, appUri).apply {
            setPackage("com.instagram.android")
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(appIntent)
        } catch (_: Exception) {
            try {
                val webIntent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(instagramUrl)
                ).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (_: Exception) {
                android.widget.Toast.makeText(
                    context,
                    "Instagram: $instagramUrl",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
