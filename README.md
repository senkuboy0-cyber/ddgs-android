# DDGS Android SDK

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg) ![Android](https://img.shields.io/badge/Android-21+-green.svg) ![License](https://img.shields.io/badge/License-MIT-blue.svg)

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

```kotlin
import com.ddgs.DDGS
import com.ddgs.models.TextResult
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val ddgs = DDGS()
    
    // Text search
    val results: List<TextResult> = ddgs.text("Kotlin programming")
    
    // Image search
    val images = ddgs.images("nature", size = "large")
    
    // News search
    val news = ddgs.news("technology", timelimit = "d")
    
    // Content extraction
    val content = ddgs.extract("https://example.com")
}
```

## Search Methods

| Method | Description | Backends |
|--------|-------------|----------|
| `text()` | Text search | google, bing, duckduckgo, brave, yandex, wikipedia |
| `images()` | Image search | bing, duckduckgo |
| `news()` | News search | bing, duckduckgo |
| `videos()` | Video search | duckduckgo |
| `books()` | Book search | annasarchive |
| `extract()` | URL content extraction | - |

## Project Structure

```
ddgs-android/
├── ddgs/
│   └── src/main/kotlin/com/ddgs/
│       ├── DDGS.kt              # Main class
│       ├── DDGSException.kt     # Exceptions
│       ├── HttpClient.kt        # Ktor HTTP client
│       ├── models/
│       │   └── Results.kt       # Result data classes
│       ├── engines/
│       │   ├── BaseSearchEngine.kt
│       │   ├── GoogleEngine.kt
│       │   ├── DuckDuckGoEngine.kt
│       │   ├── BingEngine.kt
│       │   ├── BraveEngine.kt
│       │   ├── YandexEngine.kt
│       │   ├── WikipediaEngine.kt
│       │   ├── BingImagesEngine.kt
│       │   ├── DuckDuckGoImagesEngine.kt
│       │   ├── BingNewsEngine.kt
│       │   ├── DuckDuckGoNewsEngine.kt
│       │   ├── DuckDuckGoVideosEngine.kt
│       │   └── AnnasArchiveEngine.kt
│       ├── utils/
│       │   └── Extensions.kt
│       └── demo/
│           ├── DemoActivity.kt
│           └── Examples.kt
├── build.gradle.kts
└── settings.gradle.kts
```

## Dependencies

- **Ktor** - HTTP client (OkHttp engine)
- **Jsoup** - HTML parsing
- **Kotlin Coroutines** - Async operations
- **Kotlinx Serialization** - JSON parsing

## Permissions

Add these to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## License

MIT License - see [LICENSE](ddgs/src/main/kotlin/com/ddgs/LICENSE)

## Disclaimer

This library is for educational purposes only. Please respect the terms of service of the search engines you use.
