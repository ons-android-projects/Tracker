package com.onnetsolution.calldetect.util

sealed class NetworkResponse<T>(val data: T?=null,val message:String?=null) {
    class Loading<T> : NetworkResponse<T>()
    class Success<T>(data: T?=null,message: String?=null):NetworkResponse<T>(data, message)
    class Error<T>(message: String?):NetworkResponse<T>(message = message)
    class EmptyState<T> : NetworkResponse<T>()
}