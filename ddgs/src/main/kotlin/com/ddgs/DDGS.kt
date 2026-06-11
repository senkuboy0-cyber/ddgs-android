package com.ddgs

import com.ddgs.engines.*
import com.ddgs.models.*

/**
 * DDGS - Dux Distributed Global Search
 *
 * A metasearch library for Android that aggregates results from diverse web search services.
 *
 * @param proxy Proxy URL (supports http/https/socks5 protocols)
 * @param timeout Timeout in seconds for HTTP requests
 * @param verify Whether to verify SSL certificates
 */
class DDGS(
    private val proxy: String? = null,
    private val timeout: Int = 10,
    private val verify: Boolean = true
) {
    private val httpClient = HttpClient(proxy, timeout, verify)
    private val textEnginesCache = mutableMapOf<String, BaseSearchEngine>()
    private val imageEnginesCache = mutableMapOf<String, ImageSearchEngine>()
    private val newsEnginesCache = mutableMapOf<String, NewsSearchEngine>()
    private val videoEnginesCache = mutableMapOf<String, VideoSearchEngine>()
    private val bookEnginesCache = mutableMapOf<String, BookSearchEngine>()

    /**
     * Perform a text search
     */
    suspend fun text(
        query: String,
        region: String = "us-en",
        safesearch: String = "moderate",
        timelimit: String? = null,
        maxResults: Int = 10,
        page: Int = 1,
        backend: String = "auto"
    ): List<TextResult> = searchText(query, region, safesearch, timelimit, maxResults, page, backend)

    /**
     * Perform an image search
     */
    suspend fun images(
        query: String,
        region: String = "us-en",
        safesearch: String = "moderate",
        timelimit: String? = null,
        maxResults: Int = 10,
        page: Int = 1,
        backend: String = "auto",
        size: String? = null,
        color: String? = null,
        typeImage: String? = null,
        layout: String? = null,
        licenseImage: String? = null
    ): List<ImageResult> = searchImages(
        query, region, safesearch, timelimit, maxResults, page, backend,
        size, color, typeImage, layout, licenseImage
    )

    /**
     * Perform a news search
     */
    suspend fun news(
        query: String,
        region: String = "us-en",
        safesearch: String = "moderate",
        timelimit: String? = null,
        maxResults: Int = 10,
        page: Int = 1,
        backend: String = "auto"
    ): List<NewsResult> = searchNews(query, region, safesearch, timelimit, maxResults, page, backend)

    /**
     * Perform a video search
     */
    suspend fun videos(
        query: String,
        region: String = "us-en",
        safesearch: String = "moderate",
        timelimit: String? = null,
        maxResults: Int = 10,
        page: Int = 1,
        backend: String = "auto",
        resolution: String? = null,
        duration: String? = null,
        licenseVideos: String? = null
    ): List<VideoResult> = searchVideos(
        query, region, safesearch, timelimit, maxResults, page, backend,
        resolution, duration, licenseVideos
    )

    /**
     * Perform a book search
     */
    suspend fun books(
        query: String,
        maxResults: Int = 10,
        page: Int = 1,
        backend: String = "auto"
    ): List<BookResult> = searchBooks(query, maxResults, page, backend)

    /**
     * Extract content from a URL
     */
    suspend fun extract(url: String, format: ExtractFormat = ExtractFormat.TEXT_MARKDOWN): ExtractResult {
        return httpClient.extract(url, format)
    }

    private suspend fun searchText(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        maxResults: Int,
        page: Int,
        backend: String
    ): List<TextResult> {
        if (query.isBlank()) {
            throw DDGSException("Query cannot be empty")
        }

        val engines = getTextEngines(backend)
        val results = mutableListOf<TextResult>()
        val seenUrls = mutableSetOf<String>()

        for (engine in engines) {
            try {
                val engineResults = engine.search(query, region, safesearch, timelimit, page)

                for (result in engineResults) {
                    if (result.href.isNotBlank() && result.href !in seenUrls) {
                        seenUrls.add(result.href)
                        results.add(result)
                    }
                }

                if (results.size >= maxResults) break
            } catch (e: Exception) {
                // Continue with next engine
            }
        }

        return results.take(maxResults)
    }

    private suspend fun searchImages(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        maxResults: Int,
        page: Int,
        backend: String,
        size: String?,
        color: String?,
        typeImage: String?,
        layout: String?,
        licenseImage: String?
    ): List<ImageResult> {
        if (query.isBlank()) {
            throw DDGSException("Query cannot be empty")
        }

        val engines = getImageEngines(backend)
        val results = mutableListOf<ImageResult>()
        val seenUrls = mutableSetOf<String>()

        for (engine in engines) {
            try {
                val engineResults = engine.searchImages(
                    query, region, safesearch, timelimit, page,
                    size, color, typeImage, layout, licenseImage
                )

                for (result in engineResults) {
                    if (result.url.isNotBlank() && result.url !in seenUrls) {
                        seenUrls.add(result.url)
                        results.add(result)
                    }
                }

                if (results.size >= maxResults) break
            } catch (e: Exception) {
                // Continue with next engine
            }
        }

        return results.take(maxResults)
    }

    private suspend fun searchNews(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        maxResults: Int,
        page: Int,
        backend: String
    ): List<NewsResult> {
        if (query.isBlank()) {
            throw DDGSException("Query cannot be empty")
        }

        val engines = getNewsEngines(backend)
        val results = mutableListOf<NewsResult>()
        val seenUrls = mutableSetOf<String>()

        for (engine in engines) {
            try {
                val engineResults = engine.searchNews(query, region, safesearch, timelimit, page)

                for (result in engineResults) {
                    if (result.url.isNotBlank() && result.url !in seenUrls) {
                        seenUrls.add(result.url)
                        results.add(result)
                    }
                }

                if (results.size >= maxResults) break
            } catch (e: Exception) {
                // Continue with next engine
            }
        }

        return results.take(maxResults)
    }

    private suspend fun searchVideos(
        query: String,
        region: String,
        safesearch: String,
        timelimit: String?,
        maxResults: Int,
        page: Int,
        backend: String,
        resolution: String?,
        duration: String?,
        licenseVideos: String?
    ): List<VideoResult> {
        if (query.isBlank()) {
            throw DDGSException("Query cannot be empty")
        }

        val engines = getVideoEngines(backend)
        val results = mutableListOf<VideoResult>()
        val seenUrls = mutableSetOf<String>()

        for (engine in engines) {
            try {
                val engineResults = engine.searchVideos(
                    query, region, safesearch, timelimit, page,
                    resolution, duration, licenseVideos
                )

                for (result in engineResults) {
                    if (result.content.isNotBlank() && result.content !in seenUrls) {
                        seenUrls.add(result.content)
                        results.add(result)
                    }
                }

                if (results.size >= maxResults) break
            } catch (e: Exception) {
                // Continue with next engine
            }
        }

        return results.take(maxResults)
    }

    private suspend fun searchBooks(
        query: String,
        maxResults: Int,
        page: Int,
        backend: String
    ): List<BookResult> {
        if (query.isBlank()) {
            throw DDGSException("Query cannot be empty")
        }

        val engines = getBookEngines(backend)
        val results = mutableListOf<BookResult>()
        val seenUrls = mutableSetOf<String>()

        for (engine in engines) {
            try {
                val engineResults = engine.searchBooks(query, page)

                for (result in engineResults) {
                    if (result.url.isNotBlank() && result.url !in seenUrls) {
                        seenUrls.add(result.url)
                        results.add(result)
                    }
                }

                if (results.size >= maxResults) break
            } catch (e: Exception) {
                // Continue with next engine
            }
        }

        return results.take(maxResults)
    }

    private fun getTextEngines(backend: String): List<BaseSearchEngine> {
        val engineList = listOf(
            textEnginesCache.getOrPut("google") { GoogleEngine(httpClient) },
            textEnginesCache.getOrPut("duckduckgo") { DuckDuckGoEngine(httpClient) },
            textEnginesCache.getOrPut("bing") { BingEngine(httpClient) },
            textEnginesCache.getOrPut("brave") { BraveEngine(httpClient) },
            textEnginesCache.getOrPut("yandex") { YandexEngine(httpClient) },
            textEnginesCache.getOrPut("wikipedia") { WikipediaEngine(httpClient) }
        )

        return if (backend == "auto") {
            engineList.shuffled()
        } else {
            engineList.filter { it.name in backend.split(",").map { it.trim() } }
        }
    }

    private fun getImageEngines(backend: String): List<ImageSearchEngine> {
        val engineList = listOf(
            imageEnginesCache.getOrPut("bing_images") { BingImagesEngine(httpClient) },
            imageEnginesCache.getOrPut("duckduckgo_images") { DuckDuckGoImagesEngine(httpClient) }
        )

        return if (backend == "auto") {
            engineList.shuffled()
        } else {
            engineList.filter { it.name in backend.split(",").map { it.trim() } }
        }
    }

    private fun getNewsEngines(backend: String): List<NewsSearchEngine> {
        val engineList = listOf(
            newsEnginesCache.getOrPut("bing_news") { BingNewsEngine(httpClient) },
            newsEnginesCache.getOrPut("duckduckgo_news") { DuckDuckGoNewsEngine(httpClient) }
        )

        return if (backend == "auto") {
            engineList.shuffled()
        } else {
            engineList.filter { it.name in backend.split(",").map { it.trim() } }
        }
    }

    private fun getVideoEngines(backend: String): List<VideoSearchEngine> {
        val engineList = listOf(
            videoEnginesCache.getOrPut("duckduckgo_videos") { DuckDuckGoVideosEngine(httpClient) }
        )

        return if (backend == "auto") {
            engineList.shuffled()
        } else {
            engineList.filter { it.name in backend.split(",").map { it.trim() } }
        }
    }

    private fun getBookEngines(backend: String): List<BookSearchEngine> {
        val engineList = listOf(
            bookEnginesCache.getOrPut("annas_archive") { AnnasArchiveEngine(httpClient) }
        )

        return if (backend == "auto") {
            engineList.shuffled()
        } else {
            engineList.filter { it.name in backend.split(",").map { it.trim() } }
        }
    }

    companion object {
        /**
         * Available text search backends
         */
        val TEXT_BACKENDS = listOf("google", "bing", "duckduckgo", "brave", "yandex", "wikipedia")

        /**
         * Available image search backends
         */
        val IMAGE_BACKENDS = listOf("bing", "duckduckgo")

        /**
         * Available news search backends
         */
        val NEWS_BACKENDS = listOf("bing", "duckduckgo")

        /**
         * Available video search backends
         */
        val VIDEO_BACKENDS = listOf("duckduckgo")

        /**
         * Available book search backends
         */
        val BOOK_BACKENDS = listOf("annasarchive")
    }
}

enum class ExtractFormat {
    TEXT_MARKDOWN,
    TEXT_PLAIN,
    TEXT_RICH,
    TEXT,
    CONTENT
}
