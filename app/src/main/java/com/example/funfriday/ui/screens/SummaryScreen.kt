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

    /* ---------- LOAD DATA ---------- */

    LaunchedEffect(cardId) {
        vm.loadMenu(cardId)
        vm.loadSelections(cardId)
        vm.getCard(cardId) { fetched ->
            card = fetched
        }
    }

    /* ---------- LOADING ---------- */

    if (card == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    /* ---------- ACCESS CONTROL ---------- */

    if (card?.createdBy != currentUserId) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Only the creator can view the summary")
        }
        return
    }

    /* ---------- TOTAL ITEM COUNTS ---------- */

    val totalCounts: Map<String, Int> =
        selections
            .flatMap { it.items.entries }
            .groupingBy { it.key }
            .fold(0) { acc, entry -> acc + entry.value }

    /* ---------- GRAND TOTAL ---------- */

    val grandTotal: Double = totalCounts.entries.sumOf { entry ->
        val price = menu.firstOrNull { it.id == entry.key }?.price ?: 0.0
        price * entry.value
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /* ---------- TOTAL ORDERS ---------- */

            item {
                Text(
                    "Total Orders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                totalCounts.forEach { (menuId, count) ->
                    val item = menu.firstOrNull { it.id == menuId }
                    val price = item?.price ?: 0.0
                    val subtotal = price * count

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                item?.name ?: "Unknown Item",
                                fontWeight = FontWeight.Medium
                            )
                            Text("₹ $price × $count = ₹ $subtotal")
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "Grand Total: ₹ $grandTotal",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE23744)
                )
            }

            /* ---------- USER ORDERS ---------- */

            item {
                Spacer(Modifier.height(16.dp))
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

                val userItemCounts = sel.items

                val userTotal = userItemCounts.entries.sumOf { entry ->
                    val price = menu.firstOrNull { it.id == entry.key }?.price ?: 0.0
                    price * entry.value
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {

                        Text(
                            userName,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(6.dp))

                        userItemCounts.forEach { (menuId, qty) ->
                            val item = menu.firstOrNull { it.id == menuId }
                            val price = item?.price ?: 0.0
                            val subtotal = price * qty

                            Text("• ${item?.name}  x$qty  = ₹ $subtotal")
                        }

                        Spacer(Modifier.height(8.dp))
                        Divider()
                        Spacer(Modifier.height(6.dp))

                        Text(
                            "Total: ₹ $userTotal",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE23744)
                        )
                    }
                }
            }
        }
    }
}
