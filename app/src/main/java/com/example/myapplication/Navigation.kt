package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun Navigation(navController: NavHostController=rememberNavController()){

    NavHost(navController = navController, startDestination = Screen.LoginScreen.route){
        composable(Screen.LoginScreen.route){
            LoginScreen(navController = navController)
        }
        composable(Screen.RegisterFirstScreen.route){
            RegisterScreenFirst(navController = navController)
        }
        composable(Screen.RegisterSecondScreen.route){
            RegisterScreenSecond(navController = navController)
        }
        composable(Screen.ForgotPasswordScreen.route) {
            ForgotPasswordScreen(navController = navController)
        }
        composable(Screen.OtpAutheenticationScreen.route)  {
            OtpAuthenticationScreen(navController = navController)
        }
    }
}