package com.waesh.favdish.model.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.waesh.favdish.model.database.FavDishDao
import com.waesh.favdish.model.entities.FavDish
import com.waesh.favdish.model.entities.RandomDish
import com.waesh.favdish.model.network.RandomDishApiInterface
import com.waesh.favdish.util.Constants
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class FavDishRepository(
    private val dao: FavDishDao,
    private val randomDishApiInterface: RandomDishApiInterface
) {

    @WorkerThread
    suspend fun insertDish(item: FavDish) {
        dao.insertDish(item)
    }

    @WorkerThread
    suspend fun updateFavDishDetail(item: FavDish) {
        dao.updateFavDishDetail(item)
    }

    @WorkerThread
    suspend fun updateFavoriteByTitle(title: String, favorite: Boolean) {
        dao.updateFavoriteByTitle(title, favorite)
    }

    @WorkerThread
    suspend fun deleteDish(item: FavDish) {
        dao.deleteDish(item)
    }

    @WorkerThread
    fun getDishesByCategory(category: String): Flow<List<FavDish>> {
        return dao.getDishesByCategory(category)
    }

    val allDishesList: Flow<List<FavDish>> = dao.getAllDishes()
    val favoriteDishes: Flow<List<FavDish>> = dao.getFavoriteDishes()

    //for Coroutine use (retrofit)

    private val _apiResponse = MutableLiveData<Response<RandomDish.Recipes>>()
    private val _connectionError = MutableLiveData<Boolean>(false)

    lateinit var randomDishes: List<RandomDish.Recipe>
        private set

    val apiResponse: LiveData<Response<RandomDish.Recipes>>
        get() = _apiResponse

    val connectionError: LiveData<Boolean>
        get() = _connectionError

    suspend fun getRandomDish(number: Int) {
        _connectionError.postValue(false)
        try {
            val result: Response<RandomDish.Recipes> =
                randomDishApiInterface.getRandomDishByCoroutine(
                    Constants.API_KEY_VALUE,
                    Constants.LIMIT_LICENSE_VALUE,
                    Constants.TAGS_VALUE,
                    number
                )

            if (result.isSuccessful) {
                //val x: RandomDish.Recipes? = result.body()
                randomDishes = result.body()!!.recipes
                _apiResponse.postValue(result)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _connectionError.postValue(true)
        }

    }
}