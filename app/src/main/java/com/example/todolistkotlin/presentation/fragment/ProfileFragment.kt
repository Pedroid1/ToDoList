package com.example.todolistkotlin.presentation.fragment

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.todolistkotlin.R
import com.example.todolistkotlin.common.Constants.NIGHT_KEY
import com.example.todolistkotlin.databinding.FragmentProfileBinding
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.presentation.states.MainViewState
import com.example.todolistkotlin.presentation.states.UserInfoState
import com.example.todolistkotlin.presentation.viewmodel.MainViewModel
import com.example.todolistkotlin.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var _binding: FragmentProfileBinding
    private val viewModel: MainViewModel by lazy { ViewModelProvider(requireActivity())[MainViewModel::class.java] }
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val networkChecker: NetworkChecker by lazy {
        NetworkChecker(
            ContextCompat.getSystemService(
                requireContext(),
                ConnectivityManager::class.java
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialWork()
        prepareViewListener()
        observer()
    }

    private fun prepareViewListener() {
        _binding.radioButtonDarkMode.setOnClickListener {

            if (isNightMode()) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                editor.putBoolean(NIGHT_KEY, false)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                editor.putBoolean(NIGHT_KEY, true)
            }
            editor.apply()
            _binding.radioButtonDarkMode.isChecked = isNightMode()
        }

        _binding.txtSignOut.setOnClickListener {
            showAlertDialog()
        }
    }

    private fun showAlertDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.sign_out_dialog))
            .setMessage(R.string.sign_out_info)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                Firebase.auth.signOut()
            }
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .show()
    }

    private fun observer() {
        viewLifecycleOwner.apply {
            observe(viewModel.userInfoState, ::handleUserInfoState)
            observe(viewModel.homeViewState, ::handleHomeState)
        }
    }

    private fun initialWork() {
        sharedPreferences = requireActivity().getSharedPreferences("MODE", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        _binding.radioButtonDarkMode.isChecked = isNightMode()
    }

    private fun isNightMode(): Boolean {
        return sharedPreferences.getBoolean(NIGHT_KEY, false)
    }

    private fun handleHomeState(state: MainViewState) {
        if(networkChecker.hasInternet()) {
            populateStatistics(state.taskList)
            _binding.txtWithoutConnection.toInvisible()
        } else {
            _binding.txtWithoutConnection.toVisible()
            underscoreStatistics()
        }
        state.error?.let {
            //TODO - Handle error
        }
    }

    private fun prepareSeeMoreListener() {
        _binding.txtSeeMore.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_statisticsFragment)
        }
    }

    private fun populateStatistics(taskList: List<Task>?) {
        if (taskList != null) {
            val numberOfTasks = taskList.count()
            val numberOfCompleteTasks = taskList.count { it.completed }
            _binding.txtQtdTasks.text = numberOfTasks.toString()
            _binding.txtQtdTasksDone.text = numberOfCompleteTasks.toString()
            if (numberOfTasks > 0) {
                _binding.txtSeeMore.toVisible()
                prepareSeeMoreListener()
            }
        } else {
            underscoreStatistics()
        }
    }

    private fun underscoreStatistics() {
        _binding.txtQtdTasks.text = "-"
        _binding.txtQtdTasksDone.text = "-"
    }

    private fun handleUserInfoState(state: UserInfoState) {
        state.username?.let {
            _binding.name.text = it
        }
        state.email?.let {
            _binding.txtEmail.text = it
        }
        state.imageUrl?.let {
            _binding.imgProfile.loadImage(it.toString())
        }
    }
}