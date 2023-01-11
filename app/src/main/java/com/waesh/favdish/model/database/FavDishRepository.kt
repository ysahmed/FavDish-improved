package com.waesh.favdish.model.database

import androidx.annotation.WorkerThread
import com.waesh.favdish.model.entities.FavDish
import kotlinx.coroutines.flow.Flow

class FavDishRepository(private val dao: FavDishDao) {

    @WorkerThread
    suspend fun insertDish(item: FavDish){
        dao.insertDish(item)
    }

    @WorkerThread
    suspend fun updateFavDishDetail(item: FavDish){
        dao.updateFavDishDetail(item)
    }

    @WorkerThread
    suspend fun deleteDish(item: FavDish){
        dao.deleteDish(item)
    }

    @WorkerThread
    fun getDishesByCategory(category: String): Flow<List<FavDish>>{
        return dao.getDishesByCategory(category)
    }

    val allDishesList: Flow<List<FavDish>> = dao.getAllDishes()
    val favoriteDishes: Flow<List<FavDish>> = dao.getFavoriteDishes()
}