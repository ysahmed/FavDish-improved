package com.waesh.favdish.viewmodel

import androidx.lifecycle.*
import com.waesh.favdish.model.repository.FavDishRepository
import com.waesh.favdish.model.entities.FavDish
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavDishViewModel(private val repository: FavDishRepository) : ViewModel() {

    private var _deletedDish: FavDish? = null
    private val _filterCategory = MutableLiveData<String>()
    val filterCategory: LiveData<String>
        get() = _filterCategory

    fun setFilterCategory(s: String) {
        _filterCategory.value = s
    }

    fun insertDish(dish: FavDish) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertDish(dish)
    }

    fun update(item: FavDish) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateFavDishDetail(item)
    }

    fun updateFavoriteByTitle(title: String, favorite: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavoriteByTitle(title, favorite)
        }
    }

    fun deleteDish(item: FavDish) {
        _deletedDish = item
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDish(item)
        }
    }

    fun undoDelete() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertDish(_deletedDish!!)
            _deletedDish = null
        }
    }

    fun getDishesByCategory(category: String): LiveData<List<FavDish>> =
        repository.getDishesByCategory(category).asLiveData()


    val allDishesList = repository.allDishesList.asLiveData()
    val favoriteDishes = repository.favoriteDishes.asLiveData()
}


//ViewModelFactoryClass
class FavDishViewModelFactory(private val repository: FavDishRepository) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavDishViewModel::class.java)) {
            return FavDishViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown VM Class")
    }
}