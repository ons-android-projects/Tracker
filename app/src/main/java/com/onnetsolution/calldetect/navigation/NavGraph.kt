package com.onnetsolution.calldetect.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.onnetsolution.calldetect.presentation.DashBoard

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController:NavHostController){
   
    NavHost(navController = navController, startDestination = DashBoardScreen) {
        composable<DashBoardScreen> { DashBoard()  }
       // composable<CallLogscreen> { CallLogApp() }
    }
    
}