package com.example.funfriday.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.funfriday.data.models.MenuItem
import com.example.funfriday.viewmodel.LunchViewModel

@Composable
fun AddMenuScreen(
    nav: NavController,
    cardId: String,
    vm: LunchViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    val menu = vm.menu
    val loading = vm.loading.value

    var deleteItem by remember { mutableStateOf<MenuItem?>(null) }

    LaunchedEffect(cardId) {
        vm.loadMenu(cardId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Add Menu Item", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        /* -------- INPUTS -------- */

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Image URL") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        /* -------- ADD BUTTON -------- */

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (name.isBlank()) return@Button

                vm.addMenu(
                    cardId = cardId,
                    name = name,
                    category = category,
                    price = price.toDoubleOrNull() ?: 0.0,
                    imageUrl = imageUrl
                ) {
                    name = ""
                    category = ""
                    price = ""
                    imageUrl = ""
                }
            }
        ) {
            Text(if (loading) "Adding..." else "Add Item")
        }

        Spacer(Modifier.height(16.dp))

        /* -------- MENU LIST -------- */

        Text("Menu Items", style = MaterialTheme.typography.titleLarge)

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(menu) { item ->
                MenuItemRow(
                    item = item,
                    onDelete = { deleteItem = item }
                )
                Divider()
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { nav.popBackStack() }
        ) {
            Text("Done")
        }
    }

    /* -------- DELETE CONFIRMATION -------- */

    deleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteItem = null },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete \"${item.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteMenu(cardId, item.id)
                        deleteItem = null
                    }
                ) {
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

@Composable
fun MenuItemRow(
    item: MenuItem,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (item.imageUrl.isNotBlank()) {
            Image(
                painter = rememberAsyncImagePainter(item.imageUrl),
                contentDescription = null,
                modifier = Modifier.size(70.dp)
            )
            Spacer(Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            Text(item.category, style = MaterialTheme.typography.bodySmall)
            if (item.price > 0) {
                Text("₹ ${item.price}", style = MaterialTheme.typography.bodySmall)
            }
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.Red
            )
        }
    }
}
