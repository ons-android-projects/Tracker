package com.onnetsolution.calldetect.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.onnetsolution.calldetect.data.response.CallLogResponse
import com.onnetsolution.calldetect.usecase.UploadCallLogUseCase
import com.onnetsolution.calldetect.usecase.UploadMobAndDateTimeUseCase
import com.onnetsolution.calldetect.util.NetworkResponse
import com.onnetsolution.calldetect.util.isConnectedToNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

class UploadCallLogViewModel(private val uploadCallLogUseCase: UploadCallLogUseCase):ViewModel() {
    private val _callLogState : MutableStateFlow<NetworkResponse<CallLogResponse>> = MutableStateFlow(NetworkResponse.EmptyState())
    val callLogState : StateFlow<NetworkResponse<CallLogResponse>> = _callLogState.asStateFlow()

    fun uploadCallLogs(
        context: Context,
        callLogs:String
    ){
        if (!isConnectedToNetwork(context)){
            _callLogState.value = NetworkResponse.Error("Network Unavailable")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _callLogState.value = NetworkResponse.Loading()
            uploadCallLogUseCase(callLogs)
                .catch {exception->
                    when(exception){
                        is SocketTimeoutException ->{
                            _callLogState.value = NetworkResponse.Error("Server is unreachable")
                        }
                        else->{
                            _callLogState.value = NetworkResponse.Error(exception.message)
                        }
                    }
                }
                .collect{response->
                    val body = response.body()
                    if (body?.error!!){
                        _callLogState.value = NetworkResponse.Error(body.message)
                    }else{
                        _callLogState.value = NetworkResponse.Success(body)
                    }
                }
        }
    }

}

class UploadCallLogViewModelFactory(private val uploadCallLogUseCase: UploadCallLogUseCase):
    ViewModelProvider.Factory{
    override fun <T:ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(UploadCallLogViewModel::class.java)){
            return UploadCallLogViewModel(uploadCallLogUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}