# DDGS Android SDK

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg) ![Android](https://img.shields.io/badge/Android-21+-green.svg)

A **metasearch library for Android** that aggregates results from diverse web search services. This is a Kotlin/Android port of the [DDGS](https://github.com/deedy5/ddgs) Python library.

## Features

- 🔍 **Text Search** - Search across multiple engines (Google, Bing, DuckDuckGo, Brave, Yandex, Wikipedia)
- 🖼️ **Image Search** - Search images with filters (size, color, type, layout)
- 📰 **News Search** - Get latest news from multiple sources
- 🎬 **Video Search** - Search videos with duration and resolution filters
- 📚 **Book Search** - Search books via Anna's Archive
- 🌐 **Content Extraction** - Extract clean content from any URL (Markdown, Plain text, Rich text)
- 🔄 **Proxy Support** - HTTP/HTTPS/SOCKS5 proxy support
- ⚡ **Coroutines** - Fully async with Kotlin Coroutines

## Installation

### Gradle (Kotlin DSL)

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// build.gradle.kts (app module)
dependencies {
    implementation("com.ddgs:ddgs:1.0.0")
}
```

### Maven

```xml
<dependency>
    <groupId>com.ddgs</groupId>
    <artifactId>ddgs</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

### Text Search

```kotlin
import com.ddgs.DDGS
import com.ddgs.models.TextResult
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val ddgs = DDGS()
    
    // Simple text search
    val results: List<TextResult> = ddgs.text("Kotlin programming")
    
    // Advanced search with options
    val advancedResults = ddgs.text(
        query = "android development",
        region = "us-en",
        safesearch = "moderate",
        timelimit = "m",  // d, w, m, y
        maxResults = 20,
        page = 1,
        backend = "google,bing"  // or "auto"
    )
    
    results.forEach { result ->
        println("Title: ${result.title}")
        println("URL: ${result.href}")
        println("Description: ${result.body}")
        println("---")
    }
}
```

### Image Search

```kotlin
fun searchImages() = runBlocking {
    val ddgs = DDGS()
    
    val images = ddgs.images(
        query = "nature landscape",
        maxResults = 10,
        size = "large",
        color = "blue",
        typeImage = "photo",
        layout = "wide"
    )
    
    images.forEach { image ->
        println("Title: ${image.title}")
        println("Image URL: ${image.image}")
        println("Source: ${image.source}")
    }
}
```

### News Search

```kotlin
fun searchNews() = runBlocking {
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
        println("URL: ${article.url}")
    }
}
```

### Video Search

```kotlin
fun searchVideos() = runBlocking {
    val ddgs = DDGS()
    
    val videos = ddgs.videos(
        query = "tutorial",
        duration = "medium",
        resolution = "high"
    )
    
    videos.forEach { video ->
        println("Title: ${video.title}")
        println("Duration: ${video.duration}")
        println("URL: ${video.content}")
        println("Provider: ${video.provider}")
    }
}
```

### Book Search

```kotlin
fun searchBooks() = runBlocking {
    val ddgs = DDGS()
    
    val books = ddgs.books(
        query = "kotlin in action",
        maxResults = 10
    )
    
    books.forEach { book ->
        println("Title: ${book.title}")
        println("Author: ${book.author}")
        println("Publisher: ${book.publisher}")
        println("URL: ${book.url}")
    }
}
```

### Content Extraction

```kotlin
import com.ddgs.DDGS
import com.ddgs.ExtractFormat

fun extractContent() = runBlocking {
    val ddgs = DDGS()
    
    // Extract as Markdown (default)
    val markdown = ddgs.extract("https://example.com")
    println(markdown.content)
    
    // Extract as plain text
    val plainText = ddgs.extract(
        "https://example.com",
        format = ExtractFormat.TEXT_PLAIN
    )
    
    // Extract as rich text
    val richText = ddgs.extract(
        "https://example.com",
        format = ExtractFormat.TEXT_RICH
    )
}
```

### With Proxy

```kotlin
fun withProxy() = runBlocking {
    val ddgs = DDGS(
        proxy = "http://user:pass@example.com:3128",
        timeout = 15
    )
    
    val results = ddgs.text("search query")
}
```

## API Reference

### DDGS Class

```kotlin
class DDGS(
    proxy: String? = null,      // Proxy URL
    timeout: Int = 10,           // Timeout in seconds
    verify: Boolean = true       // SSL verification
)
```

### Search Methods

| Method | Description | Backends |
|--------|-------------|----------|
| `text()` | Text search | google, bing, duckduckgo, brave, yandex, wikipedia |
| `images()` | Image search | bing, duckduckgo |
| `news()` | News search | bing, duckduckgo |
| `videos()` | Video search | duckduckgo |
| `books()` | Book search | annasarchive |
| `extract()` | URL content extraction | - |

### Available Backends

```kotlin
DDGS.TEXT_BACKENDS   // List of text search backends
DDGS.IMAGE_BACKENDS  // List of image search backends
DDGS.NEWS_BACKENDS   // List of news search backends
DDGS.VIDEO_BACKENDS  // List of video search backends
DDGS.BOOK_BACKENDS   // List of book search backends
```

## Result Models

### TextResult
```kotlin
data class TextResult(
    val title: String,   // Result title
    val href: String,    // URL
    val body: String     // Description/snippet
)
```

### ImageResult
```kotlin
data class ImageResult(
    val title: String,
    val image: String,      // Full image URL
    val thumbnail: String,  // Thumbnail URL
    val url: String,        // Source page URL
    val height: String,
    val width: String,
    val source: String      // Search engine source
)
```

### NewsResult
```kotlin
data class NewsResult(
    val date: String,
    val title: String,
    val body: String,
    val url: String,
    val image: String,
    val source: String
)
```

### VideoResult
```kotlin
data class VideoResult(
    val title: String,
    val content: String,        // Video URL
    val description: String,
    val duration: String,
    val embedUrl: String,
    val images: Map<String, String>,
    val provider: String,
    val published: String,
    val publisher: String,
    val statistics: Map<String, String>,
    val uploader: String
)
```

### BookResult
```kotlin
data class BookResult(
    val title: String,
    val author: String,
    val publisher: String,
    val info: String,
    val url: String,
    val thumbnail: String
)
```

## Permissions

Add these permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Dependencies

- **Ktor** - HTTP client (OkHttp engine)
- **Jsoup** - HTML parsing
- **Kotlin Coroutines** - Async operations
- **Kotlinx Serialization** - JSON parsing

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Disclaimer

This library is for educational purposes only. Please respect the terms of service of the search engines you use.
