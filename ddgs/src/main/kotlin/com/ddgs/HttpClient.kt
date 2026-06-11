package com.ddgs

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * HTTP Client wrapper using Ktor
 */
class HttpClient(
    private val proxy: String? = null,
    private val timeout: Int = 10,
    private val verify: Boolean = true
) {
    private val userAgents = listOf(
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
        "Mozilla/5.0 (Linux; Android 13; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
        "Mozilla/5.0 (Linux; Android 12; SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
        "Mozilla/5.0 (Linux; Android 14; Xiaomi 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"
    )

    private val ktor: HttpClient = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(timeout.toLong(), TimeUnit.SECONDS)
                readTimeout(timeout.toLong(), TimeUnit.SECONDS)
                writeTimeout(timeout.toLong(), TimeUnit.SECONDS)

                if (!verify) {
                    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    })

                    val sslContext = SSLContext.getInstance("TLS")
                    sslContext.init(null, trustAllCerts, SecureRandom())
                    sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                }
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = timeout * 1000L
            connectTimeoutMillis = timeout * 1000L
            socketTimeoutMillis = timeout * 1000L
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.NONE
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private fun getRandomUserAgent(): String = userAgents.random()

    suspend fun get(url: String, params: Map<String, String> = emptyMap()): String = withContext(Dispatchers.IO) {
        try {
            ktor.get(url) {
                headers {
                    append(HttpHeaders.UserAgent, getRandomUserAgent())
                    append(HttpHeaders.Accept, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    append(HttpHeaders.AcceptLanguage, "en-US,en;q=0.9")
                    append(HttpHeaders.AcceptEncoding, "gzip, deflate, br")
                }
                url {
                    params.forEach { (key, value) ->
                        parameters.append(key, value)
                    }
                }
            }.bodyAsText()
        } catch (e: Exception) {
            throw DDGSException("Failed to fetch $url: ${e.message}", e)
        }
    }

    suspend fun post(url: String, data: Map<String, String> = emptyMap()): String = withContext(Dispatchers.IO) {
        try {
            ktor.post(url) {
                headers {
                    append(HttpHeaders.UserAgent, getRandomUserAgent())
                    append(HttpHeaders.Accept, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    append(HttpHeaders.AcceptLanguage, "en-US,en;q=0.9")
                    append(HttpHeaders.AcceptEncoding, "gzip, deflate, br")
                    append(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
                }
                setBody(Parameters.build {
                    data.forEach { (key, value) ->
                        append(key, value)
                    }
                }.formUrlEncode())
            }.bodyAsText()
        } catch (e: Exception) {
            throw DDGSException("Failed to post to $url: ${e.message}", e)
        }
    }

    suspend fun extract(url: String, format: ExtractFormat): ExtractResult = withContext(Dispatchers.IO) {
        try {
            val html = get(url)
            val document = Jsoup.parse(html)

            // Remove scripts and styles
            document.select("script, style, nav, footer, header").remove()

            val content = when (format) {
                ExtractFormat.TEXT_MARKDOWN -> htmlToMarkdown(document)
                ExtractFormat.TEXT_PLAIN -> document.text()
                ExtractFormat.TEXT_RICH -> htmlToRichText(document)
                ExtractFormat.TEXT -> html
                ExtractFormat.CONTENT -> html
            }

            ExtractResult(url, content)
        } catch (e: Exception) {
            throw DDGSException("Failed to extract content from $url: ${e.message}", e)
        }
    }

    private fun htmlToMarkdown(document: Document): String {
        val sb = StringBuilder()

        document.body()?.children()?.forEach { element ->
            sb.append(elementToMarkdown(element, 0))
            sb.append("\n")
        }

        return sb.toString().trim()
    }

    private fun elementToMarkdown(element: org.jsoup.nodes.Element, depth: Int): String {
        val sb = StringBuilder()
        val indent = "  ".repeat(depth)

        when (element.tagName()) {
            "h1" -> sb.append("# ${element.text()}\n")
            "h2" -> sb.append("## ${element.text()}\n")
            "h3" -> sb.append("### ${element.text()}\n")
            "h4" -> sb.append("#### ${element.text()}\n")
            "h5" -> sb.append("##### ${element.text()}\n")
            "h6" -> sb.append("###### ${element.text()}\n")
            "p" -> sb.append("${element.text()}\n")
            "a" -> {
                val href = element.attr("href")
                val text = element.text()
                if (href.isNotBlank() && text.isNotBlank()) {
                    sb.append("[$text]($href)")
                } else {
                    sb.append(text)
                }
            }
            "ul", "ol" -> {
                element.children().forEach { li ->
                    val prefix = if (element.tagName() == "ul") "- " else "1. "
                    sb.append("$indent$prefix${li.text()}\n")
                }
            }
            "blockquote" -> sb.append("> ${element.text()}\n")
            "code" -> sb.append("`${element.text()}`")
            "pre" -> sb.append("```\n${element.text()}\n```\n")
            "img" -> {
                val src = element.attr("src")
                val alt = element.attr("alt")
                if (src.isNotBlank()) {
                    sb.append("![$alt]($src)\n")
                }
            }
            "div", "span", "article", "section", "main" -> {
                element.children().forEach { child ->
                    sb.append(elementToMarkdown(child, depth))
                }
            }
            else -> {
                if (element.children().isEmpty() && element.text().isNotBlank()) {
                    sb.append(element.text())
                } else {
                    element.children().forEach { child ->
                        sb.append(elementToMarkdown(child, depth))
                    }
                }
            }
        }

        return sb.toString()
    }

    private fun htmlToRichText(document: Document): String {
        val sb = StringBuilder()

        document.body()?.children()?.forEach { element ->
            sb.append(elementToRichText(element))
            sb.append("\n")
        }

        return sb.toString().trim()
    }

    private fun elementToRichText(element: org.jsoup.nodes.Element): String {
        val sb = StringBuilder()

        when (element.tagName()) {
            "h1", "h2", "h3", "h4", "h5", "h6" -> sb.append("**${element.text()}**\n")
            "p" -> sb.append("${element.text()}\n")
            "ul", "ol" -> {
                element.children().forEach { li ->
                    sb.append("• ${li.text()}\n")
                }
            }
            "blockquote" -> sb.append("> ${element.text()}\n")
            else -> {
                if (element.children().isEmpty() && element.text().isNotBlank()) {
                    sb.append(element.text())
                } else {
                    element.children().forEach { child ->
                        sb.append(elementToRichText(child))
                    }
                }
            }
        }

        return sb.toString()
    }

    fun close() {
        ktor.close()
    }
}

data class ExtractResult(
    val url: String,
    val content: String
)
