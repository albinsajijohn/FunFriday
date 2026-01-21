package com.example.funfriday.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.funfriday.viewmodel.LunchViewModel

@Composable
fun HomeScreen(
    nav: NavController,
    vm: LunchViewModel = viewModel()
) {

    LaunchedEffect(Unit) {
        vm.loadCards()
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFE23744), // Zomato red
                                Color(0xFFD32F2F)
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Text(
                    "Fun Friday",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Discover today’s lunch 🍛",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { nav.navigate("createCard") },
                containerColor = Color(0xFFE23744)
            ) {
                Text("+", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->

        if (vm.cards.isEmpty()) {
            /* ---------- EMPTY STATE ---------- */
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No Lunch Cards Yet 🍽️",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tap + to create today’s menu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

        } else {

            /* ---------- LIST ---------- */
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                items(vm.cards) { card ->

                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {

                        ZomatoStyleCard(
                            title = card.title,
                            onClick = {
                                nav.navigate("cardDetail/${card.id}")
                            }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}
@Composable
fun ZomatoStyleCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFFE0B2),
                            Color(0xFFFFCDD2)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "View menu →",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}
