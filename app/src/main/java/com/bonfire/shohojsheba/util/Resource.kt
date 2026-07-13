package com.bonfire.shohojsheba.util

sealed class Resource<T>(val data: T? = null, val error: String? = null) {
    class Loading<T> : Resource<T>()
    class Success<T>(data: T?) : Resource<T>(data)
    class Error<T>(error: String, data: T? = null) : Resource<T>(data, error)

    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
}
