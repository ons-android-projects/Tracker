package com.onnetsolution.calldetect.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.onnetsolution.calldetect.data.local.MobAndDateTimeDataClass
import com.onnetsolution.calldetect.repository.TargetMobAndDateTimeRepo
import kotlinx.coroutines.launch

class TargetMobAndDateTimeViewModel(private val repo:TargetMobAndDateTimeRepo):ViewModel() {
    private val _targetUserData:MutableLiveData<MobAndDateTimeDataClass?> = MutableLiveData()
    val targetUserData: LiveData<MobAndDateTimeDataClass?> get()=_targetUserData

    fun insertUserMobAndDateTime(data:MobAndDateTimeDataClass){
        viewModelScope.launch {
            repo.insertUserMobAndDateTime(data)
        }
    }

    fun getMobAndDateTime(){
        viewModelScope.launch {
            try {
                _targetUserData.postValue(repo.getMobAndDateTime())
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun updateDateTime(updatedDateTime:String){
        viewModelScope.launch {
            repo.updateDateTime(updatedDateTime)
        }
    }

    fun deleteTargetUser(){
        repo.deleteTargetUser()
    }
}

class TargetMobAndDateTimeViewModelFactory(private val repo:TargetMobAndDateTimeRepo):ViewModelProvider.Factory{
    override fun <T:ViewModel> create(modelClass: Class<T>):T{
        return TargetMobAndDateTimeViewModel(repo)as T
    }
}