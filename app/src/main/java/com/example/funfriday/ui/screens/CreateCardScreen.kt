package com.example.funfriday.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.funfriday.viewmodel.LunchViewModel

@Composable
fun CreateCardScreen(
    nav: NavController,
    vm: LunchViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }

    val loading = vm.loading.value
    val error = vm.error.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            "Create Lunch Card",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Card Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank() && !loading,
            onClick = {
                vm.createCard(title) { cardId ->
                    if (cardId != null) {
                        nav.navigate("addMenu/$cardId") {
                            popUpTo("createCard") { inclusive = true }
                        }
                    }
                }
            }
        ) {
            Text(if (loading) "Creating..." else "Create")
        }
    }
}
