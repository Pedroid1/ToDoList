package com.example.todolistkotlin.presentation.adapter.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistkotlin.R
import com.example.todolistkotlin.databinding.IconLayoutBinding

class IconItemAdapter(private var iconList: List<Int>) :
    RecyclerView.Adapter<IconItemAdapter.IconViewHolder>() {

    private var positionSelected: Int = 0
    private lateinit var onIconSelectedListener: onIconSelected

    class IconViewHolder(var binding: IconLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        return IconViewHolder(
            IconLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        if (positionSelected == holder.adapterPosition) {
            holder.binding.rootCard.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.light_grey_2
                )
            )
            onIconSelectedListener.onIconSelected(iconList[position])
        } else {
            holder.binding.rootCard.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.md_theme_light_primary
                )
            )
        }

        holder.binding.imgCategory.setImageResource(iconList[position])

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
            onIconSelectedListener.onIconSelected(iconList[position])
        }
    }

    override fun getItemCount(): Int {
        return iconList.size
    }

    interface onIconSelected {
        fun onIconSelected(icon: Int)
    }

    fun setOnIconSelectedListener(onItemSelected: onIconSelected) {
        this.onIconSelectedListener = onItemSelected
    }
}