package com.ddgs.engines

import com.ddgs.HttpClient
import com.ddgs.models.BookResult

/**
 * Anna's Archive search engine implementation
 */
class AnnasArchiveEngine(httpClient: HttpClient) : BookSearchEngine(httpClient) {
    override val name = "annas_archive"

    private val searchUrl = "https://annas-archive.org/search"

    override suspend fun searchBooks(
        query: String,
        page: Int
    ): List<BookResult> {
        val params = mutableMapOf<String, String>(
            "q" to query,
            "page" to page.toString()
        )

        val html = httpClient.get(searchUrl, params)
        return parseResults(html)
    }

    private fun parseResults(html: String): List<BookResult> {
        val results = mutableListOf<BookResult>()
        val document = org.jsoup.Jsoup.parse(html)

        document.select("div.book-item").forEach { element ->
            val titleElement = element.selectFirst("h3")
            val authorElement = element.selectFirst("span.author")
            val publisherElement = element.selectFirst("span.publisher")
            val infoElement = element.selectFirst("span.info")
            val linkElement = element.selectFirst("a")
            val thumbElement = element.selectFirst("img")

            val title = titleElement?.text() ?: ""
            val author = authorElement?.text() ?: ""
            val publisher = publisherElement?.text() ?: ""
            val info = infoElement?.text() ?: ""
            val url = linkElement?.attr("href") ?: ""
            val thumbnail = thumbElement?.attr("src") ?: ""

            if (title.isNotBlank() && url.isNotBlank()) {
                results.add(BookResult(
                    title = normalizeText(title),
                    author = normalizeText(author),
                    publisher = normalizeText(publisher),
                    info = normalizeText(info),
                    url = normalizeUrl(url),
                    thumbnail = normalizeUrl(thumbnail)
                ))
            }
        }

        return results
    }

    private fun normalizeText(text: String): String = text.trim().replace(Regex("\\s+"), " ")
    private fun normalizeUrl(url: String): String {
        return if (url.startsWith("/")) {
            "https://annas-archive.org$url"
        } else {
            url.trim().replace(Regex("^//"), "https://")
        }
    }
}
