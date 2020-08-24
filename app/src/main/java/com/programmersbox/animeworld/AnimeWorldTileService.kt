package com.programmersbox.animeworld

import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.programmersbox.animeworld.utils.UpdateWorker
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.N)
class AnimeWorldTileService : TileService() {
    override fun onClick() {
        super.onClick()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "updateChecking",
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<UpdateWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(false)
                        .setRequiresCharging(false)
                        .setRequiresDeviceIdle(false)
                        .setRequiresStorageNotLow(false)
                        .build()
                )
                .setInitialDelay(10, TimeUnit.SECONDS)
                .build()
        )
    }
}