package com.example.todolistkotlin.presentation.adapter.recycler

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistkotlin.R
import com.example.todolistkotlin.presentation.model.ColorItem
import com.example.todolistkotlin.databinding.ColorItemBinding

class ColorItemAdapter(private var colorList: List<ColorItem>) :
    RecyclerView.Adapter<ColorItemAdapter.ColorViewHolder>() {

    private var positionSelected: Int = 0
    private lateinit var onColorSelectedListener: onColorSelected

    fun setOnItemSelectedListener(onColorSelected: onColorSelected) {
        this.onColorSelectedListener = onColorSelected
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorViewHolder(
            ColorItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        if (positionSelected == holder.adapterPosition) {
            holder.binding.rootCard.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.light_grey_2
                )
            )
            onColorSelectedListener.onColorSelected(colorList[position])
        } else {
            holder.binding.rootCard.setCardBackgroundColor(Color.parseColor(colorList[position].color))
        }
        holder.binding.linearColor.setBackgroundColor(Color.parseColor(colorList[position].color))


        holder.binding.rootCard.setOnClickListener {
            positionSelected.let {
                val lasPositionSelected = it
                notifyItemChanged(lasPositionSelected)
            }

            positionSelected = holder.adapterPosition
            holder.binding.rootCard.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.light_grey_2
                )
            )
            onColorSelectedListener.onColorSelected(colorList[position])
        }
    }

    override fun getItemCount(): Int {
        return colorList.size
    }

    class ColorViewHolder(binding: ColorItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val binding: ColorItemBinding = binding
    }

    interface onColorSelected {
        fun onColorSelected(colorItem: ColorItem)
    }
}