package com.example.funfriday.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.example.funfriday.data.models.MenuItem
import com.example.funfriday.viewmodel.LunchViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun CardDetailScreen(
    nav: NavController,
    cardId: String,
    vm: LunchViewModel = viewModel()
) {

    val menu = vm.menu
    var card by remember { mutableStateOf<LunchCard?>(null) }

    val currentUid = Firebase.auth.currentUser?.uid
    val isCreator = card?.createdBy == currentUid

    // menuId -> quantity (ZERO IS ALLOWED)
    var selectedItems by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    // manual add
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    // json import
    var jsonText by remember { mutableStateOf("") }
    var jsonError by remember { mutableStateOf<String?>(null) }

    // delete menu dialog
    var deleteItem by remember { mutableStateOf<MenuItem?>(null) }

    /* ---------------- LOAD DATA ---------------- */

    LaunchedEffect(cardId) {
        vm.loadMenu(cardId)
        vm.getCard(cardId) { card = it }

        // load existing cart for user
        vm.loadUserSelection(cardId) { selection ->
            if (selection != null) {
                selectedItems = selection.items
            }
        }
    }

    /* ---------------- UI ---------------- */

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        /* ---------- TITLE ---------- */
        item {
            Text(
                card?.title ?: "Lunch Menu",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        /* ---------- CREATOR : ADD MENU ---------- */
        if (isCreator) {
            item {
                Card {
                    Column(Modifier.padding(12.dp)) {

                        Text("Add Menu Manually", fontWeight = FontWeight.Bold)

                        OutlinedTextField(name, { name = it }, label = { Text("Name") })
                        OutlinedTextField(category, { category = it }, label = { Text("Category") })
                        OutlinedTextField(price, { price = it }, label = { Text("Price") })
                        OutlinedTextField(imageUrl, { imageUrl = it }, label = { Text("Image URL") })

                        Spacer(Modifier.height(8.dp))

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                if (name.isBlank()) return@Button
                                vm.addMenu(
                                    cardId,
                                    name,
                                    category,
                                    price.toDoubleOrNull() ?: 0.0,
                                    imageUrl
                                ) {
                                    name = ""
                                    category = ""
                                    price = ""
                                    imageUrl = ""
                                }
                            }
                        ) {
                            Text("Add Item")
                        }
                    }
                }
            }
        }

        /* ---------- CREATOR : JSON IMPORT ---------- */
        if (isCreator) {
            item {
                Card {
                    Column(Modifier.padding(12.dp)) {

                        Text("Import Menu via JSON", fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = jsonText,
                            onValueChange = { jsonText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = {
                                Text(
                                    """[
  {"name":"Biriyani","category":"Main","price":180,"imageUrl":""}
]"""
                                )
                            }
                        )

                        jsonError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                vm.uploadMenuFromJsonText(
                                    cardId,
                                    jsonText,
                                    onSuccess = {
                                        jsonText = ""
                                        jsonError = null
                                    },
                                    onError = { jsonError = it }
                                )
                            }
                        ) {
                            Text("Import JSON")
                        }
                    }
                }
            }
        }

        /* ---------- MENU LIST ---------- */
        item {
            Text("Menu Items", style = MaterialTheme.typography.titleLarge)
        }

        items(menu) { item ->
            MenuRowWithQuantity(
                item = item,
                quantity = selectedItems[item.id] ?: 0,
                onIncrement = {
                    selectedItems =
                        selectedItems + (item.id to ((selectedItems[item.id] ?: 0) + 1))
                },
                onDecrement = {
                    val current = selectedItems[item.id] ?: 0
                    selectedItems =
                        selectedItems + (item.id to maxOf(0, current - 1))
                },
                showDelete = isCreator,
                onDelete = { deleteItem = item }
            )
            Divider()
        }

        /* ---------- ADD TO CART ---------- */
        if (selectedItems.isNotEmpty()) {
            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        vm.saveUserSelection(cardId, selectedItems) {
                            nav.popBackStack()
                        }
                    }
                ) {
                    Text("Add to Cart (${selectedItems.values.sum()} items)")
                }
            }
        }

        /* ---------- VIEW SUMMARY ---------- */
        if (isCreator) {
            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { nav.navigate("summary/$cardId") }
                ) {
                    Text("📊 View Summary")
                }
            }
        }
    }

    /* ---------- DELETE MENU DIALOG ---------- */
    deleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteItem = null },
            title = { Text("Delete Item") },
            text = { Text("Delete ${item.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteMenu(cardId, item.id)
                    deleteItem = null
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteItem = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/* ---------------- MENU ROW ---------------- */

@Composable
fun MenuRowWithQuantity(
    item: MenuItem,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    showDelete: Boolean,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.Bold)
            Text(item.category)
            Text("₹ ${item.price}")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {

            IconButton(onClick = onDecrement) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease")
            }

            Text(
                quantity.toString(),
                modifier = Modifier.padding(horizontal = 6.dp),
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onIncrement) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase")
            }
        }

        if (showDelete) {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Item",
                    tint = Color.Red
                )
            }
        }
    }
}
