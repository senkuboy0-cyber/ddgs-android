package com.ddgs.engines

import com.ddgs.HttpClient
import com.ddgs.models.ImageResult

/**
 * DuckDuckGo Images search engine implementation
 */
class DuckDuckGoImagesEngine(httpClient: HttpClient) : ImageSearchEngine(httpClient) {
    override val name = "duckduckgo_images"

    private val searchUrl = "https://duckduckgo.com/i.js"

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
            "s" to "${(page - 1) * 50}"
        )

        // Safe search
        when (safesearch) {
            "on" -> params["p"] = "1"
            "off" -> params["p"] = "-1"
            else -> params["p"] = "0"
        }

        // Size filter
        if (size != null) {
            params["iaf"] = "size:${size.lowercase()}"
        }

        // Color filter
        if (color != null) {
            params["iaf"] = (params["iaf"] ?: "") + ",color:${color.lowercase()}"
        }

        val html = httpClient.get(searchUrl, params)
        return parseResults(html)
    }

    private fun parseResults(html: String): List<ImageResult> {
        val results = mutableListOf<ImageResult>()

        try {
            val json = kotlinx.serialization.json.Json.decodeFromString<DuckDuckGoImagesResponse>(html)
            json.results?.forEach { item ->
                results.add(ImageResult(
                    title = item.title ?: "",
                    image = normalizeUrl(item.image ?: ""),
                    thumbnail = normalizeUrl(item.thumbnail ?: ""),
                    url = normalizeUrl(item.url ?: ""),
                    height = item.height?.toString() ?: "",
                    width = item.width?.toString() ?: "",
                    source = "DuckDuckGo"
                ))
            }
        } catch (e: Exception) {
            // Fallback to HTML parsing
            val document = org.jsoup.Jsoup.parse(html)
            document.select("div.image").forEach { element ->
                val imgElement = element.selectFirst("img")
                val linkElement = element.selectFirst("a")

                results.add(ImageResult(
                    title = imgElement?.attr("alt") ?: "",
                    image = normalizeUrl(imgElement?.attr("src") ?: ""),
                    thumbnail = normalizeUrl(imgElement?.attr("src") ?: ""),
                    url = normalizeUrl(linkElement?.attr("href") ?: ""),
                    source = "DuckDuckGo"
                ))
            }
        }

        return results
    }

    private fun normalizeUrl(url: String): String = url.trim().replace(Regex("^//"), "https://")
}

@kotlinx.serialization.Serializable
data class DuckDuckGoImagesResponse(
    val results: List<DuckDuckGoImageItem>? = null
)

@kotlinx.serialization.Serializable
data class DuckDuckGoImageItem(
    val title: String? = null,
    val image: String? = null,
    val thumbnail: String? = null,
    val url: String? = null,
    val height: Int? = null,
    val width: Int? = null
)
