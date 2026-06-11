package com.ddgs.engines

import com.ddgs.HttpClient
import com.ddgs.models.TextResult

/**
 * DuckDuckGo search engine implementation
 */
class DuckDuckGoEngine(httpClient: HttpClient) : BaseSearchEngine(httpClient) {
    override val name = "duckduckgo"
    override val searchUrl = "https://html.duckduckgo.com/html/"
    override val searchMethod = "POST"

    override suspend fun searchText(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        page: Int
    ): List<TextResult> {
        val data = mutableMapOf<String, String>(
            "q" to query,
            "l" to region
        )

        if (page > 1) {
            data["s"] = "${10 + (page - 2) * 15}"
        }

        if (timelimit != null) {
            data["df"] = timelimit
        }

        val html = httpClient.post(searchUrl, data)
        return parseResults(html)
    }

    private fun parseResults(html: String): List<TextResult> {
        val results = mutableListOf<TextResult>()
        val document = parseHtml(html)

        document.select("div.result").forEach { element ->
            val titleElement = element.selectFirst("a.result__a")
            val snippetElement = element.selectFirst("a.result__snippet")

            val title = titleElement?.text() ?: ""
            val href = titleElement?.attr("href") ?: ""
            val body = snippetElement?.text() ?: ""

            if (title.isNotBlank() && href.isNotBlank() && !href.contains("duckduckgo.com/y.js")) {
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
