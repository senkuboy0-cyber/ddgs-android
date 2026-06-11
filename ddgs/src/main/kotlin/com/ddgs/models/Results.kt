package com.ddgs.models

/**
 * Base result class with normalization
 */
abstract class BaseResult {
    abstract fun normalize(): Map<String, String>
}

/**
 * Text search result
 */
data class TextResult(
    val title: String = "",
    val href: String = "",
    val body: String = ""
) : BaseResult() {
    override fun normalize(): Map<String, String> = mapOf(
        "title" to title.trim(),
        "href" to normalizeUrl(href),
        "body" to body.trim()
    )

    private fun normalizeUrl(url: String): String {
        return url.trim()
            .replace("\\s+".toRegex(), "")
            .replace("^//".toRegex(), "https://")
    }
}

/**
 * Image search result
 */
data class ImageResult(
    val title: String = "",
    val image: String = "",
    val thumbnail: String = "",
    val url: String = "",
    val height: String = "",
    val width: String = "",
    val source: String = ""
) : BaseResult() {
    override fun normalize(): Map<String, String> = mapOf(
        "title" to title.trim(),
        "image" to normalizeUrl(image),
        "thumbnail" to normalizeUrl(thumbnail),
        "url" to normalizeUrl(url),
        "height" to height,
        "width" to width,
        "source" to source.trim()
    )

    private fun normalizeUrl(url: String): String {
        return url.trim()
            .replace("\\s+".toRegex(), "")
            .replace("^//".toRegex(), "https://")
    }
}

/**
 * News search result
 */
data class NewsResult(
    val date: String = "",
    val title: String = "",
    val body: String = "",
    val url: String = "",
    val image: String = "",
    val source: String = ""
) : BaseResult() {
    override fun normalize(): Map<String, String> = mapOf(
        "date" to normalizeDate(date),
        "title" to title.trim(),
        "body" to body.trim(),
        "url" to normalizeUrl(url),
        "image" to normalizeUrl(image),
        "source" to source.trim()
    )

    private fun normalizeUrl(url: String): String {
        return url.trim()
            .replace("\\s+".toRegex(), "")
            .replace("^//".toRegex(), "https://")
    }

    private fun normalizeDate(date: String): String {
        return date.trim()
            .replace(Regex("\\s+"), " ")
    }
}

/**
 * Video search result
 */
data class VideoResult(
    val title: String = "",
    val content: String = "",
    val description: String = "",
    val duration: String = "",
    val embedHtml: String = "",
    val embedUrl: String = "",
    val imageToken: String = "",
    val images: Map<String, String> = emptyMap(),
    val provider: String = "",
    val published: String = "",
    val publisher: String = "",
    val statistics: Map<String, String> = emptyMap(),
    val uploader: String = ""
) : BaseResult() {
    override fun normalize(): Map<String, String> = mapOf(
        "title" to title.trim(),
        "content" to normalizeUrl(content),
        "description" to description.trim(),
        "duration" to duration.trim(),
        "embedUrl" to normalizeUrl(embedUrl),
        "provider" to provider.trim(),
        "published" to published.trim(),
        "publisher" to publisher.trim(),
        "uploader" to uploader.trim()
    )

    private fun normalizeUrl(url: String): String {
        return url.trim()
            .replace("\\s+".toRegex(), "")
            .replace("^//".toRegex(), "https://")
    }
}

/**
 * Book search result
 */
data class BookResult(
    val title: String = "",
    val author: String = "",
    val publisher: String = "",
    val info: String = "",
    val url: String = "",
    val thumbnail: String = ""
) : BaseResult() {
    override fun normalize(): Map<String, String> = mapOf(
        "title" to title.trim(),
        "author" to author.trim(),
        "publisher" to publisher.trim(),
        "info" to info.trim(),
        "url" to normalizeUrl(url),
        "thumbnail" to normalizeUrl(thumbnail)
    )

    private fun normalizeUrl(url: String): String {
        return url.trim()
            .replace("\\s+".toRegex(), "")
            .replace("^//".toRegex(), "https://")
    }
}
