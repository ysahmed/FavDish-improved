package com.waesh.favdish.view.fragments

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.waesh.favdish.R
import com.waesh.favdish.application.FavDishApplication
import com.waesh.favdish.databinding.FragmentRandomDishBinding
import com.waesh.favdish.model.entities.FavDish
import com.waesh.favdish.model.entities.RandomDish
import com.waesh.favdish.util.Constants
import com.waesh.favdish.viewmodel.*

class RandomDishFragment : Fragment() {

    private var _binding: FragmentRandomDishBinding? = null
    private val binding get() = _binding!!
    private lateinit var recipes: ListIterator<RandomDish.Recipe>

    val TAG = "RANDOMDISH"

    private val viewModel by viewModels<RandomDishViewModelCoroutines> {
        RandomDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
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

        val args: RandomDishFragmentArgs by navArgs()

        if (args.fromNotification) {
            setUi(viewModel.randomDish[0])
        } else {
            viewModel.getRandomDishes(15)
        }

        viewModel.apiResponse.observe(viewLifecycleOwner) {
            it?.let { response ->
                if (response.body() != null)
                    recipes = response.body()!!.recipes.listIterator()

                if (response.isSuccessful) {
                    setUi(recipes.next())
                } else
                    showDialog(R.string.title_response_failed, R.string.msg_response_failed)

            }
        }

        viewModel.connectionError.observe(viewLifecycleOwner) {
            if (it) {
                binding.srlRandomDish.isRefreshing = false
                showDialog(R.string.title_connection_error, R.string.msg_connection_error)
            }
        }

        binding.srlRandomDish.setOnRefreshListener {
            //viewModel.getRandomDish()
            if (recipes.hasNext()) {
                setUi(recipes.next())
            } else {
                viewModel.getRandomDishes(15)
            }
        }
    }


    private fun setUi(recipe: RandomDish.Recipe): Boolean {

        var dishType = "Others"
        var ingredients = ""

        var favorited = false   //LOL
        var inserted = false

        binding.apply {

            ivFavoriteDish.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.ic_favorite_unselected
                )
            )

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
                val mFavDishViewModel: FavDishViewModel by viewModels {
                    FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
                }

                if (!inserted) {
                    mFavDishViewModel.insertDish(
                        FavDish(
                            recipe.image,
                            Constants.IMAGE_SOURCE_ONLINE,
                            recipe.title,
                            dishType,
                            "Others",
                            ingredients,
                            recipe.readyInMinutes.toString(),
                            tvCookingDirection.text.toString(),
                            true
                        )
                    )
                    inserted = true
                }

                favorited = !favorited

                when (favorited) {
                    true -> binding.ivFavoriteDish.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_favorite_selected
                        )
                    )
                    false -> {
                        binding.ivFavoriteDish.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireActivity(),
                                R.drawable.ic_favorite_unselected
                            )
                        )
                        mFavDishViewModel.updateFavoriteByTitle(recipe.title, false)
                    }
                }
            }

            srlRandomDish.isRefreshing = false
        }
        return true
    }

    private fun showDialog(titleID: Int, messageId: Int) {
        AlertDialog.Builder(requireActivity())
            .setCancelable(false)
            .setTitle(titleID)
            .setMessage(messageId)
            .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
            }).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}