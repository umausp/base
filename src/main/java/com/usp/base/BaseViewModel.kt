package com.usp.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usp.data.type.atomic
import com.usp.mylogging.tm.log
import com.usp.mynetwork.model.NetworkResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import java.lang.Error

open class BaseViewModel : ViewModel() {

    val loadingProgression = MutableStateFlow(false)

    fun <T, U> Flow<T>.collectData(
        mutableData: MutableStateFlow<U>,
        forLoading: Boolean = false
    ): Job {
        val job = atomic<Job?>(null)
        return map {
            it as NetworkResponse<Any, Error>
        }.onEach {
            when (it) {
                is NetworkResponse.ApiError -> handleError(it)
                is NetworkResponse.NetworkError -> handleNetworkError(it)
                is NetworkResponse.Success -> {
                    mutableData.value = it.body as U
                    loadingProgression.value = false
                }
                is NetworkResponse.UnknownError -> handleUnknownError(it)
            }
        }.onStart {
            if (forLoading) loadingProgression.value = true
        }.launchIn(viewModelScope)
            .also { job.value = it }
    }

    fun handleUnknownError(unknownError: NetworkResponse.UnknownError) {
        loadingProgression.value = false
        log.d(unknownError.toString())
    }

    fun handleNetworkError(networkError: NetworkResponse.NetworkError) {
        loadingProgression.value = false
        log.d(networkError.toString())
    }

    fun handleError(apiError: NetworkResponse.ApiError<Error>) {
        loadingProgression.value = false
        log.d(apiError.toString())
    }
}
