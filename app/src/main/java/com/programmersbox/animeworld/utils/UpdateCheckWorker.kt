package com.programmersbox.animeworld.utils

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.programmersbox.anime_db.ShowDao
import com.programmersbox.anime_db.ShowDatabase
import com.programmersbox.anime_db.ShowDbModel
import com.programmersbox.anime_sources.Episode
import com.programmersbox.anime_sources.ShowInfo
import com.programmersbox.anime_sources.Sources
import com.programmersbox.animeworld.R
import com.programmersbox.animeworld.firebase.FirebaseDb
import com.programmersbox.gsonutils.toJson
import com.programmersbox.helpfulutils.GroupBehavior
import com.programmersbox.helpfulutils.NotificationDslBuilder
import com.programmersbox.helpfulutils.intersect
import com.programmersbox.helpfulutils.notificationManager
import com.programmersbox.loggingutils.Loged
import com.programmersbox.loggingutils.f
import com.programmersbox.rxutils.invoke
import io.reactivex.Single
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicReference

class UpdateWorker(context: Context, workerParams: WorkerParameters) : RxWorker(context, workerParams) {

    private val update by lazy { UpdateNotification(this.applicationContext) }
    private val dao by lazy { ShowDatabase.getInstance(this@UpdateWorker.applicationContext).showDao() }

    override fun startWork(): ListenableFuture<Result> {
        update.sendRunningNotification(100, 0, "Starting check")
        return super.startWork()
    }

    override fun createWork(): Single<Result> {
        Loged.f("Starting check here")
        applicationContext.lastUpdateCheck = System.currentTimeMillis()
        return Single.create<List<ShowDbModel>> { emitter ->
            Loged.f("Start")
            val list = listOf(
                dao.getAllShowSync(),
                FirebaseDb.getAllShows().requireNoNulls()
            ).flatten().groupBy(ShowDbModel::showUrl).map { it.value.maxByOrNull(ShowDbModel::numEpisodes)!! }
            //applicationContext.dbAndFireMangaSync3(dao)
            /*val sourceList = Sources.getUpdateSearches()
                .filter { s -> list.any { m -> m.source == s } }
                .flatMap { m -> m.getManga() }*/

            val newList = list.intersect(
                Sources.values()
                    .filter { s -> list.any { m -> m.source == s } }
                    .mapNotNull { m ->
                        try {
                            m.getRecent().blockingGet()
                        } catch (e: Exception) {
                            //e.crashlyticsLog(m.name, "manga_load_error")
                            e.printStackTrace()
                            null
                        }
                    }.flatten()
            ) { o, n -> o.showUrl == n.url }
            //emitter(list.filter { m -> sourceList.any { it.mangaUrl == m.mangaUrl } })
            emitter(newList.distinctBy { it.showUrl })
        }
            .map { list ->
                Loged.f("Map1")
                val loadMarkersJob: AtomicReference<Job?> = AtomicReference(null)
                fun methodReturningJob() = GlobalScope.launch {
                    println("Before Delay")
                    delay(30000)
                    println("After Delay")
                    throw Exception("Finished")
                }
                list.mapIndexedNotNull { index, model ->
                    update.sendRunningNotification(list.size, index, model.title)
                    try {
                        loadMarkersJob.getAndSet(methodReturningJob())?.cancel()
                        val newData = model.toShow().getEpisodeInfo().blockingGet()
                        if (model.numEpisodes >= newData.episodes.size) null
                        else Pair(newData, model)
                    } catch (e: Exception) {
                        //e.crashlyticsLog(model.title, "manga_load_error")
                        e.printStackTrace()
                        null
                    }
                }.also {
                    try {
                        loadMarkersJob.get()?.cancel()
                    } catch (ignored: Exception) {
                    }
                }
            }
            .map {
                update.updateManga(dao, it)
                update.mapDbModel(it)
            }
            .map { update.onEnd(it).also { Loged.f("Finished!") } }
            .map {
                update.sendFinishedNotification()
                Result.success()
            }
            .onErrorReturn {
                println(it)
                update.sendFinishedNotification()
                Result.failure()
            }
    }

}

