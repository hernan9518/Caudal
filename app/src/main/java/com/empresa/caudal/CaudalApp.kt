package com.empresa.caudal

import android.app.Application
import com.empresa.caudal.data.local.CaudalDatabase
import com.empresa.caudal.data.local.UserPreferences
import com.empresa.caudal.data.repository.CaudalRepository

class CaudalApp : Application() {

    val database: CaudalDatabase by lazy {
        CaudalDatabase.getInstance(this)
    }

    val userPreferences: UserPreferences by lazy {
        UserPreferences(this)
    }

    val repository: CaudalRepository by lazy {
        CaudalRepository(
            transactionDao = database.transactionDao(),
            categoryDao = database.categoryDao(),
            userPreferences = userPreferences
        )
    }
}