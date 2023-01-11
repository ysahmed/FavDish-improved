package com.waesh.favdish.view.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.waesh.favdish.R
import com.waesh.favdish.application.FavDishApplication
import com.waesh.favdish.databinding.FragmentDishDetailsBinding
import com.waesh.favdish.viewmodel.FavDishViewModel
import com.waesh.favdish.viewmodel.FavDishViewModelFactory
import java.io.IOException
import java.util.*


class DishDetailsFragment : Fragment() {

    private var binding: FragmentDishDetailsBinding? = null
    private val viewModel by viewModels<FavDishViewModel> {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDishDetailsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: DishDetailsFragmentArgs? by navArgs()
        args?.let { dishDetailsArgs ->
            try {
                Glide.with(requireActivity())
                    .load(dishDetailsArgs.dishDetails.image)
                    .centerCrop()
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            resource?.let {
                                Palette.from(resource.toBitmap()).generate { palette ->
                                    val intColor = palette?.vibrantSwatch?.rgb ?: 0
                                    binding!!.rlDishDetailMain.setBackgroundColor(intColor)
                                }
                            }
                            return false
                        }

                    })
                    .into(binding!!.ivDishImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            binding?.apply {
                tvTitle.text = dishDetailsArgs.dishDetails.title
                tvType.text = dishDetailsArgs.dishDetails.type.replaceFirstChar { type ->
                    if (type.isLowerCase()) type.titlecase(
                        Locale.ROOT
                    ) else type.toString()
                }
                tvCategory.text = dishDetailsArgs.dishDetails.category
                tvIngredients.text = dishDetailsArgs.dishDetails.ingredients
                tvCookingDirection.text = dishDetailsArgs.dishDetails.directionToCook
                tvCookingTime.text =
                    resources.getString(
                        R.string.lbl_estimate_cooking_time,
                        dishDetailsArgs.dishDetails.cookingTime
                    )
                ivFavoriteDish.setImageDrawable(getFavouriteDrawable(dishDetailsArgs.dishDetails.favoriteDish))
            }

            binding!!.ivFavoriteDish.setOnClickListener {
                dishDetailsArgs.dishDetails.favoriteDish = !dishDetailsArgs.dishDetails.favoriteDish
                viewModel.update(dishDetailsArgs.dishDetails)
                binding!!.ivFavoriteDish.setImageDrawable(getFavouriteDrawable(dishDetailsArgs.dishDetails.favoriteDish))
            }
        }
    }

    private fun getFavouriteDrawable(isFavourite: Boolean): Drawable? {

        val imageDrawable: Drawable? = if (isFavourite) {
            ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.ic_favorite_selected
            )

        } else {
            ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.ic_favorite_unselected
            )
        }
        return imageDrawable
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}