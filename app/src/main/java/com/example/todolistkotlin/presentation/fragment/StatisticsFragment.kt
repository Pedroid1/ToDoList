package com.example.todolistkotlin.presentation.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.todolistkotlin.databinding.FragmentStatisticsBinding
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.presentation.states.HomeViewState
import com.example.todolistkotlin.presentation.viewmodel.HomeViewModel
import com.example.todolistkotlin.util.observe
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import android.R.attr.colorBackground
import com.example.todolistkotlin.R
import com.example.todolistkotlin.presentation.states.CategoryViewState
import com.example.todolistkotlin.presentation.viewmodel.CategoryViewModel
import com.example.todolistkotlin.util.Utils
import com.google.android.material.R.attr.colorOnBackground
import com.google.android.material.color.MaterialColors


class StatisticsFragment : Fragment() {

    private lateinit var _binding: FragmentStatisticsBinding
    private val homeViewModel: HomeViewModel by lazy { ViewModelProvider(requireActivity())[HomeViewModel::class.java] }
    private val categoryViewModel: CategoryViewModel by lazy { ViewModelProvider(requireActivity())[CategoryViewModel::class.java] }

    private var font: Typeface? = null
    private var textColor: Int? = null
    private var holeColor: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialWork()
        prepareViewListener()
        observer()
    }

    private fun prepareViewListener() {
        _binding.apply {
            header.apply {
                this.setLeftIconListener {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }

            chipTask.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    populateTaskChart(homeViewModel.taskList)
                }
            }

            chipCategory.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    populateCategoryChart(categoryViewModel.categoriesList, homeViewModel.taskList)
                }
            }
        }
    }

    private fun observer() {
        viewLifecycleOwner.observe(homeViewModel.homeViewState, ::handleMainViewState)
        viewLifecycleOwner.observe(categoryViewModel.categoryViewState, ::handleCategoryViewState)
    }

    private fun handleMainViewState(homeViewState: HomeViewState) {
        if (homeViewState.taskList != null) {
            populateTaskChart(homeViewState.taskList)
        }
        homeViewState.error?.let {
            //TODO
        }
    }
    private fun handleCategoryViewState(categoryViewState: CategoryViewState) {
        categoryViewState.error?.let {
            //TODO
        }
    }

    private fun populateTaskChart(taskList: List<Task>) {
        val entries = ArrayList<PieEntry>()
        val totalTasks = taskList.count()
        val openTasks = taskList.count { !it.completed }
        val taskDone = taskList.count { it.completed }
        entries.add(PieEntry((openTasks.toFloat() / totalTasks) * 100, getString(R.string.open_tasks)))
        entries.add(PieEntry((taskDone.toFloat() / totalTasks) * 100, getString(R.string.completed_tasks)))

        val pieDataSet = getPieDataSet(entries, "")
        val pieData = PieData(pieDataSet)
        pieData.apply {
            setValueFormatter(PercentFormatter(_binding.pieChart))
            setValueTextSize(11f)
            textColor?.apply { setValueTextColor(textColor!!) }
            font?.apply { setValueTypeface(font) }
        }
        _binding.pieChart.apply {
            data = pieData
            highlightValues(null)
            invalidate()
            centerText = "Tarefas"
            animateY(1000, Easing.EaseInOutQuad)
        }
    }

    private fun populateCategoryChart(categoryList: List<Category>, taskList: List<Task>) {
        val entries = ArrayList<PieEntry>()
        val totalTasks = taskList.count()
        categoryList.forEach {
            val qtdTaskWithThisCategory: Int = getQtdTaskWithCategory(it.id, taskList)
            entries.add(
                PieEntry(
                    (qtdTaskWithThisCategory.toFloat() / totalTasks) * 100, it.name
                )
            )
        }

        val pieDataSet = getPieDataSet(entries, "")
        val pieData = PieData(pieDataSet)
        pieData.apply {
            setValueFormatter(PercentFormatter(_binding.pieChart))
            setValueTextSize(11f)
            textColor?.apply { setValueTextColor(textColor!!) }
            font?.apply { setValueTypeface(font) }
        }
        _binding.pieChart.apply {
            data = pieData
            highlightValues(null)
            invalidate()
            centerText = "Categorias"
            animateY(1000, Easing.EaseInOutQuad)
        }
    }

    private fun getQtdTaskWithCategory(id: String, taskList: List<Task>): Int {
        return taskList.count { it.category?.id == id }
    }

    private fun getPieDataSet(entries: java.util.ArrayList<PieEntry>, label: String): PieDataSet {
        val dataSet = PieDataSet(entries, label)

        dataSet.apply {
            setDrawIcons(false)
            sliceSpace = 3f
            iconsOffset = MPPointF(0f, 40f)
            selectionShift = 5f

            val colors = ArrayList<Int>()
            if (!Utils.isNightMode(requireActivity())) {
                for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
                for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)
                for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
                for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)
            } else {
                for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
            }


            colors.add(ColorTemplate.getHoloBlue())
            this.colors = colors
        }
        return dataSet
    }


    private fun initialWork() {
        _binding.categoryViewModelXml = categoryViewModel
        _binding.lifecycleOwner = viewLifecycleOwner

        font = ResourcesCompat.getFont(requireContext(), R.font.poppins_medium)
        textColor = MaterialColors.getColor(_binding.root, colorOnBackground)
        holeColor = MaterialColors.getColor(_binding.root, colorBackground)
        setupDefaultChart()
    }

    private fun setupDefaultChart() {
        _binding.pieChart.apply {
            setExtraOffsets(10f, -50f, 10f, 0f)
            setUsePercentValues(true)
            description.isEnabled = false
            dragDecelerationFrictionCoef = 0.95f
            font?.let {
                setCenterTextTypeface(it)
                setEntryLabelTypeface(it)
            }
            textColor?.apply {
                setEntryLabelColor(this)
                setCenterTextColor(this)
            }
            holeColor?.let {
                setHoleColor(it)
            }
            isDrawHoleEnabled = true
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true

            val l: Legend = _binding.pieChart.legend
            l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            l.orientation = Legend.LegendOrientation.HORIZONTAL
            l.setDrawInside(false)
            textColor?.let { l.textColor = it }
            l.xEntrySpace = 7f
            l.yEntrySpace = 0f
            setEntryLabelTextSize(12f)
        }
    }
}