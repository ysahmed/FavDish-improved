package com.waesh.favdish.view.fragments

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.waesh.favdish.R
import com.waesh.favdish.application.FavDishApplication
import com.waesh.favdish.databinding.FragmentRandomDishBinding
import com.waesh.favdish.model.entities.FavDish
import com.waesh.favdish.model.entities.RandomDish
import com.waesh.favdish.util.Constants
import com.waesh.favdish.viewmodel.FavDishViewModelFactory
import com.waesh.favdish.viewmodel.RandomDishViewModel

class RandomDishFragment : Fragment() {

    private var _binding: FragmentRandomDishBinding? = null
    private val binding get() = _binding!!
    private val mViewModel: RandomDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRandomDishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.getRandomDishFromApi()
        randomDishViewModelObserver()
        binding.srlRandomDish.setOnRefreshListener {
            mViewModel.getRandomDishFromApi()
        }
    }

    private fun randomDishViewModelObserver() {
        mViewModel.randomDishResponse.observe(
            viewLifecycleOwner
        ) { randomDishResponse ->
            if (binding.srlRandomDish.isRefreshing){
                binding.srlRandomDish.isRefreshing = false
            }
            randomDishResponse?.let {
                setInUi(randomDishResponse.recipes[0])
            }
        }

        mViewModel.randomDishLoadError.observe(viewLifecycleOwner) { dataError ->
            if (binding.srlRandomDish.isRefreshing){
                binding.srlRandomDish.isRefreshing = false
            }
            dataError?.let {
                Log.i("kkkCat", "$dataError")
            }
        }

        mViewModel.loadRandomDish.observe(viewLifecycleOwner) { loadDish ->
            loadDish?.let {
                Log.i("kkkCat", "$loadDish")
            }
        }
    }

    private fun setInUi(recipe: RandomDish.Recipe) {

        var dishType = "Others"
        var ingredients = ""

        binding.apply {
            Glide.with(requireActivity())
                .load(recipe.image)
                .centerCrop()
                .into(binding.ivDishImage)

            tvTitle.text = recipe.title

            if (recipe.dishTypes.isNotEmpty()) {
                dishType = recipe.dishTypes[0]
                tvType.text = dishType
            }


            for (value in recipe.extendedIngredients) {
                ingredients = if (ingredients.isEmpty()) {
                    value.original
                } else
                    ingredients + ", \n" + value.original
            }

            tvIngredients.text = ingredients

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvCookingDirection.text = Html.fromHtml(
                    recipe.instructions,
                    Html.FROM_HTML_MODE_COMPACT
                )
            } else {
                @Suppress("DEPRECATION")
                tvCookingDirection.text = Html.fromHtml(
                    recipe.instructions
                )
            }

            tvCookingTime.text = getString(
                R.string.lbl_estimate_cooking_time,
                recipe.readyInMinutes.toString()
            )
            ivFavoriteDish.setOnClickListener {
                mViewModel.insertDish(
                    FavDish(
                        recipe.image,
                        Constants.IMAGE_SOURCE_ONLINE,
                        recipe.title,
                        dishType,
                        "Others",
                        ingredients,
                        recipe.readyInMinutes.toString(),
                        recipe.instructions,
                        true
                    )
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}