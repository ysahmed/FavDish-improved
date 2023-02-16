package com.waesh.favdish.view.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
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
import com.waesh.favdish.model.entities.FavDish
import com.waesh.favdish.util.Constants
import com.waesh.favdish.viewmodel.FavDishViewModel
import com.waesh.favdish.viewmodel.FavDishViewModelFactory
import java.io.IOException
import java.util.*


class DishDetailsFragment : Fragment(), MenuProvider {

    private var binding: FragmentDishDetailsBinding? = null
    private val viewModel by viewModels<FavDishViewModel> {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }

    private lateinit var mFavDishDetails: FavDish

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDishDetailsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost = requireActivity()

        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val args: DishDetailsFragmentArgs? by navArgs()

        mFavDishDetails = args!!.dishDetails

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


    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        // Add menu items here
        menuInflater.inflate(R.menu.menu_share, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        // Handle the menu selection
        return when (menuItem.itemId) {
            R.id.action_share_dish -> {
                val type = "text/plain"
                val subject = "Checkout this dish recipe"
                var extraText: String
                val shareWith = "share with"

                mFavDishDetails.let {

                    var image = ""

                    if (it.imageSource == Constants.IMAGE_SOURCE_ONLINE) {
                        image = it.image
                    }

                    val cookingInstructions: String

                    // The instruction or you can say the Cooking direction text is in the HTML format so we will you the fromHtml to populate it in the TextView.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        cookingInstructions = Html.fromHtml(
                            it.directionToCook,
                            Html.FROM_HTML_MODE_COMPACT
                        ).toString()
                    } else {
                        @Suppress("DEPRECATION")
                        cookingInstructions = Html.fromHtml(it.directionToCook).toString()
                    }

                    extraText =
                        "$image \n" +
                                "\n Title:  ${it.title} \n\n Type: ${it.type} \n\n Category: ${it.category}" +
                                "\n\n Ingredients: \n ${it.ingredients} \n\n Instructions To Cook: \n $cookingInstructions" +
                                "\n\n Time required to cook the dish approx ${it.cookingTime} minutes."
                }


                val intent = Intent(Intent.ACTION_SEND)


                intent.setType(type)
                    .putExtra(Intent.EXTRA_SUBJECT, subject)
                    .putExtra(Intent.EXTRA_TEXT, extraText)
                startActivity(Intent.createChooser(intent, shareWith))
                true
            }
            else -> false
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