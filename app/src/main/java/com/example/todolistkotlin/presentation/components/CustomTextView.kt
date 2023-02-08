package com.example.todolistkotlin.presentation.components

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import com.example.todolistkotlin.R
import com.google.android.material.R.attr.colorOnBackground
import com.google.android.material.R.attr.colorOnSurfaceVariant
import com.google.android.material.color.MaterialColors

class CustomTextView(context: Context, attrs: AttributeSet) : AppCompatTextView(context, attrs) {

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CustomTextView).apply {
            setTextType(EnumTextType.getTextType(this.getInt(R.styleable.CustomTextView_textType, EnumTextType.BODY1.ordinal)))
            setupTitleAccessibility(this.getBoolean(R.styleable.CustomTextView_isTitle, false))
            setupPriorityText(EnumTextViewPriority.getTextPriority(this.getInt(R.styleable.CustomTextView_textPriority, EnumTextViewPriority.SECONDARY.ordinal)))
        }
        this.letterSpacing = 0.05f
    }

    private fun setupPriorityText(textPriority: EnumTextViewPriority) {
        when(textPriority) {
            EnumTextViewPriority.PRIMARY -> {
                this.setTextColor(MaterialColors.getColor(this, colorOnBackground))
            }
            EnumTextViewPriority.SECONDARY -> {
                //PATTERN COLOR
            }
            EnumTextViewPriority.TERTIARY -> {
                this.setTextColor(MaterialColors.getColor(this, colorOnSurfaceVariant))
            }
        }
    }

    private fun setupTitleAccessibility(isTitle: Boolean) {
        ViewCompat.setAccessibilityHeading(this, isTitle);
    }

    private fun setTextType(type: EnumTextType) {
        when (type) {
            EnumTextType.TITLE1 -> {
                val font = ResourcesCompat.getFont(this.context, R.font.poppins_semibold)
                this.typeface = font
                this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            }
            EnumTextType.TITLE2 -> {
                val font = ResourcesCompat.getFont(this.context, R.font.poppins_semibold)
                this.typeface = font
                this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            }
            EnumTextType.TITLE3 -> {
                val font = ResourcesCompat.getFont(context, R.font.poppins_semibold)
                this.typeface = font
                this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            }
            EnumTextType.SUBTITLE1 -> {
                val font = ResourcesCompat.getFont(context, R.font.poppins_medium);
                this.typeface = font
                this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            }
            EnumTextType.SUBTITLE2 -> {
                val font = ResourcesCompat.getFont(context, R.font.poppins_medium)
                this.typeface = font
                this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            }
            EnumTextType.HEADER -> {
                val font = ResourcesCompat.getFont(context, R.font.poppins_semibold);
                this.typeface = font
                this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            }
            EnumTextType.BODY1 -> {
                val font = ResourcesCompat.getFont(context, R.font.poppins_regular)
                this.typeface = font
                this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }
            EnumTextType.BODY2 -> {
                val font = ResourcesCompat.getFont(context, R.font.poppins_regular)
                this.typeface = font
                this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            }
            EnumTextType.CAPTION -> {
                val font = ResourcesCompat.getFont(context, R.font.poppins_semibold)
                this.typeface = font
                this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            }
            EnumTextType.OVERLINE -> {
                val font = ResourcesCompat.getFont(context, R.font.poppins_regular)
                this.typeface = font
                this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                isAllCaps = true
            }
        }
    }

    enum class EnumTextType {
        TITLE1,
        TITLE2,
        TITLE3,
        SUBTITLE1,
        SUBTITLE2,
        HEADER,
        BODY1,
        BODY2,
        CAPTION,
        OVERLINE;

        companion object {
            @JvmStatic
            fun getTextType(index: Int): EnumTextType {
                return values()[index]
            }
        }
    }

    enum class EnumTextViewPriority {
        PRIMARY,
        SECONDARY,
        TERTIARY;

        companion object {
            @JvmStatic
            fun getTextPriority(index: Int): EnumTextViewPriority {
                return values()[index]
            }
        }
    }
}