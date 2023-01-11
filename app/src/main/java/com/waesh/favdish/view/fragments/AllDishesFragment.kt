package com.waesh.favdish.view.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.waesh.favdish.R
import com.waesh.favdish.application.FavDishApplication
import com.waesh.favdish.databinding.DialogCustomLlistBinding
import com.waesh.favdish.databinding.FragmentAllDishesBinding
import com.waesh.favdish.model.entities.FavDish
import com.waesh.favdish.util.Constants
import com.waesh.favdish.view.activities.AddUpdateDishActivity
import com.waesh.favdish.view.activities.MainActivity
import com.waesh.favdish.view.adapters.CustomItemListAdapter
import com.waesh.favdish.view.adapters.FavDishAdapter
import com.waesh.favdish.view.adapters.ISelectionAction
import com.waesh.favdish.view.adapters.ItemClickActions
import com.waesh.favdish.viewmodel.FavDishViewModel
import com.waesh.favdish.viewmodel.FavDishViewModelFactory

class AllDishesFragment : Fragment(), MenuProvider, ItemClickActions, ISelectionAction {

    private var _binding: FragmentAllDishesBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: Dialog
    private lateinit var adapter: FavDishAdapter

    private val mFavDishViewModel by viewModels<FavDishViewModel> {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllDishesBinding.inflate(inflater, container, false)

        adapter = FavDishAdapter(this@AllDishesFragment, this)
        binding.apply {
            rvAllDishes.layoutManager = GridLayoutManager(requireActivity(), 2)
            rvAllDishes.adapter = adapter
        }
        mFavDishViewModel.setFilterCategory(getString(R.string.all_dishes))

        mFavDishViewModel.filterCategory.observe(viewLifecycleOwner){
            (activity as AppCompatActivity).supportActionBar?.title = it
        }

        attachObserver()

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        return binding.root
    }
    

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivity) {
            (activity as MainActivity).showBottomNavigationView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_add_dishes, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_add_dish -> {
                startActivity(Intent(requireActivity(), AddUpdateDishActivity::class.java))
                true
            }
            R.id.action_filter_dishes -> {
                showFilterDialog()
            }
            else -> false
        }
    }

    private fun attachObserver() {
        val allDish = getString(R.string.all_dishes)
        if (mFavDishViewModel.filterCategory.value == allDish) {
            mFavDishViewModel.getDishesByCategory(mFavDishViewModel.filterCategory.value!!)
                .removeObservers(viewLifecycleOwner)
            mFavDishViewModel.allDishesList.observe(viewLifecycleOwner) {
                if (it.isEmpty()) {
                    binding.tvNoItem.visibility = View.VISIBLE
                } else {
                    binding.tvNoItem.visibility = View.GONE
                }
                adapter.submitList(it)
            }
        } else {
            mFavDishViewModel.allDishesList.removeObservers(viewLifecycleOwner)
            mFavDishViewModel.getDishesByCategory(mFavDishViewModel.filterCategory.value!!)
                .observe(viewLifecycleOwner) {
                    if (it.isEmpty()) {
                        binding.tvNoItem.visibility = View.VISIBLE
                    } else {
                        binding.tvNoItem.visibility = View.GONE
                    }
                    adapter.submitList(it)
                }

        }
    }

    private fun showFilterDialog(): Boolean {
        dialog = Dialog(requireActivity())
        val dialogBinding = DialogCustomLlistBinding.inflate(layoutInflater)

        dialog.setContentView(dialogBinding.root)

        dialogBinding.tvTitle.text = resources.getString(R.string.title_select_item_for_filter)
        dialogBinding.rvList.layoutManager = LinearLayoutManager(requireActivity())
        val categories = resources.getStringArray(R.array.dishCategory).toMutableList()
        categories.add(resources.getString(R.string.all_dishes))
        dialogBinding.rvList.adapter = CustomItemListAdapter(
            this,
            categories,
            null
        )

        dialog.show()

        return true
    }

    override fun dishDetails(favDish: FavDish) {
        findNavController()
            .navigate(
                AllDishesFragmentDirections.actionNavigationAllDishesToNavigationDishDetails(
                    favDish
                )
            )

        if (activity is MainActivity) {
            (activity as MainActivity).hideBottomNavigationView()
        }
    }

    override fun deleteDish(favDish: FavDish): Boolean {
        val alert = AlertDialog.Builder(requireActivity())

        alert.setTitle(resources.getString(R.string.alert_delete_title))
            .setMessage(resources.getString(R.string.alert_delete_message, favDish.title))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(true)
            .setPositiveButton(resources.getString(R.string.alert_delete_positive_text)) { _, _ ->
                mFavDishViewModel.deleteDish(favDish)
                Snackbar.make(
                    binding.clParent,
                    resources.getString(R.string.snackbar_undo_message, favDish.title),
                    Snackbar.LENGTH_LONG
                )
                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                    .setAction(resources.getString(R.string.snackbar_action_text)) {
                        mFavDishViewModel.undoDelete()
                        Toast.makeText(requireActivity(), getString(R.string.toast_item_restored), Toast.LENGTH_SHORT)
                            .show()
                    }
                    .show()
            }
            .setNegativeButton(resources.getString(R.string.alert_delete_negative_text)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .show()
        return true
    }

    override fun onItemClick(itemText: String, position: Int, viewId: Int?): Boolean {
        dialog.dismiss()
        mFavDishViewModel.setFilterCategory(Constants.DISH_CATEGORIES[position])
        attachObserver()
        return true
    }
}