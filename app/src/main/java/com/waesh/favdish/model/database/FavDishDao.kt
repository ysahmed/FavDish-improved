package com.waesh.favdish.model.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.waesh.favdish.model.entities.FavDish
import kotlinx.coroutines.flow.Flow

@Dao
interface FavDishDao {

    @Insert
    suspend fun insertDish(item: FavDish)

    @Update
    suspend fun updateFavDishDetail (item: FavDish)

    @Query("SELECT * FROM fav_dish_table ORDER by id")
    fun getAllDishes(): Flow<List<FavDish>>

    @Query("SELECT * FROM FAV_DISH_TABLE WHERE category = :category ORDER by id")
    fun getDishesByCategory(category: String): Flow<List<FavDish>>

    @Query("SELECT * FROM FAV_DISH_TABLE WHERE favorite_dish = 1 ORDER by id")
    fun getFavoriteDishes(): Flow<List<FavDish>>

    @Delete
    suspend fun deleteDish(item: FavDish)
}