package com.waesh.favdish.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.waesh.favdish.model.entities.RandomDish
import com.waesh.favdish.model.network.RandomDishApiService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers

class RandomDishViewModel: ViewModel() {

    private val randomDishApiService = RandomDishApiService()
    private val compositeDisposable = CompositeDisposable()

    val loadRandomDish = MutableLiveData<Boolean>()
    val randomDishResponse = MutableLiveData<RandomDish.Recipes>()
    val randomDishLoadError = MutableLiveData<Boolean>()

    fun getRandomDishFromApi(){
        loadRandomDish.value = true
        compositeDisposable.add(
            randomDishApiService.getRandomDish()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<RandomDish.Recipes>(){
                    override fun onSuccess(value: RandomDish.Recipes) {
                        loadRandomDish.value = false
                        randomDishResponse.value = value
                        randomDishLoadError.value = false
                    }

                    override fun onError(e: Throwable) {
                        loadRandomDish.value = false
                        randomDishLoadError.value = true
                        e.printStackTrace()
                    }
                })
        )
    }
}