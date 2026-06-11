package com.ddgs.engines

import com.ddgs.HttpClient
import com.ddgs.models.TextResult

/**
 * Google search engine implementation
 */
class GoogleEngine(httpClient: HttpClient) : BaseSearchEngine(httpClient) {
    override val name = "google"
    override val searchUrl = "https://www.google.com/search"
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
            "start" to "${(page - 1) * 10}"
        )

        // Safe search
        when (safesearch) {
            "on" -> params["safe"] = "active"
            "off" -> params["safe"] = "off"
            else -> params["safe"] = "medium"
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

        // Region
        if (region != "us-en") {
            params["cr"] = region
        }

        val html = httpClient.get(searchUrl, params)
        return parseResults(html)
    }

    private fun parseResults(html: String): List<TextResult> {
        val results = mutableListOf<TextResult>()
        val document = parseHtml(html)

        document.select("div.g").forEach { element ->
            val titleElement = element.selectFirst("h3")
            val linkElement = element.selectFirst("a")
            val snippetElement = element.selectFirst("div[data-sncf]")
                ?: element.selectFirst("div.VwiC3b")
                ?: element.selectFirst("span.aCOpRe")

            val title = titleElement?.text() ?: ""
            val href = linkElement?.attr("href") ?: ""
            val body = snippetElement?.text() ?: ""

            if (title.isNotBlank() && href.isNotBlank() && !href.contains("google.com")) {
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
