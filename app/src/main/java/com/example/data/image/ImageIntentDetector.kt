package com.example.data.image

/**
 * Intelligent Image Intent Detector.
 *
 * Detects whether a user prompt is asking for image generation,
 * image editing, or image analysis.
 *
 * Supports English, Malayalam script, and Manglish / Malayalam-English mixed queries.
 */
object ImageIntentDetector {

    private val imageGenerationKeywords = listOf(
        "generate image", "generate an image", "create image", "create an image",
        "make an image", "make a picture", "create a picture", "generate picture",
        "draw an image", "draw a picture", "paint a picture", "make a photo",
        "create a photo", "generate a photo", "create a poster", "make a poster",
        "cinematic image", "realistic image", "cartoon-style image", "cartoon image",
        "cinematic picture", "realistic picture", "image of", "picture of", "photo of"
    )

    private val malayalamImageKeywords = listOf(
        "ചിത്രം ഉണ്ടാക്കൂ", "ചിത്രം ഉണ്ടാക്കുക", "ചിത്രം വരയ്ക്കൂ", "ചിത്രം കാണിക്കൂ",
        "ഒരു ചിത്രം", "ഫോട്ടോ ഉണ്ടാക്കൂ", "ഇമേജ് ഉണ്ടാക്കൂ", "ചിത്രം നിർമ്മിക്കൂ",
        "ചിത്രം താ", "ചിത്രം വേണം", "ഇമേജ് ഉണ്ടാക്കുക"
    )

    private val manglishImageKeywords = listOf(
        "image undakku", "picture undakku", "photo undakku",
        "image generate cheyyu", "image undakkamo", "picture undakkamo",
        "photo undakkamo", "image undakkanam", "oru picture", "oru image"
    )

    private val imageEditKeywords = listOf(
        "make this", "transform this", "edit this", "change background",
        "remove background", "turn this into", "watercolor", "make it cinematic",
        "make it look like", "create a different version"
    )

    /**
     * Returns true if the query is asking to generate an image from text.
     */
    fun isImageGenerationRequest(query: String): Boolean {
        val trimmed = query.trim().lowercase()
        if (trimmed.isBlank()) return false

        // Check explicit starters
        if (trimmed.startsWith("generate ") || trimmed.startsWith("create ") ||
            trimmed.startsWith("make a ") || trimmed.startsWith("draw ") ||
            trimmed.startsWith("paint ")
        ) {
            if (trimmed.contains("image") || trimmed.contains("picture") ||
                trimmed.contains("photo") || trimmed.contains("poster") ||
                trimmed.contains("illustration") || trimmed.contains("wallpaper") ||
                trimmed.contains("painting") || trimmed.contains("portrait") ||
                trimmed.contains("artwork")
            ) {
                return true
            }
        }

        for (kw in imageGenerationKeywords) {
            if (trimmed.contains(kw)) return true
        }

        for (kw in malayalamImageKeywords) {
            if (query.contains(kw)) return true
        }

        for (kw in manglishImageKeywords) {
            if (trimmed.contains(kw)) return true
        }

        return false
    }

    /**
     * Returns true if user has attached an image and is asking to edit/transform it.
     */
    fun isImageEditRequest(query: String, hasAttachedImage: Boolean): Boolean {
        if (!hasAttachedImage) return false
        val lower = query.trim().lowercase()
        return imageEditKeywords.any { lower.contains(it) } ||
                lower.contains("transform") || lower.contains("restyle") ||
                lower.contains("cinematic") || lower.contains("watercolor") ||
                lower.contains("background") || lower.contains("version")
    }

    /**
     * Cleans up conversational prefixes from prompt to optimize for image generation models.
     * E.g.: "Please create a realistic image of a Kerala village at sunset" -> "A realistic Kerala village at sunset"
     */
    fun extractImagePrompt(query: String): String {
        var clean = query.trim()
        val removePrefixes = listOf(
            "please generate an image of", "please create an image of", "generate an image of",
            "create an image of", "make a picture of", "create a picture of", "generate a picture of",
            "make an image of", "draw an image of", "generate image of", "create image of",
            "generate image", "create image", "make picture", "draw picture", "paint a picture of",
            "create a realistic image of", "make a realistic picture of", "make a realistic image of",
            "create a realistic photo of", "generate a cartoon-style", "create a cinematic poster for",
            "ഒരു ചിത്രം ഉണ്ടാക്കൂ:", "ഒരു ചിത്രം ഉണ്ടാക്കൂ", "ചിത്രം ഉണ്ടാക്കൂ", "ഇമേജ് ഉണ്ടാക്കൂ"
        )

        val lower = clean.lowercase()
        for (prefix in removePrefixes) {
            if (lower.startsWith(prefix)) {
                clean = clean.substring(prefix.length).trim(' ', ':', ',', '.', '-')
                break
            }
        }

        if (clean.isBlank()) return query
        return clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
