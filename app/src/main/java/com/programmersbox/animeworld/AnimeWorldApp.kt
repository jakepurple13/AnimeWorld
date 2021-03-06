package com.programmersbox.animeworld

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.facebook.stetho.Stetho
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.icon
import com.programmersbox.animeworld.utils.CustomFetchNotificationManager
import com.programmersbox.animeworld.utils.UpdateWorker
import com.programmersbox.animeworld.utils.updateCheck
import com.programmersbox.helpfulutils.NotificationChannelImportance
import com.programmersbox.helpfulutils.createNotificationChannel
import com.programmersbox.helpfulutils.createNotificationGroup
import com.programmersbox.loggingutils.Loged
import com.tonyodev.fetch2.Fetch.Impl.setDefaultInstanceConfiguration
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.HttpUrlConnectionDownloader
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2core.Downloader.FileDownloaderType
import io.reactivex.plugins.RxJavaPlugins
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class AnimeWorldApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        Loged.FILTER_BY_PACKAGE_NAME = "programmersbox"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("showChannel", importance = NotificationChannelImportance.HIGH)
            createNotificationGroup("showGroup")
            createNotificationChannel("updateCheckChannel", importance = NotificationChannelImportance.MIN)
            createNotificationChannel("appUpdate", importance = NotificationChannelImportance.HIGH)
        }
        val fetchConfiguration = FetchConfiguration.Builder(this)
            .enableAutoStart(true)
            .enableRetryOnNetworkGain(true)
            .enableLogging(true)
            .setProgressReportingInterval(1000L)
            .setGlobalNetworkType(NetworkType.ALL) //.setHttpDownloader(new HttpUrlConnectionDownloader(Downloader.FileDownloaderType.PARALLEL))
            .setHttpDownloader(HttpsUrlConnectionDownloader(FileDownloaderType.PARALLEL)) //.setHttpDownloader(new OkHttpDownloader(okHttpClient, Downloader.FileDownloaderType.PARALLEL))
            .setDownloadConcurrentLimit(1)
            //.setNotificationManager(DefaultFetchNotificationManager(this))
            .setNotificationManager(CustomFetchNotificationManager(this))
            .build()
        setDefaultInstanceConfiguration(fetchConfiguration)
        RxJavaPlugins.setErrorHandler {
            it.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(it)
            /*try {
                //runOnUIThread { Toast.makeText(this, it.cause?.localizedMessage, Toast.LENGTH_SHORT).show() }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
            }*/
        }
        /*RxFetch.getRxInstance(fetchConfiguration)
            .getDownloads().flowable
            .subscribe {
                println(it)
            }*/

        setupUpdate(this, updateCheck)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutSetup()
        }

    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun shortcutSetup() {
        val manager = getSystemService(ShortcutManager::class.java)
        if (manager.dynamicShortcuts.size == 0) {
            // Application restored. Need to re-publish dynamic shortcuts.
            if (manager.pinnedShortcuts.size > 0) {
                // Pinned shortcuts have been restored. Use
                // updateShortcuts() to make sure they contain
                // up-to-date information.
                manager.removeAllDynamicShortcuts()
            }
        }

        val shortcuts = mutableListOf<ShortcutInfo>()

        //download viewer
        shortcuts.add(
            ShortcutInfo.Builder(this, "download_viewer")
                .setIcon(Icon.createWithBitmap(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_file_download).toBitmap()))
                .setShortLabel("View Downloads")
                .setLongLabel("View Downloads")
                .setIntent(Intent(Intent.ACTION_MAIN, Uri.EMPTY, this, DownloadViewerActivity::class.java))
                .build()
        )

        //video viewer
        shortcuts.add(
            ShortcutInfo.Builder(this, "video_viewer")
                .setIcon(Icon.createWithBitmap(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_video_library).toBitmap()))
                .setShortLabel("View Videos")
                .setLongLabel("View Videos")
                .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse("animeworld://view_videos")))
                .build()
        )

        manager.dynamicShortcuts = shortcuts
    }

    companion object {
        fun setupUpdate(context: Context, shouldCheck: Boolean) {
            try {

                val work = WorkManager.getInstance(context)
                //work.cancelAllWork()
                if (shouldCheck) {
                    work.enqueueUniquePeriodicWork(
                        "updateChecks",
                        ExistingPeriodicWorkPolicy.KEEP,
                        PeriodicWorkRequest.Builder(UpdateWorker::class.java, 1, TimeUnit.HOURS)
                            //PeriodicWorkRequest.Builder(UpdateWorker::class.java, 15, TimeUnit.MINUTES)
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
                    ).state.observeForever { println(it) }
                } else work.cancelAllWork()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}

class HttpsUrlConnectionDownloader(fileDownloaderType: FileDownloaderType) : HttpUrlConnectionDownloader(fileDownloaderType) {
    override fun onPreClientExecute(client: HttpURLConnection, request: Downloader.ServerRequest): Void? {
        super.onPreClientExecute(client, request)
        if (request.url.startsWith("https")) {
            val httpsURLConnection: HttpsURLConnection = client as HttpsURLConnection
            httpsURLConnection.sslSocketFactory = sSLSocketFactory
        }
        return null
    }

    private val sSLSocketFactory: SSLSocketFactory?
        get() {
            var sslContext: SSLContext? = null
            try {
                val tm: Array<TrustManager> = arrayOf(
                    object : X509TrustManager {
                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkClientTrusted(p0: Array<out java.security.cert.X509Certificate>?, authType: String?) {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkServerTrusted(p0: Array<out java.security.cert.X509Certificate>?, authType: String?) {
                        }

                        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate>? = null
                    }
                )
                sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, tm, null)
                HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return sslContext?.socketFactory
        }
}

class GenericFileProvider : FileProvider()

