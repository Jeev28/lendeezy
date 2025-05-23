package com.example.lendeezy.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.lendeezy.ui.viewmodel.UserState
import com.example.lendeezy.ui.viewmodel.UserViewModel
import coil.compose.AsyncImage

/**
 * Shows user details currently
 * Later change to be full list of products
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: UserViewModel, padding: PaddingValues) {
    val userState by viewModel.userState.collectAsState()
    val user = (userState as? UserState.Success)?.user

    // for search and filters
    var searchQuery by remember { mutableStateOf("") }
    val filters = listOf("All", "Available", "Borrowed")
    var selectedFilter by remember { mutableStateOf(filters.first())}

    /**
     * Column for home page with:
     * 1. Search
     * 2. Filter badges
     * 3. Grid of products
     */
    Column(modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .padding(24.dp)
        .padding(padding)
        .fillMaxSize()

    ) {

        //=== Search Bar ===
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search items...") },
            leadingIcon = {
                          Icon(
                              imageVector = Icons.Default.Search,
                              contentDescription = "Search",
                              tint = Color.Black,
                          )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFF3EDE6)),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color(0xFFF7F7F7),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            singleLine = true,
            shape = RoundedCornerShape(50),
        )

        //=== Spacer ===
        Spacer(modifier = Modifier.height(12.dp))

        //=== Filter badges ===
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filters.size) { i ->
                val filter = filters[i]
                FilterChip(
                    selected = (filter == selectedFilter),
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        selectedContainerColor = Color.White,
                        labelColor = Color.Black,
                        selectedLabelColor = Color.Black
                    ),
                    border = BorderStroke(
                        width = if (filter == selectedFilter) 2.dp else 1.dp,
                        color = if (filter == selectedFilter) Color.Black else Color(0xFFA2A2A2)
                    )

                )
            }
        }

        //=== Spacer ===
        Spacer(modifier = Modifier.height(24.dp))

        //=== Grid of products ===
        Text(
            text = "Product grid here",
            style = MaterialTheme.typography.bodyLarge
        )


    }
}
