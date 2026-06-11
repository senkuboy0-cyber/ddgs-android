package com.ddgs.engines

import com.ddgs.HttpClient
import com.ddgs.models.TextResult

/**
 * Bing search engine implementation
 */
class BingEngine(httpClient: HttpClient) : BaseSearchEngine(httpClient) {
    override val name = "bing"
    override val searchUrl = "https://www.bing.com/search"
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
                "y" -> "qdr:y"
                else -> "qdr:y"
            }
            params["tbs"] = tbsValue
        }

        val html = httpClient.get(searchUrl, params)
        return parseResults(html)
    }

    private fun parseResults(html: String): List<TextResult> {
        val results = mutableListOf<TextResult>()
        val document = parseHtml(html)

        document.select("li.b_algo").forEach { element ->
            val titleElement = element.selectFirst("h2")
            val linkElement = element.selectFirst("a")
            val snippetElement = element.selectFirst("p")

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
