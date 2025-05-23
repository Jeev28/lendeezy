package com.example.lendeezy.ui.nav

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun NavBar(navController: NavController) {
    // list of screens that can be accessed from top level of app
    val navItems = listOf(
        BottomNavItem("Home", "home", Icons.Filled.Home),
        BottomNavItem("Add", "add", Icons.Filled.Add),
        BottomNavItem("User", "user", Icons.Filled.Person)
    )

    val navEntry = navController.currentBackStackEntryAsState().value
    val currentPage = navEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(
                width = 1.dp,
                color = Color(0xFFD9D9D9),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
    ) {
        // for each nav item
        NavigationBar(
            containerColor = Color(0xFFF7F7F7),
        ) {
            navItems.forEach { item ->
                NavigationBarItem(
                    icon = { Icon(imageVector = item.icon, contentDescription = item.name) },
                    label = { Text(item.name) },
                    selected = currentPage?.route == item.route,
                    onClick = {
                        if (currentPage?.route != item.route) {
                            navController.navigate(item.route) {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.Black,
                        unselectedTextColor = Color.Black,
                        indicatorColor = Color.Black.copy(alpha = 0.6f)
                    )
                )
            }
        }


    }



}
