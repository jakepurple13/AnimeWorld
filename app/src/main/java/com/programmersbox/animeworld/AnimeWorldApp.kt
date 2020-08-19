package com.programmersbox.animeworld

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.core.content.FileProvider
import androidx.work.WorkManager
import com.facebook.stetho.Stetho
import com.programmersbox.animeworld.utils.CustomFetchNotificationManager
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
import java.net.HttpURLConnection
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
        /*RxFetch.getRxInstance(fetchConfiguration)
            .getDownloads().flowable
            .subscribe {
                println(it)
            }*/

        setupUpdate(this, updateCheck)

    }

    companion object {
        fun setupUpdate(context: Context, shouldCheck: Boolean) {
            try {

                val work = WorkManager.getInstance(context)
                //work.cancelAllWork()
                if (shouldCheck) {
                    /*work.enqueueUniquePeriodicWork(
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
                    ).state.observeForever { println(it) }*/
                }
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
                        override fun checkClientTrusted(p0: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkServerTrusted(p0: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
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

