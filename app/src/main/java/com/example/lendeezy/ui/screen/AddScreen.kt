package com.example.lendeezy.ui.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddScreen(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(padding)
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Add Product Screen (Placeholder)")
    }
}