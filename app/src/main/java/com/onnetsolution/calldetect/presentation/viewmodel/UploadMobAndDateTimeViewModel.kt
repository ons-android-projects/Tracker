package com.onnetsolution.calldetect.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.onnetsolution.calldetect.data.response.MobAndDateTimeResponse
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

class UploadMobAndDateTimeViewModel(private val uploadMobAndDateTimeUseCase: UploadMobAndDateTimeUseCase):ViewModel() {
    private val _mobDateTimeFlow : MutableStateFlow<NetworkResponse<MobAndDateTimeResponse>> = MutableStateFlow(NetworkResponse.EmptyState())
    val mobDateTimeFlow : StateFlow<NetworkResponse<MobAndDateTimeResponse>> = _mobDateTimeFlow.asStateFlow()

    fun uploadMobAndDateTime(context: Context,targetUserMob:String,installationDateTime:String){
        if(!isConnectedToNetwork(context)){
            _mobDateTimeFlow.value=NetworkResponse.Error("Internet unavailable.")
        }
        else{
            viewModelScope.launch (Dispatchers.IO){
                _mobDateTimeFlow.value = NetworkResponse.Loading()
                val res=uploadMobAndDateTimeUseCase(targetUserMob, installationDateTime)
                res
                    .catch { exception->
                        if(exception is SocketTimeoutException){
                            _mobDateTimeFlow.value=NetworkResponse.Error("Server unreachable.")
                        }else{
                            _mobDateTimeFlow.value=NetworkResponse.Error(exception.message)
                        }
                    }
                    .collect{result->
                        val body=result.body()
                        if(body?.error ==true){
                            _mobDateTimeFlow.value=NetworkResponse.Error(body.updated_datetime)
                        }else{
                            _mobDateTimeFlow.value=NetworkResponse.Success(body)
                        }
                    }
            }
        }
    }
}


class UploadMobAndDateTimeViewModelFactory(private val uploadMobAndDateTimeUseCase: UploadMobAndDateTimeUseCase):ViewModelProvider.Factory{
    override fun <T:ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(UploadMobAndDateTimeViewModel::class.java)){
            return UploadMobAndDateTimeViewModel(uploadMobAndDateTimeUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}