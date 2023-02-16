package com.waesh.favdish.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.waesh.favdish.R
import com.waesh.favdish.application.FavDishApplication
import com.waesh.favdish.model.repository.FavDishRepository
import com.waesh.favdish.util.Constants
import com.waesh.favdish.view.activities.MainActivity
import kotlinx.coroutines.*

class NotificationWork(
    private val context: Context,
    private val workerParams: WorkerParameters
) : Worker(
    context,
    workerParams
) {
    override fun doWork(): Result {

        val scope = CoroutineScope(Dispatchers.IO + CoroutineName("work"))
        val repository =
            (applicationContext as FavDishApplication).repository

        // following code doesn't look right
        // TODO recheck
        try {
            scope.launch {
                repository.getRandomDish(1)
                if (!repository.connectionError.value!!) {
                    val notification =
                        NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                            .setAutoCancel(true)
                            .setSmallIcon(R.drawable.ic_favorite_dish)
                            .setContentTitle(context.getString(R.string.notification_title))
                            .setContentText(repository.randomDishes[0].title)
                            .setContentIntent(
                                PendingIntent.getActivity(
                                    context,
                                    145877,
                                    Intent(applicationContext, MainActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        action = Constants.NOTIFICATION_ACTION_RANDOM_DISH
                                    },
                                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
                                        PendingIntent.FLAG_IMMUTABLE
                                    else
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                )
                            )
                            .build()

                    //NotificationManagerCompat.from(context).notify(Constants.NOTIFICATION_ID, notification)

                    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                        Constants.NOTIFICATION_ID,
                        notification
                    )
                }
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }
}