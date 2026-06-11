package com.ddgs

/**
 * Base exception for DDGS library
 */
open class DDGSException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when a search request times out
 */
class TimeoutException(message: String, cause: Throwable? = null) : DDGSException(message, cause)
