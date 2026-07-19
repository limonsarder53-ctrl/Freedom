package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.FreedomRepository
import com.example.ui.screens.MainAppContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FreedomViewModel
import com.example.ui.viewmodel.FreedomViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getDatabase(applicationContext)
    val repository = FreedomRepository(database.freedomDao())
    val factory = FreedomViewModelFactory(application, repository)
    val viewModel = ViewModelProvider(this, factory)[FreedomViewModel::class.java]

    setContent {
      val user by viewModel.activeUser.collectAsState()
      val darkTheme = user?.isDarkMode ?: true

      MyApplicationTheme(darkTheme = darkTheme) {
        MainAppContainer(viewModel = viewModel)
      }
    }
  }
}
