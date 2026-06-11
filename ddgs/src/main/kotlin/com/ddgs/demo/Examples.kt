package com.ddgs.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ddgs.DDGS
import com.ddgs.ExtractFormat
import com.ddgs.models.*
import kotlinx.coroutines.launch

/**
 * Example usage of DDGS Android SDK
 */
fun main() {
    // This is a demonstration file showing various usage patterns
    println("DDGS Android SDK Demo")
}

/**
 * Example 1: Basic text search
 */
suspend fun exampleTextSearch() {
    val ddgs = DDGS()

    // Simple search
    val results = ddgs.text("Kotlin programming")
    results.forEach { result ->
        println("Title: ${result.title}")
        println("URL: ${result.href}")
        println("---")
    }
}

/**
 * Example 2: Advanced text search with filters
 */
suspend fun exampleAdvancedTextSearch() {
    val ddgs = DDGS()

    val results = ddgs.text(
        query = "android development tutorial",
        region = "us-en",
        safesearch = "moderate",
        timelimit = "m",  // Last month
        maxResults = 20,
        page = 1,
        backend = "google,bing"  // Use specific backends
    )

    println("Found ${results.size} results")
}

/**
 * Example 3: Image search with filters
 */
suspend fun exampleImageSearch() {
    val ddgs = DDGS()

    val images = ddgs.images(
        query = "mountain landscape",
        maxResults = 15,
        size = "large",
        color = "blue",
        typeImage = "photo",
        layout = "wide"
    )

    images.forEach { image ->
        println("Image: ${image.title}")
        println("URL: ${image.image}")
        println("Size: ${image.width}x${image.height}")
    }
}

/**
 * Example 4: News search
 */
suspend fun exampleNewsSearch() {
    val ddgs = DDGS()

    val news = ddgs.news(
        query = "technology",
        timelimit = "d",  // Last 24 hours
        maxResults = 10
    )

    news.forEach { article ->
        println("Title: ${article.title}")
        println("Source: ${article.source}")
        println("Date: ${article.date}")
    }
}

/**
 * Example 5: Video search
 */
suspend fun exampleVideoSearch() {
    val ddgs = DDGS()

    val videos = ddgs.videos(
        query = "kotlin tutorial",
        duration = "medium",
        resolution = "high"
    )

    videos.forEach { video ->
        println("Title: ${video.title}")
        println("Duration: ${video.duration}")
        println("URL: ${video.content}")
    }
}

/**
 * Example 6: Book search
 */
suspend fun exampleBookSearch() {
    val ddgs = DDGS()

    val books = ddgs.books(
        query = "kotlin in action",
        maxResults = 10
    )

    books.forEach { book ->
        println("Title: ${book.title}")
        println("Author: ${book.author}")
        println("Publisher: ${book.publisher}")
    }
}

/**
 * Example 7: Content extraction
 */
suspend fun exampleExtract() {
    val ddgs = DDGS()

    // Extract as Markdown
    val markdown = ddgs.extract(
        "https://kotlinlang.org",
        format = ExtractFormat.TEXT_MARKDOWN
    )
    println(markdown.content)

    // Extract as plain text
    val plainText = ddgs.extract(
        "https://kotlinlang.org",
        format = ExtractFormat.TEXT_PLAIN
    )
    println(plainText.content)
}

/**
 * Example 8: Using with proxy
 */
suspend fun exampleWithProxy() {
    val ddgs = DDGS(
        proxy = "http://user:pass@proxy.example.com:8080",
        timeout = 30
    )

    val results = ddgs.text("search query")
    println("Found ${results.size} results")
}

/**
 * Example 9: Using specific backend
 */
suspend fun exampleSpecificBackend() {
    val ddgs = DDGS()

    // Use only Google
    val googleResults = ddgs.text(
        "search query",
        backend = "google"
    )

    // Use multiple specific backends
    val multiResults = ddgs.text(
        "search query",
        backend = "google,bing,duckduckgo"
    )
}
