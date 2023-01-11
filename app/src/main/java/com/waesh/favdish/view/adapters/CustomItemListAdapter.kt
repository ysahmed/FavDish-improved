package com.waesh.favdish.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.waesh.favdish.databinding.ItemCustomListBinding

class CustomItemListAdapter(private val selectionAction: ISelectionAction ,private val list: List<String>, private val viewId: Int?): Adapter<CustomItemListAdapter.ViewHolder>() {

    class ViewHolder(binding: ItemCustomListBinding): RecyclerView.ViewHolder(binding.root){
        val titleTextView = binding.tvTitleText
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemCustomListBinding = ItemCustomListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)
        binding.root.setOnClickListener(){
            selectionAction.onItemClick(list[holder.adapterPosition], holder.adapterPosition, viewId)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titleTextView.text = list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }
}

interface ISelectionAction{
    fun onItemClick(itemText: String, position: Int, viewId: Int?): Boolean
}