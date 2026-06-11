package com.ddgs.engines

import com.ddgs.HttpClient
import com.ddgs.models.NewsResult

/**
 * Bing News search engine implementation
 */
class BingNewsEngine(httpClient: HttpClient) : NewsSearchEngine(httpClient) {
    override val name = "bing_news"

    private val searchUrl = "https://www.bing.com/news/search"

    override suspend fun searchNews(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        page: Int
    ): List<NewsResult> {
        val params = mutableMapOf<String, String>(
            "q" to query,
            "first" to "${(page - 1) * 10 + 1}"
        )

        // Safe search
        when (safesearch) {
            "on" -> params["adlt"] = "strict"
            "off" -> params["adlt"] = "off"
            else -> params["adlt"] = "moderate"
        }

        // Time limit
        if (timelimit != null) {
            val tbsValue = when (timelimit) {
                "d" -> "qdr:d"
                "w" -> "qdr:w"
                "m" -> "qdr:m"
                else -> "qdr:m"
            }
            params["tbs"] = tbsValue
        }

        val html = httpClient.get(searchUrl, params)
        return parseResults(html)
    }

    private fun parseResults(html: String): List<NewsResult> {
        val results = mutableListOf<NewsResult>()
        val document = org.jsoup.Jsoup.parse(html)

        document.select("div.news-card").forEach { element ->
            val titleElement = element.selectFirst("a.title")
            val snippetElement = element.selectFirst("div.snippet")
            val sourceElement = element.selectFirst("div.source")
            val dateElement = element.selectFirst("span.news-date")
            val imageElement = element.selectFirst("img")

            val title = titleElement?.text() ?: ""
            val url = titleElement?.attr("href") ?: ""
            val body = snippetElement?.text() ?: ""
            val source = sourceElement?.text() ?: ""
            val date = dateElement?.text() ?: ""
            val image = imageElement?.attr("src") ?: ""

            if (title.isNotBlank() && url.isNotBlank()) {
                results.add(NewsResult(
                    date = normalizeText(date),
                    title = normalizeText(title),
                    body = normalizeText(body),
                    url = normalizeUrl(url),
                    image = normalizeUrl(image),
                    source = normalizeText(source)
                ))
            }
        }

        return results
    }

    private fun normalizeText(text: String): String = text.trim().replace(Regex("\\s+"), " ")
    private fun normalizeUrl(url: String): String = url.trim().replace(Regex("^//"), "https://")
}
