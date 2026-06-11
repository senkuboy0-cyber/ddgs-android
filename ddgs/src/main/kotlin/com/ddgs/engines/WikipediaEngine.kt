package com.ddgs.engines

import com.ddgs.HttpClient
import com.ddgs.models.TextResult

/**
 * Wikipedia search engine implementation
 */
class WikipediaEngine(httpClient: HttpClient) : BaseSearchEngine(httpClient) {
    override val name = "wikipedia"
    override val searchUrl = "https://en.wikipedia.org/w/index.php"
    override val searchMethod = "GET"

    override suspend fun searchText(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        page: Int
    ): List<TextResult> {
        val params = mutableMapOf<String, String>(
            "search" to query,
            "offset" to "${(page - 1) * 10}"
        )

        val html = httpClient.get(searchUrl, params)
        return parseResults(html)
    }

    private fun parseResults(html: String): List<TextResult> {
        val results = mutableListOf<TextResult>()
        val document = parseHtml(html)

        document.select("li.mw-search-result").forEach { element ->
            val titleElement = element.selectFirst("a")
            val snippetElement = element.selectFirst("div.searchresult")

            val title = titleElement?.text() ?: ""
            val href = titleElement?.attr("href") ?: ""
            val body = snippetElement?.text() ?: ""

            if (title.isNotBlank() && href.isNotBlank()) {
                val fullUrl = if (href.startsWith("/")) {
                    "https://en.wikipedia.org$href"
                } else {
                    href
                }
                results.add(TextResult(
                    title = normalizeText(title),
                    href = normalizeUrl(fullUrl),
                    body = normalizeText(body)
                ))
            }
        }

        return results
    }
}
