package com.company.favdish.model.database

import androidx.annotation.WorkerThread
import com.company.favdish.model.entities.FavDish
import kotlinx.coroutines.flow.Flow

class FavDishRepository(private val favDishDoa: FavDishDao) {

    @WorkerThread
    suspend fun insertFavDishData(favDish: FavDish){
        favDishDoa.insertFavDishDetails(favDish)
    }

    val allDishesList: Flow<List<FavDish>> = favDishDoa.getAllDishesList()
}