package com.example.todolistkotlin.presentation.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.todolistkotlin.R
import com.example.todolistkotlin.databinding.CustomHeaderBinding

class CustomHeader(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val binding: CustomHeaderBinding

    init {
        binding = CustomHeaderBinding.inflate(LayoutInflater.from(context), this, true)

        context.obtainStyledAttributes(attrs, R.styleable.CustomHeader).apply {
            this.getString(R.styleable.CustomHeader_headerTitle)?.let {
                setTitle(it)
            }
            this.getString(R.styleable.CustomHeader_leftIconContentDescription)?.let {
                setLeftIconContentDescription(it)
            }
            this.getString(R.styleable.CustomHeader_rightIconContentDescription)?.let {
                setRightIconContentDescription(it)
            }
            setLeftIcon(this.getResourceId(R.styleable.CustomHeader_leftIcon, -1))
            setRightIcon(this.getResourceId(R.styleable.CustomHeader_rightIcon, -1))
        }
    }

    fun setLeftIconListener(listener: OnClickListener) {
        binding.leftIcon.setOnClickListener(listener)
    }

    fun setRightIconListener(listener: OnClickListener) {
        binding.iconRight.setOnClickListener(listener)
    }

    fun setRightIcon(it: Int) {
        if (it != -1) {
            binding.iconRight.setImageResource(it)
        }
    }

    fun setLeftIcon(it: Int) {
        if (it != -1) {
            binding.leftIcon.setImageResource(it)
        }
    }

    fun setRightIconContentDescription(it: String) {
        binding.iconRight.contentDescription = it
    }

    fun setLeftIconContentDescription(it: String) {
        binding.leftIcon.contentDescription = it
    }

    fun setTitle(title: String) {
        binding.txtTitle.text = title
    }
}