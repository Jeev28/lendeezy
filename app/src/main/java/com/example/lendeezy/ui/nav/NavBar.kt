package com.example.lendeezy.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
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

    // for each nav item, make it an item in the bottom nav bar
    NavigationBar {
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
                }
            )
        }
    }

}
