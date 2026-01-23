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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.example.funfriday.data.models.LunchCard
import com.example.funfriday.viewmodel.LunchViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    nav: NavController,
    vm: LunchViewModel = viewModel()
) {
    val currentUid = Firebase.auth.currentUser?.uid
    var deleteCard by remember { mutableStateOf<LunchCard?>(null) }

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
                            listOf(Color(0xFFE23744), Color(0xFFD32F2F))
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No Lunch Cards Yet 🍽️")
            }
        } else {

            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                items(vm.cards) { card ->

                    LaunchedEffect(card.createdBy) {
                        vm.loadUserName(card.createdBy)
                    }

                    val creatorName =
                        vm.userNameCache[card.createdBy] ?: "Loading..."

                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        LunchCardItem(
                            card = card,
                            creatorName = creatorName,
                            isCreator = card.createdBy == currentUid,
                            onDelete = { deleteCard = card },
                            onClick = {
                                nav.navigate("cardDetail/${card.id}")
                            }
                        )
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    /* ---------- DELETE CONFIRMATION ---------- */
    deleteCard?.let { card ->
        AlertDialog(
            onDismissRequest = { deleteCard = null },
            title = { Text("Delete Lunch Card") },
            text = { Text("Delete \"${card.title}\" permanently?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteCard(card.id)
                    deleteCard = null
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCard = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/* ---------------- CARD UI ---------------- */

@Composable
fun LunchCardItem(
    card: LunchCard,
    creatorName: String,
    isCreator: Boolean,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val dateText = remember(card.createdAt) {
        card.createdAt?.toDate()?.let {
            SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(it)
        } ?: ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFFE0B2), Color(0xFFFFCDD2))
                    )
                )
        ) {

            if (isCreator) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    card.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text("By $creatorName", fontSize = 13.sp, color = Color.DarkGray)
                Text("🕒 $dateText", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(6.dp))
                Text("View menu →", fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}
