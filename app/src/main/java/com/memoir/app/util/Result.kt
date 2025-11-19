package com.memoir.app.util

/**
 * Result sealed class for API responses and operations
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: String? = null, val throwable: Throwable? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error

    val isLoading: Boolean
        get() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun errorOrNull(): Error? = when (this) {
        is Error -> this
        else -> null
    }
}
