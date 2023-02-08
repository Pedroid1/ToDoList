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
        prepareViewListener();
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    showBottomNav(true)
                    showFloatingButton(true)
                    binding.floatingBtn.contentDescription = getString(R.string.add_task)
                }
                R.id.categoryFragment -> {
                    showBottomNav(true)
                    showFloatingButton(true)
                    binding.floatingBtn.contentDescription = getString(R.string.add_category_accessbility)
                }
                R.id.calendarFragment -> {
                    showBottomNav(true)
                    showFloatingButton(true)
                    binding.floatingBtn.contentDescription = getString(R.string.add_task)
                }
                R.id.profileFragment -> {
                    showBottomNav(true)
                    showFloatingButton(false)
                }
                else -> {
                    showBottomNav(false)
                    showFloatingButton(false)
                }
            }
        }
    }

    private fun showFloatingButton(show: Boolean) {
        if (show) {
            binding.floatingBtn.show()
        } else {
            binding.floatingBtn.hide()
        }
    }

    private fun showBottomNav(show: Boolean) {
        if (show) {
            binding.bottomNavMain.toVisible()
        } else {
            binding.bottomNavMain.toInvisible()
        }
    }

    private fun prepareViewListener() {
        binding.floatingBtn.setOnClickListener {
            when (binding.bottomNavMain.selectedItemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.action_homeFragment_to_createTaskFragment)
                }
                R.id.categoryFragment -> {
                    val action = CategoryFragmentDirections.actionCategoryFragmentToCreateCategoryFragment(CreateCategoryFragment.CATEGORY_ROOT_SCREEN)
                    navController.navigate(action)
                }
                R.id.calendarFragment -> {
                    navController.navigate(R.id.action_calendarFragment_to_createTaskFragment)
                }
            }
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




