package com.dutaeko.shinigamireader.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Home : AppDestination("home", "Home", Icons.Outlined.Home)
    data object Discover : AppDestination("discover", "Discover", Icons.Outlined.Explore)
    data object Library : AppDestination("library", "Library", Icons.Outlined.CollectionsBookmark)
    data object Profile : AppDestination("profile", "Profile", Icons.Outlined.Person)
    data object Detail : AppDestination("detail", "Detail", Icons.Outlined.Book)
    data object Reader : AppDestination("reader", "Reader", Icons.Outlined.MenuBook)
}
