

package com.example.funfriday.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {

    LaunchedEffect(Unit) {
        delay(2000) // 2 seconds splash delay



        navController.navigate("login") {
                popUpTo("splash") { inclusive = true }

        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE23744)), // Zomato Red
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = "FunFriday",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Lunch made fun 🍽",
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(Modifier.height(20.dp))

            CircularProgressIndicator(
                color = Color.Black,
                strokeWidth = 10.dp
            )
        }
    }
}
