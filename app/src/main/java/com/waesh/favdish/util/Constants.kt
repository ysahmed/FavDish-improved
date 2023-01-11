package com.waesh.favdish.util

import android.content.Context
import android.widget.Toast
import com.waesh.favdish.R

fun makeToast(context: Context, message: String) {
    Toast.makeText(
        context,
        message,
        Toast.LENGTH_SHORT
    ).show()
}


object Constants {
    const val IMAGE_SOURCE_LOCAL = "local"
    const val IMAGE_SOURCE_ONLINE = "online"
    const val DISH_DETAILS_EXTRA = "DishDetail"

    const val BASE_URL = "https://api.spoonacular.com/"
    const val API_ENDPOINT = "recipes/random"
    const val API_KEY = "apiKey"
    const val LIMIT_LICENSE = "limitLicense"
    const val TAGS = "tags"
    const val NUMBER = "number"


    const val API_KEY_VALUE = com.waesh.favdish.util.API_KEY_VALUE
    const val LIMIT_LICENSE_VALUE = true
    const val TAGS_VALUE = "vegetarian, dessert"
    const val NUMBER_VALUE = 1

    val DISH_CATEGORIES = listOf(
        "Pizza",
        "BBQ",
        "Bakery",
        "Burger",
        "Cafe",
        "Chicken",
        "Dessert",
        "Hot Dogs",
        "Juices",
        "Sandwich",
        "Tea/Coffee",
        "Wraps",
        "Others",
        "All Dishes"
    )
}