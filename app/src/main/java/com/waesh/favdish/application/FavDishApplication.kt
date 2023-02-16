package com.waesh.favdish.application

import android.app.Application
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.waesh.favdish.model.repository.FavDishRepository
import com.waesh.favdish.model.database.FavDishRoomDatabase
import com.waesh.favdish.model.network.RetrofitHelper
import com.waesh.favdish.model.network.RandomDishApiInterface
import com.waesh.favdish.notification.NotificationWork
import com.waesh.favdish.util.Constants
import java.util.concurrent.TimeUnit

class FavDishApplication : Application() {

    private val database by lazy {
        FavDishRoomDatabase.getDatabase(this@FavDishApplication)
    }

    //changes
    private val apiInterface by lazy {
        RetrofitHelper.getRetrofitInstance().create(RandomDishApiInterface::class.java)
    }

    val repository by lazy {
        FavDishRepository(database.favDishDao(), apiInterface)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(NotificationWork::class.java, 16L, TimeUnit.MINUTES)
                .setInitialDelay(1L, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "Notification",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
    }

    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannelCompat.Builder(
            Constants.NOTIFICATION_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        ).apply {
            setName(Constants.NOTIFICATION_CHANNEL_NAME)
            setDescription(Constants.NOTIFICATION_CHANNEL_DESCRIPTION)
        }.build()

        NotificationManagerCompat.from(this).createNotificationChannel(notificationChannel)
    }

}