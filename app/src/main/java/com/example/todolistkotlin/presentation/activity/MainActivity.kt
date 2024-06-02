package com.example.todolistkotlin.presentation.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.todolistkotlin.R
import com.example.todolistkotlin.databinding.ActivityMainBinding
import com.example.todolistkotlin.presentation.fragment.CategoryFragmentDirections
import com.example.todolistkotlin.presentation.fragment.CreateCategoryFragment
import com.example.todolistkotlin.util.toInvisible
import com.example.todolistkotlin.util.toVisible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialWork()
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    showBottomNav(true)
                }
                R.id.categoryFragment -> {
                    showBottomNav(true)
                }
                R.id.calendarFragment -> {
                    showBottomNav(true)
                }
                R.id.profileFragment -> {
                    showBottomNav(true)
                }
                else -> {
                    showBottomNav(false)
                }
            }
        }
    }

    private fun showBottomNav(show: Boolean) {
        if (show) {
            binding.bottomNavMain.toVisible()
        } else {
            binding.bottomNavMain.toInvisible()
        }
    }

    private fun initialWork() {
        binding.bottomNavMain.background = null

        val navHostFragment =
            (supportFragmentManager.findFragmentById(binding.fragmentContainerView.id)) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavMain.setupWithNavController(navController)
    }
}




