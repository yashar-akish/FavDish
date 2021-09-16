package com.company.favdish.model.database

import androidx.annotation.WorkerThread
import com.company.favdish.model.entities.FavDish

class FavDishRepository(private val favDishDoa: FavDishDao) {

    @WorkerThread
    suspend fun insertFavDishData(favDish: FavDish){
        favDishDoa.insertFavDishDetails(favDish)
    }
}