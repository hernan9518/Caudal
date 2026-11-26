package com.empresa.caudal.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {

    val availableIcons: List<Pair<String, ImageVector>> = listOf(
        "food" to Icons.Rounded.Restaurant,
        "coffee" to Icons.Rounded.LocalCafe,
        "transport" to Icons.Rounded.DirectionsBus,
        "bills" to Icons.Rounded.Bolt,
        "internet" to Icons.Rounded.Wifi,
        "entertainment" to Icons.Rounded.Movie,
        "health" to Icons.Rounded.Favorite,
        "gym" to Icons.Rounded.FitnessCenter,
        "pets" to Icons.Rounded.Pets,
        "home" to Icons.Rounded.Home,
        "shopping" to Icons.Rounded.ShoppingCart,
        "clothing" to Icons.Rounded.Checkroom,
        "education" to Icons.Rounded.School,
        "travel" to Icons.Rounded.Flight,
        "tech" to Icons.Rounded.Devices,
        "gift" to Icons.Rounded.CardGiftcard,
        "wellness" to Icons.Rounded.Spa,
        "tools" to Icons.Rounded.Build,
        "salary" to Icons.Rounded.Payments,
        "work" to Icons.Rounded.Work,
        "savings" to Icons.Rounded.Savings,
        "bank" to Icons.Rounded.AccountBalance,
        "wallet" to Icons.Rounded.AccountBalanceWallet,
        "other" to Icons.Rounded.Category
    )

    private val iconMap: Map<String, ImageVector> = availableIcons.toMap()

    fun getIcon(key: String): ImageVector = iconMap[key] ?: Icons.Rounded.Category
}