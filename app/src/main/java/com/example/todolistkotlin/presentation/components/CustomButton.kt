package com.example.todolistkotlin.presentation.components

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.todolistkotlin.R
import com.google.android.material.color.MaterialColors

class CustomButton(context: Context, attrs: AttributeSet) : AppCompatButton(context, attrs) {

    init {
        setupDefaultButton()
        context.obtainStyledAttributes(attrs, R.styleable.CustomButton).apply {
            setButtonPriority(EnumViewPriority.getViewPriority(this.getInt(R.styleable.CustomButton_buttonPriority, -1)))
        }
    }

    private fun setButtonPriority(priority: EnumViewPriority) {
        when (priority) {
            EnumViewPriority.PRIMARY -> setupPrimaryButton()
            EnumViewPriority.SECONDARY -> setupSecondaryButton()
        }
    }

    private fun setupSecondaryButton() {
        this.background = ContextCompat.getDrawable(context, R.drawable.button_with_border)
        this.setTextColor(MaterialColors.getColor(this, android.R.attr.colorPrimary))
    }

    private fun setupPrimaryButton() {
        this.background = ContextCompat.getDrawable(context, R.drawable.blue_button)
    }

    private fun setupDefaultButton() {
        this.isAllCaps = true
        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        this.letterSpacing = 0.01f
        this.setTextColor(ContextCompat.getColor(context, R.color.white))
        val typefaceFont = ResourcesCompat.getFont(context, R.font.poppins_semibold)
        this.typeface = typefaceFont;
    }

    enum class EnumViewPriority {
        PRIMARY,
        SECONDARY;

        companion object {
            @JvmStatic
            fun getViewPriority(index: Int): EnumViewPriority {
                val priorities = values();
                return if(index >= priorities[0].ordinal && index < priorities.size) priorities[index] else PRIMARY
            }
        }
    }
}