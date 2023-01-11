package com.waesh.favdish.view.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.waesh.favdish.R
import com.waesh.favdish.databinding.ItemDishLayoutBinding
import com.waesh.favdish.model.entities.FavDish
import com.waesh.favdish.util.Constants
import com.waesh.favdish.view.activities.AddUpdateDishActivity
import com.waesh.favdish.view.fragments.AllDishesFragment
import com.waesh.favdish.view.fragments.FavoriteDishesFragment

class FavDishAdapter(
    private val fragment: Fragment,
    private val itemClickActions: ItemClickActions
) : ListAdapter<FavDish, FavDishAdapter.ViewHolder>(FavDishComparator()) {

    inner class ViewHolder(binding: ItemDishLayoutBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        val ivDishImage = binding.ivDishImage
        val tvDishTitle = binding.tvDishTitle
        val ibMore = binding.ibMore
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemDishLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val favDish = currentList[holder.adapterPosition]
        holder.tvDishTitle.text = favDish.title
        Glide.with(fragment)
            .load(favDish.image)
            .into(holder.ivDishImage)


        when (fragment) {
            is AllDishesFragment -> holder.ibMore.visibility = View.VISIBLE
            is FavoriteDishesFragment -> holder.ibMore.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            itemClickActions.dishDetails(favDish)
        }

        holder.ibMore.setOnClickListener {
            val popup = PopupMenu(fragment.context, holder.ibMore)
            popup.menuInflater.inflate(R.menu.menu_item_adapter, popup.menu)

            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_edit_dish -> {
                        val intent = Intent(fragment.requireActivity(), AddUpdateDishActivity::class.java)
                        intent.putExtra(Constants.DISH_DETAILS_EXTRA, favDish)
                        fragment.requireActivity().startActivity(intent)
                    }
                    R.id.action_delete_dish -> {
                        Log.i("kkkCat", "action delete")
                        itemClickActions.deleteDish(favDish)
                    }
                }
                true
            }
            popup.show()
        }
    }
}

class FavDishComparator : DiffUtil.ItemCallback<FavDish>() {
    override fun areItemsTheSame(oldItem: FavDish, newItem: FavDish): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FavDish, newItem: FavDish): Boolean {
        return oldItem == newItem
    }
}

interface ItemClickActions {
    fun dishDetails(favDish: FavDish)
    fun deleteDish(favDish: FavDish): Boolean
}