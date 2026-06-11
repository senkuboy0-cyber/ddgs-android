package com.ddgs.utils

import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

/**
 * String extensions for DDGS
 */
fun String.normalizeText(): String {
    return this.trim().replace(Regex("\\s+"), " ")
}

fun String.normalizeUrl(): String {
    return this.trim()
        .replace(Regex("^//"), "https://")
        .replace(Regex("\\s+"), "")
}

fun String.urlEncode(): String {
    return URLEncoder.encode(this, "UTF-8")
}

fun String.parseDate(format: String = "yyyy-MM-dd"): Date? {
    return try {
        SimpleDateFormat(format, Locale.US).parse(this)
    } catch (e: Exception) {
        null
    }
}

fun String.toRelativeTime(): String {
    val date = this.parseDate() ?: return this
    val now = Date()
    val diff = now.time - date.time

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 365 -> "${days / 365} year(s) ago"
        days > 30 -> "${days / 30} month(s) ago"
        days > 0 -> "$days day(s) ago"
        hours > 0 -> "$hours hour(s) ago"
        minutes > 0 -> "$minutes minute(s) ago"
        else -> "Just now"
    }
}

/**
 * Duration formatting utilities
 */
fun String.formatDuration(): String {
    // Input: "PT5M30S" (ISO 8601 duration)
    if (!this.startsWith("PT")) return this

    val duration = this.removePrefix("PT")
    var result = ""

    if (duration.contains("H")) {
        val hours = duration.substringBefore("H").toIntOrNull() ?: 0
        result += "${hours}h "
    }
    if (duration.contains("M")) {
        val minutes = duration.substringAfter("H", "").substringBefore("M").toIntOrNull() ?: 0
        result += "${minutes}m "
    }
    if (duration.contains("S")) {
        val seconds = duration.substringAfter("M", duration).substringBefore("S").toIntOrNull() ?: 0
        result += "${seconds}s"
    }

    return result.trim()
}

/**
 * File size formatting
 */
fun Long.formatFileSize(): String {
    val kb = 1024
    val mb = kb * 1024
    val gb = mb * 1024

    return when {
        this >= gb -> String.format("%.2f GB", this.toDouble() / gb)
        this >= mb -> String.format("%.2f MB", this.toDouble() / mb)
        this >= kb -> String.format("%.2f KB", this.toDouble() / kb)
        else -> "$this bytes"
    }
}
