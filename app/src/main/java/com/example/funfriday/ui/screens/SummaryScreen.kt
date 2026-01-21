package com.example.funfriday.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.funfriday.data.models.LunchCard
import com.example.funfriday.viewmodel.LunchViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun SummaryScreen(
    nav: NavController,
    cardId: String,
    vm: LunchViewModel = viewModel()
) {
    val menu = vm.menu
    val selections = vm.selections

    val currentUserId = Firebase.auth.currentUser?.uid
    var card by remember { mutableStateOf<LunchCard?>(null) }

    LaunchedEffect(cardId) {
        vm.loadMenu(cardId)
        vm.loadSelections(cardId)
        vm.getCard(cardId) { fetched ->
            card = fetched
        }
    }

    /* ---------- LOADING ---------- */

    if (card == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    /* ---------- ACCESS CONTROL ---------- */

    if (card?.createdBy != currentUserId) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Only the creator can view the summary")
        }
        return
    }

    /* ---------- UI ---------- */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {

        /* ---------- HEADER ---------- */

        Surface(
            color = Color(0xFFE23744),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Order Summary",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    card?.title ?: "",
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            /* ---------- TOTAL ORDERS ---------- */

            item {
                Text(
                    "Total Orders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                val totalCounts = selections
                    .flatMap { it.items }
                    .groupingBy { it }
                    .eachCount()

                if (totalCounts.isEmpty()) {
                    Text("No orders yet", color = Color.Gray)
                } else {
                    totalCounts.forEach { (menuId, count) ->
                        val name =
                            menu.find { it.id == menuId }?.name ?: "Unknown Item"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    name,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Medium
                                )
                                Surface(
                                    color = Color(0xFFE23744),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        "x$count",
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 4.dp
                                        ),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }

            /* ---------- USER ORDERS ---------- */

            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    "User Orders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(selections) { sel ->

                LaunchedEffect(sel.userId) {
                    vm.loadUserName(sel.userId)
                }

                val userName = vm.userNameCache[sel.userId] ?: "Loading..."

                val selectedNames = sel.items.map { id ->
                    menu.find { it.id == id }?.name ?: "Unknown"
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text(
                            userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        selectedNames.forEach { item ->
                            Text("• $item")
                        }
                    }
                }
            }
        }
    }
}
