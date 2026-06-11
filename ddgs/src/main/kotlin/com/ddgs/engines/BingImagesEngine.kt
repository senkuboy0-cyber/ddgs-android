package com.ddgs.engines

import com.ddgs.HttpClient
import com.ddgs.models.ImageResult

/**
 * Bing Images search engine implementation
 */
class BingImagesEngine(httpClient: HttpClient) : ImageSearchEngine(httpClient) {
    override val name = "bing_images"

    private val searchUrl = "https://www.bing.com/images/search"

    override suspend fun searchImages(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        page: Int,
        size: String?,
        color: String?,
        typeImage: String?,
        layout: String?,
        licenseImage: String?
    ): List<ImageResult> {
        val params = mutableMapOf<String, String>(
            "q" to query,
            "first" to "${(page - 1) * 35 + 1}"
        )

        // Safe search
        when (safesearch) {
            "on" -> params["adlt"] = "strict"
            "off" -> params["adlt"] = "off"
            else -> params["adlt"] = "moderate"
        }

        // Size filter
        if (size != null) {
            params["size"] = when (size.lowercase()) {
                "small" -> "Small"
                "medium" -> "Medium"
                "large" -> "Large"
                "wallpaper" -> "Wallpaper"
                else -> "Medium"
            }
        }

        // Color filter
        if (color != null) {
            params["color"] = color
        }

        // Type filter
        if (typeImage != null) {
            params["contenttype"] = when (typeImage.lowercase()) {
                "photo" -> "photo"
                "clipart" -> "clipart"
                "gif" -> "animatedgif"
                "transparent" -> "transparent"
                "line" -> "linedrawing"
                else -> "photo"
            }
        }

        // Layout filter
        if (layout != null) {
            params["layout"] = when (layout.lowercase()) {
                "square" -> "Square"
                "tall" -> "Tall"
                "wide" -> "Wide"
                else -> ""
            }
        }

        val html = httpClient.get(searchUrl, params)
        return parseResults(html)
    }

    private fun parseResults(html: String): List<ImageResult> {
        val results = mutableListOf<ImageResult>()
        val document = org.jsoup.Jsoup.parse(html)

        document.select("div.imgpt").forEach { element ->
            val title = element.selectFirst("div.title")?.text() ?: ""
            val imageElement = element.selectFirst("img")
            val imageUrl = imageElement?.attr("src") ?: ""
            val thumbnailUrl = imageElement?.attr("data-src") ?: imageUrl
            val linkElement = element.selectFirst("a.iusc")
            val pageUrl = linkElement?.attr("href") ?: ""

            val height = imageElement?.attr("height") ?: ""
            val width = imageElement?.attr("width") ?: ""

            if (imageUrl.isNotBlank()) {
                results.add(ImageResult(
                    title = normalizeText(title),
                    image = normalizeUrl(imageUrl),
                    thumbnail = normalizeUrl(thumbnailUrl),
                    url = normalizeUrl(pageUrl),
                    height = height,
                    width = width,
                    source = "Bing"
                ))
            }
        }

        return results
    }

    private fun normalizeText(text: String): String = text.trim().replace(Regex("\\s+"), " ")
    private fun normalizeUrl(url: String): String = url.trim().replace(Regex("^//"), "https://")
}
