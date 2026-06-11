package com.ddgs.engines

import com.ddgs.HttpClient
import com.ddgs.models.TextResult

/**
 * Brave search engine implementation
 */
class BraveEngine(httpClient: HttpClient) : BaseSearchEngine(httpClient) {
    override val name = "brave"
    override val searchUrl = "https://search.brave.com/search"
    override val searchMethod = "GET"

    override suspend fun searchText(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        page: Int
    ): List<TextResult> {
        val params = mutableMapOf<String, String>(
            "q" to query,
            "offset" to "${(page - 1) * 10}"
        )

        // Safe search
        when (safesearch) {
            "on" -> params["safesearch"] = "strict"
            "off" -> params["safesearch"] = "off"
            else -> params["safesearch"] = "moderate"
        }

        val html = httpClient.get(searchUrl, params)
        return parseResults(html)
    }

    private fun parseResults(html: String): List<TextResult> {
        val results = mutableListOf<TextResult>()
        val document = parseHtml(html)

        document.select("div.snippet").forEach { element ->
            val titleElement = element.selectFirst("a.title")
            val snippetElement = element.selectFirst("div.description")

            val title = titleElement?.text() ?: ""
            val href = titleElement?.attr("href") ?: ""
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
