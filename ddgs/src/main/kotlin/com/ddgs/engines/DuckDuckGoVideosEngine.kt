package com.ddgs.engines

import com.ddgs.HttpClient
import com.ddgs.models.VideoResult

/**
 * DuckDuckGo Videos search engine implementation
 */
class DuckDuckGoVideosEngine(httpClient: HttpClient) : VideoSearchEngine(httpClient) {
    override val name = "duckduckgo_videos"

    private val searchUrl = "https://duckduckgo.com/v.js"

    override suspend fun searchVideos(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        page: Int,
        resolution: String?,
        duration: String?,
        licenseVideos: String?
    ): List<VideoResult> {
        val params = mutableMapOf<String, String>(
            "q" to query,
            "s" to "${(page - 1) * 30}"
        )

        // Safe search
        when (safesearch) {
            "on" -> params["p"] = "1"
            "off" -> params["p"] = "-1"
            else -> params["p"] = "0"
        }

        // Time limit
        if (timelimit != null) {
            params["df"] = timelimit
        }

        val html = httpClient.get(searchUrl, params)
        return parseResults(html)
    }

    private fun parseResults(html: String): List<VideoResult> {
        val results = mutableListOf<VideoResult>()

        try {
            val json = kotlinx.serialization.json.Json.decodeFromString<DuckDuckGoVideosResponse>(html)
            json.results?.forEach { item ->
                results.add(VideoResult(
                    title = item.title ?: "",
                    content = normalizeUrl(item.content ?: ""),
                    description = item.description ?: "",
                    duration = item.duration ?: "",
                    embedUrl = normalizeUrl(item.embedUrl ?: ""),
                    images = mapOf(
                        "large" to normalizeUrl(item.images?.large ?: ""),
                        "medium" to normalizeUrl(item.images?.medium ?: ""),
                        "small" to normalizeUrl(item.images?.small ?: "")
                    ),
                    provider = item.provider ?: "",
                    published = item.published ?: "",
                    publisher = item.publisher ?: "",
                    uploader = item.uploader ?: "",
                    statistics = item.statistics ?: emptyMap()
                ))
            }
        } catch (e: Exception) {
            // Fallback to HTML parsing
            val document = org.jsoup.Jsoup.parse(html)
            document.select("div.video").forEach { element ->
                val titleElement = element.selectFirst("a.title")
                val descElement = element.selectFirst("p.description")
                val thumbElement = element.selectFirst("img")

                results.add(VideoResult(
                    title = titleElement?.text() ?: "",
                    content = normalizeUrl(titleElement?.attr("href") ?: ""),
                    description = descElement?.text() ?: "",
                    images = mapOf("medium" to normalizeUrl(thumbElement?.attr("src") ?: "")),
                    provider = "DuckDuckGo"
                ))
            }
        }

        return results
    }

    private fun normalizeUrl(url: String): String = url.trim().replace(Regex("^//"), "https://")
}

@kotlinx.serialization.Serializable
data class DuckDuckGoVideosResponse(
    val results: List<DuckDuckGoVideoItem>? = null
)

@kotlinx.serialization.Serializable
data class DuckDuckGoVideoItem(
    val title: String? = null,
    val content: String? = null,
    val description: String? = null,
    val duration: String? = null,
    val embedUrl: String? = null,
    val images: DuckDuckGoVideoImages? = null,
    val provider: String? = null,
    val published: String? = null,
    val publisher: String? = null,
    val uploader: String? = null,
    val statistics: Map<String, String> = emptyMap()
)

@kotlinx.serialization.Serializable
data class DuckDuckGoVideoImages(
    val large: String? = null,
    val medium: String? = null,
    val small: String? = null
)
