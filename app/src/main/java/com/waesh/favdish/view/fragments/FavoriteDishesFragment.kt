package com.waesh.favdish.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.waesh.favdish.application.FavDishApplication
import com.waesh.favdish.databinding.FragmentFavoriteDishesBinding
import com.waesh.favdish.model.entities.FavDish
import com.waesh.favdish.view.activities.MainActivity
import com.waesh.favdish.view.adapters.FavDishAdapter
import com.waesh.favdish.view.adapters.ItemClickActions
import com.waesh.favdish.viewmodel.FavDishViewModel
import com.waesh.favdish.viewmodel.FavDishViewModelFactory

class FavoriteDishesFragment : Fragment(), ItemClickActions {

    private var _binding: FragmentFavoriteDishesBinding? = null
    private val binding get() = _binding!!

    private val mFavDishViewModel by viewModels<FavDishViewModel> {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteDishesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val adapter = FavDishAdapter(this, this)
        binding.apply {
            rvFavoriteDishes.layoutManager = GridLayoutManager(requireActivity(), 2)
            rvFavoriteDishes.adapter = adapter
        }

        mFavDishViewModel.favoriteDishes.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.tvNoItem.visibility = View.VISIBLE
            } else {
                binding.tvNoItem.visibility = View.GONE
                adapter.submitList(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(requireActivity() is MainActivity){
            (activity as MainActivity).showBottomNavigationView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun dishDetails(favDish: FavDish) {
        findNavController()
            .navigate(
                FavoriteDishesFragmentDirections.actionNavigationFavoriteDishesToNavigationDishDetails(
                    favDish
                )
            )

        if (activity is MainActivity) {
            (activity as MainActivity).hideBottomNavigationView()
        }
    }

    override fun deleteDish(favDish: FavDish): Boolean {
        return false
    }
}