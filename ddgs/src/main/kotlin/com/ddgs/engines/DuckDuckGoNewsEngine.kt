package com.ddgs.engines

import com.ddgs.HttpClient
import com.ddgs.models.NewsResult

/**
 * DuckDuckGo News search engine implementation
 */
class DuckDuckGoNewsEngine(httpClient: HttpClient) : NewsSearchEngine(httpClient) {
    override val name = "duckduckgo_news"

    private val searchUrl = "https://duckduckgo.com/news.js"

    override suspend fun searchNews(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        page: Int
    ): List<NewsResult> {
        val params = mutableMapOf<String, String>(
            "q" to query,
            "s" to "${(page - 1) * 30}"
        )

        val html = httpClient.get(searchUrl, params)
        return parseResults(html)
    }

    private fun parseResults(html: String): List<NewsResult> {
        val results = mutableListOf<NewsResult>()

        try {
            val json = kotlinx.serialization.json.Json.decodeFromString<DuckDuckGoNewsResponse>(html)
            json.results?.forEach { item ->
                results.add(NewsResult(
                    date = item.date ?: "",
                    title = item.title ?: "",
                    body = item.excerpt ?: "",
                    url = normalizeUrl(item.url ?: ""),
                    image = normalizeUrl(item.image ?: ""),
                    source = item.source ?: ""
                ))
            }
        } catch (e: Exception) {
            // Fallback to HTML parsing
            val document = org.jsoup.Jsoup.parse(html)
            document.select("div.news-item").forEach { element ->
                val titleElement = element.selectFirst("a")
                val snippetElement = element.selectFirst("p")
                val sourceElement = element.selectFirst("span.source")

                results.add(NewsResult(
                    title = titleElement?.text() ?: "",
                    body = snippetElement?.text() ?: "",
                    url = normalizeUrl(titleElement?.attr("href") ?: ""),
                    source = sourceElement?.text() ?: ""
                ))
            }
        }

        return results
    }

    private fun normalizeUrl(url: String): String = url.trim().replace(Regex("^//"), "https://")
}

@kotlinx.serialization.Serializable
data class DuckDuckGoNewsResponse(
    val results: List<DuckDuckGoNewsItem>? = null
)

@kotlinx.serialization.Serializable
data class DuckDuckGoNewsItem(
    val title: String? = null,
    val url: String? = null,
    val excerpt: String? = null,
    val image: String? = null,
    val source: String? = null,
    val date: String? = null
)
