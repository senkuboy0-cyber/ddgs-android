package com.ddgs.engines

import com.ddgs.HttpClient
import com.ddgs.models.*
import org.jsoup.Jsoup

/**
 * Base search engine interface
 */
abstract class BaseSearchEngine(
    protected val httpClient: HttpClient
) {
    abstract val name: String
    abstract val searchUrl: String
    abstract val searchMethod: String

    abstract suspend fun searchText(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        page: Int
    ): List<TextResult>

    suspend fun search(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        page: Int
    ): List<TextResult> = searchText(query, region, safesearch, timelimit, page)

    protected fun parseHtml(html: String): org.jsoup.nodes.Document {
        return Jsoup.parse(html)
    }

    protected fun normalizeText(text: String): String {
        return text.trim().replace(Regex("\\s+"), " ")
    }

    protected fun normalizeUrl(url: String): String {
        return url.trim()
            .replace(Regex("^//"), "https://")
            .replace(Regex("\\s+"), "")
    }
}

/**
 * Image search engine interface
 */
abstract class ImageSearchEngine(
    protected val httpClient: HttpClient
) {
    abstract val name: String

    abstract suspend fun searchImages(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        page: Int,
        size: String?,
        color: String?,
        typeImage: String?,
        layout: String?,
        licenseImage: String?
    ): List<ImageResult>
}

/**
 * News search engine interface
 */
abstract class NewsSearchEngine(
    protected val httpClient: HttpClient
) {
    abstract val name: String

    abstract suspend fun searchNews(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        page: Int
    ): List<NewsResult>
}

/**
 * Video search engine interface
 */
abstract class VideoSearchEngine(
    protected val httpClient: HttpClient
) {
    abstract val name: String

    abstract suspend fun searchVideos(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        page: Int,
        resolution: String?,
        duration: String?,
        licenseVideos: String?
    ): List<VideoResult>
}

/**
 * Book search engine interface
 */
abstract class BookSearchEngine(
    protected val httpClient: HttpClient
) {
    abstract val name: String

    abstract suspend fun searchBooks(
        query: String,
        page: Int
    ): List<BookResult>
}
