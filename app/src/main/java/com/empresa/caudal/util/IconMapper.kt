package com.empresa.caudal.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {

    val availableIcons: Map<String, ImageVector> = mapOf(
        "food" to Icons.Rounded.Restaurant,
        "transport" to Icons.Rounded.DirectionsBus,
        "utilities" to Icons.Rounded.Bolt,
        "leisure" to Icons.Rounded.Movie,
        "health" to Icons.Rounded.Favorite,
        "home" to Icons.Rounded.Home,
        "shopping" to Icons.Rounded.ShoppingCart,
        "education" to Icons.Rounded.School,
        "salary" to Icons.Rounded.Payments,
        "work" to Icons.Rounded.Work,
        "wallet" to Icons.Rounded.AccountBalanceWallet,
        "other" to Icons.Rounded.Category
    )

    fun getIcon(key: String): ImageVector =
        availableIcons[key] ?: Icons.Rounded.Category
}