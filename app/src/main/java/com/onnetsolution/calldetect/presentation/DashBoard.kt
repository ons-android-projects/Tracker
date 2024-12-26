package com.onnetsolution.calldetect.presentation

import android.annotation.SuppressLint
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.onnetsolution.calldetect.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow


//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun DashBoard() {
//    val context= LocalContext.current
//    val sharedPref=context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
//
//    //Observe shared preferences or state updates
//    var shouldRefreshFlow = remember { MutableStateFlow(sharedPref.getBoolean("shouldRefresh", false)) }
//
//    DisposableEffect(Unit) {
//        val listener = SharedPreferences.OnSharedPreferenceChangeListener{_,key->
//            if(key == "shouldRefresh"){
//                shouldRefreshFlow.value = sharedPref.getBoolean("shouldRefresh", false)
//                Log.d("DashBoard", "shouldRefresh updated to $shouldRefreshFlow")
//
////                //Reset the refresh flag
////                if(shouldRefresh){
////                    sharedPref.edit().putBoolean("shouldRefresh", false).apply()
////                }
//            }
//        }
//        sharedPref.registerOnSharedPreferenceChangeListener(listener)
//        onDispose{
//            sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
//        }
//    }
//    val shouldRefresh by shouldRefreshFlow.collectAsState()
//
//    Column(modifier = Modifier.fillMaxSize()) {
//        Text(text = "Welcome to Dashboard")
//        if (shouldRefresh) {
//            // Simulate an update (e.g., refresh data)
//             Log.d("DashBoard", "Refreshing Dashboard at ${System.currentTimeMillis()}")
//            Text(text = "Refreshing Dashboard at ${System.currentTimeMillis()}")
//            UserPhnNumber()
//        }
//        UserPhnNumber()
//    }
//}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun DashBoard() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome to Dashboard")

        UserPhnNumber()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun hideApp(context: Context) {
    val componentName = ComponentName(context, MainActivity::class.java)
    context.packageManager.setComponentEnabledSetting(
        componentName,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP
    )

    val appInfo=context.packageManager.getApplicationInfo(context.packageName,0)
    appInfo.nonLocalizedLabel = " "
}


