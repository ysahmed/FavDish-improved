package com.waesh.favdish.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.waesh.favdish.model.repository.FavDishRepository
import com.waesh.favdish.model.entities.RandomDish
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class RandomDishViewModelCoroutines(
    private val repository: FavDishRepository
) : ViewModel() {

    fun getRandomDishes(numberOfRecipes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getRandomDish(numberOfRecipes)
        }
    }

    val apiResponse: LiveData<Response<RandomDish.Recipes>>
        get() = repository.apiResponse

    val randomDish: List<RandomDish.Recipe>
        get() = repository.randomDishes

    val connectionError: LiveData<Boolean>
        get() = repository.connectionError
}


class RandomDishViewModelFactory(private val repository: FavDishRepository) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RandomDishViewModelCoroutines::class.java)) {
            return RandomDishViewModelCoroutines(repository) as T
        }
        throw IllegalArgumentException("Unknown VM Class")
    }
}