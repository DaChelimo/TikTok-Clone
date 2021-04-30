package com.andre_max.tiktokclone.models

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class TheResult<out R> {

    data class Success<out T>(val data: T) : TheResult<T>()
    data class Error(val exception: Exception) : TheResult<Nothing>()
    object Loading : TheResult<Nothing>()

    @JvmName("tryData")
    fun getData() = (this as? Success)?.data
    fun forceData() = (this as Success).data

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"
        }
    }
    
    companion object {
        fun <T> theSuccess(data: T) = Success(data)
        fun theError(exception: Exception) = Error(exception)
    }
}

/**
 * `true` if [TheResult] is of type [Success] & holds non-null [Success.data].
 */
val TheResult<*>.succeeded
    get() = this is TheResult.Success && data != null