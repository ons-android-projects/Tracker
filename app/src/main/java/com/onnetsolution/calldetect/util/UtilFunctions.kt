package com.onnetsolution.calldetect.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

@SuppressLint("SuspiciousIndentation")
fun isConnectedToNetwork(context: Context):Boolean{
    val connectivityManager=
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

        return when{
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->true
            else -> false
        }
    }
    else{
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager.activeNetworkInfo
        @Suppress("DEPRECATION")
        return networkInfo != null && networkInfo.isConnected
    }
}
