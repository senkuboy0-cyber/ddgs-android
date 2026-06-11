package com.ddgs.engines

import com.ddgs.HttpClient
import com.ddgs.models.TextResult

/**
 * Yandex search engine implementation
 */
class YandexEngine(httpClient: HttpClient) : BaseSearchEngine(httpClient) {
    override val name = "yandex"
    override val searchUrl = "https://yandex.com/search/"
    override val searchMethod = "GET"

    override suspend fun searchText(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        page: Int
    ): List<TextResult> {
        val params = mutableMapOf<String, String>(
            "text" to query,
            "p" to "${page - 1}"
        )

        // Safe search
        when (safesearch) {
            "on" -> params["family"] = "adult"
            "off" -> params["family"] = "all"
            else -> params["family"] = "medium"
        }

        val html = httpClient.get(searchUrl, params)
        return parseResults(html)
    }

    private fun parseResults(html: String): List<TextResult> {
        val results = mutableListOf<TextResult>()
        val document = parseHtml(html)

        document.select("li.serp-item").forEach { element ->
            val titleElement = element.selectFirst("h2")
                ?: element.selectFirst(".OrganicTitle-Link")
            val linkElement = element.selectFirst("a")
            val snippetElement = element.selectFirst("div.OrganicTextContentSpan")
                ?: element.selectFirst("p")

            val title = titleElement?.text() ?: ""
            val href = linkElement?.attr("href") ?: ""
            val body = snippetElement?.text() ?: ""

            if (title.isNotBlank() && href.isNotBlank()) {
                results.add(TextResult(
                    title = normalizeText(title),
                    href = normalizeUrl(href),
                    body = normalizeText(body)
                ))
            }
        }

        return results
    }
}