class UpdateNotification(private val context: Context) {

    fun updateManga(dao: ShowDao, triple: List<Pair<Episode, ShowDbModel>>) {
        triple.forEach {
            val show = it.second
            show.numEpisodes = it.first.episodes.size
            dao.updateShowById(show).subscribe()
            FirebaseDb.updateShow(show).subscribe()
        }
    }

    fun mapDbModel(list: List<Pair<Episode, ShowDbModel>>) = list.mapIndexed { index, pair ->
        sendRunningNotification(list.size, index, pair.second.title)
        //index + 3 + (Math.random() * 50).toInt() //for a possible new notification value
        pair.second.hashCode() to NotificationDslBuilder.builder(
            context,
            "showChannel",
            R.mipmap.round_logo_foreground
        ) {
            title = pair.second.title
            subText = pair.second.source.name
            getBitmapFromURL(pair.second.imageUrl)?.let {
                largeIconBitmap = it
                pictureStyle {
                    bigPicture = it
                    largeIcon = it
                    contentTitle = pair.first.episodes.firstOrNull()?.name ?: ""
                    summaryText = context.getString(
                        R.string.hadAnUpdate,
                        pair.second.title,
                        pair.first.episodes.firstOrNull()?.name ?: ""
                    )
                }
            } ?: bigTextStyle {
                contentTitle = pair.first.episodes.firstOrNull()?.name ?: ""
                bigText = context.getString(
                    R.string.hadAnUpdate,
                    pair.second.title,
                    pair.first.episodes.firstOrNull()?.name.orEmpty()
                )
            }
            showWhen = true
            groupId = "showGroup"
            pendingIntent { context ->
                NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.all_nav)
                    .setDestination(R.id.showInfoFragment2)
                    .setArguments(Bundle().apply { putString("showInfo", pair.second.toShow().toJson()) })
                    .createPendingIntent()
            }
        }
    } to list.map { m -> m.second.toShow() }

    fun onEnd(list: Pair<List<Pair<Int, Notification>>, List<ShowInfo>>) {
        val n = context.notificationManager
        val currentNotificationSize = n.activeNotifications.filterNot { list.first.any { l -> l.first == it.id } }.size - 1
        list.first.forEach { pair -> n.notify(pair.first, pair.second) }
        if (list.first.isNotEmpty()) n.notify(
            42,
            NotificationDslBuilder.builder(context, "showChannel", R.mipmap.round_logo_foreground) {
                title = context.getText(R.string.app_name)
                val size = list.first.size + currentNotificationSize
                subText = context.resources.getQuantityString(R.plurals.updateAmount, size, size)
                showWhen = true
                groupSummary = true
                groupAlertBehavior = GroupBehavior.ALL
                groupId = "showGroup"
            }
        )
    }

    private fun getBitmapFromURL(strURL: String?): Bitmap? = try {
        val url = URL(strURL)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        BitmapFactory.decodeStream(connection.inputStream)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }

    fun sendRunningNotification(max: Int, progress: Int, contextText: CharSequence = "") {
        val notification = NotificationDslBuilder.builder(context, "updateCheckChannel", R.mipmap.round_logo_foreground) {
            onlyAlertOnce = true
            ongoing = true
            progress {
                this.max = max
                this.progress = progress
                indeterminate = progress == 0
            }
            showWhen = true
            message = contextText
            subText = "Checking"
        }
        context.notificationManager.notify(13, notification)
        Loged.f("Checking for $contextText")
    }

    fun sendFinishedNotification() {
        val notification = NotificationDslBuilder.builder(context, "updateCheckChannel", R.mipmap.round_logo_foreground) {
            onlyAlertOnce = true
            subText = "Finished"
            timeoutAfter = 750L
        }
        context.notificationManager.notify(13, notification)
    }
}
