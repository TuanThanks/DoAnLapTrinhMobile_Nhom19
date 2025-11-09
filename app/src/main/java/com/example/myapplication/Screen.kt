package com.example.myapplication

import androidx.lifecycle.ViewModel

open class Screen(val route:String) {
    object LoginScreen: Screen("login_screen")
    object RegisterFirstScreen: Screen("register_first_screen")
    object RegisterSecondScreen: Screen("register_second_screen")
    object ForgotPasswordScreen: Screen("forgot_password_screen")
    object OtpAutheenticationScreen: Screen("otp_auth_screen")

}