package com.onnetsolution.calldetect.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.onnetsolution.calldetect.data.local.CallLogDataClass
import com.onnetsolution.calldetect.repository.CallLogRepo
import com.onnetsolution.calldetect.repository.TargetMobAndDateTimeRepo
import kotlinx.coroutines.launch

class CallLogViewModel(private val repo:CallLogRepo):ViewModel() {
    private val _callLogData:MutableLiveData<List<CallLogDataClass>> = MutableLiveData()
    val callLogData:LiveData<List<CallLogDataClass>> get()=_callLogData

    fun insertCallLog(data:List<CallLogDataClass>){
        viewModelScope.launch {
            repo.insertCallLog(data)
        }
    }

    fun getAllCallLogs(){
        viewModelScope.launch {
            try{
                _callLogData.postValue(repo.getAllCallLogs())
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun deleteCallLog(){
        repo.deleteAllCallLog()
    }
}

class CallLogViewModelFactory(private val repo: CallLogRepo):
    ViewModelProvider.Factory{
    override fun <T:ViewModel> create(modelClass: Class<T>):T{
        return CallLogViewModel(repo)as T
    }
}