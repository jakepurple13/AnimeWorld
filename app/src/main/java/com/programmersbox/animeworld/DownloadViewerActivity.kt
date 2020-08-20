package com.programmersbox.animeworld

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.programmersbox.animeworld.adapters.ActionListener
import com.programmersbox.animeworld.adapters.FileAdapter
import com.programmersbox.dragswipe.Direction
import com.programmersbox.dragswipe.DragSwipeActionBuilder
import com.programmersbox.dragswipe.DragSwipeUtils
import com.programmersbox.helpfulutils.notificationManager
import com.programmersbox.loggingutils.Loged
import com.tonyodev.fetch2.AbstractFetchListener
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2core.Func
import kotlinx.android.synthetic.main.activity_download_viewer.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class DownloadViewerActivity : AppCompatActivity(), ActionListener {

    private val UNKNOWN_REMAINING_TIME: Long = -1
    private val UNKNOWN_DOWNLOADED_BYTES_PER_SECOND: Long = 0
    private var fetch: Fetch = Fetch.getDefaultInstance()
    private var fileAdapter: FileAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_viewer)

        fetch = Fetch.getDefaultInstance()
        fileAdapter = FileAdapter(this, this)
        download_list.adapter = fileAdapter

        DragSwipeUtils.setDragSwipeUp(
            fileAdapter!!,
            download_list,
            listOf(Direction.NOTHING),
            listOf(Direction.START, Direction.END),
            DragSwipeActionBuilder {
                onSwiped { viewHolder, direction, dragSwipeAdapter ->
                    (dragSwipeAdapter as? FileAdapter)?.onItemDismiss(viewHolder.adapterPosition, direction.value)
                }
            }
        )

        multiple_download_delete.setOnClickListener {

            val downloadItems = mutableListOf<FileAdapter.DownloadData>()
            MaterialAlertDialogBuilder(this)
                .setTitle("Delete")
                .setMultiChoiceItems(fileAdapter!!.dataList.map { it.download?.file }.toTypedArray(), null) { _, i, b ->
                    if (b) downloadItems.add(fileAdapter!!.dataList[i]) else downloadItems.remove(fileAdapter!!.dataList[i])
                }
                .setPositiveButton("Delete") { d, _ ->
                    Fetch.getDefaultInstance().delete(downloadItems.map { it.id })
                    d.dismiss()
                }
                .show()

            GlobalScope.launch {

                //val downloadList = fileAdapter!!.downloads

                /*fetch!!.getDownloads(Func {
                    Loged.wtf(it.joinToString { "," })
                    downloadList.addAll(it)
                })*/

                /*val multiSelectDialog = MultiSelectDialog()
                    .title("Select the Downloads to Cancel") //setting title for dialog
                    .titleSize(25f)
                    .positiveText("Done")
                    .negativeText("Cancel")
                    .setMinSelectionLimit(0) //you can set minimum checkbox selection limit (Optional)
                    .setMaxSelectionLimit(downloadList.size) //you can set maximum checkbox selection limit (Optional)
                    .multiSelectList(ArrayList<MultiSelectModel>().apply {
                        for (i in 0 until downloadList.size) {
                            add(MultiSelectModel(downloadList[i].id, Uri.parse(downloadList[i].download!!.url).lastPathSegment))
                        }
                    }) // the multi select model list with ids and name
                    .onSubmit(object : MultiSelectDialog.SubmitCallbackListener {
                        override fun onSelected(selectedIds: ArrayList<Int>?, selectedNames: ArrayList<String>?, dataString: String?) {
                            FetchingUtils.downloadCount -= selectedIds!!.size
                            fetch!!.cancel(selectedIds)
                                .delete(selectedIds)
                                .remove(selectedIds)
                        }

                        override fun onCancel() {
                            Loged.e("cancelled")
                        }

                    })

                runOnUiThread {
                    multiSelectDialog.show(supportFragmentManager, "multiSelectDialog")
                }*/
            }
        }

    }

    override fun onResume() {
        super.onResume()
        fetch.getDownloads(Func { downloads ->
            val list = ArrayList(downloads)
            list.sortWith(Comparator { first, second -> java.lang.Long.compare(first.created, second.created) })
            for (download in list) {
                fileAdapter!!.addDownload(download)
            }
        }).addListener(fetchListener)
    }

    override fun onPause() {
        super.onPause()
        //fetch!!.removeListener(fetchListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        //fetch!!.close()
    }

    private val fetchListener = object : AbstractFetchListener() {
        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
            fileAdapter!!.addDownload(download)
            fileAdapter!!.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
        }

        override fun onCompleted(download: Download) {
            fileAdapter!!.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
            notificationManager.cancel(download.id)
            //mNotificationManager.cancelAll()
            Fetch.getDefaultInstance().remove(download.id)

            //FetchingUtils.downloadCount+=1
            /*sendNotification(this@DownloadViewerActivity, android.R.mipmap.sym_def_app_icon,
                    download.file.substring(download.file.lastIndexOf("/") + 1),
                    "All Finished!",
                    ConstantValues.CHANNEL_ID,
                    EpisodeActivity::class.java, download.id,
                    EpisodeActivity.KeyAndValue(ConstantValues.URL_INTENT, "${download.extras.map[ConstantValues.URL_INTENT]}"),
                    EpisodeActivity.KeyAndValue(ConstantValues.NAME_INTENT, "${download.extras.map[ConstantValues.NAME_INTENT]}"))*/
            /* if (defaultSharedPreferences.getBoolean("useNotifications", true)) {
                 sendNotification(this@DownloadViewerActivity, android.R.mipmap.sym_def_app_icon,
                     download.file.substring(download.file.lastIndexOf("/") + 1),
                     "All Finished!",
                     ConstantValues.CHANNEL_ID,
                     StartVideoFromNotificationActivity::class.java, download.id,
                     EpisodeActivity.KeyAndValue("video_path", download.file),
                     EpisodeActivity.KeyAndValue("video_name", download.file))
                 sendGroupNotification(this@DownloadViewerActivity,
                     android.R.mipmap.sym_def_app_icon,
                     "Finished Downloads",
                     ConstantValues.CHANNEL_ID,
                     ViewVideosActivity::class.java)
             }*/
        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            super.onError(download, error, throwable)
            fileAdapter!!.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
        }

        override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
            fileAdapter!!.update(download, etaInMilliSeconds, downloadedBytesPerSecond)
            /*val progress = "%.2f".format(FetchingUtils.getProgress(download.downloaded, download.total))
            val info = "$progress% " +
                    "at ${FetchingUtils.getDownloadSpeedString(downloadedBytesPerSecond)} " +
                    "with ${FetchingUtils.getETAString(etaInMilliSeconds)}"*/
            /*sendProgressNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                    info,
                    download.progress,
                    this@DownloadViewerActivity,
                    DownloadViewerActivity::class.java,
                    download.id)*/
            //DefaultFetchNotificationManager(this@DownloadViewerActivity).postNotificationUpdate(download, etaInMilliSeconds, downloadedBytesPerSecond)
        }

        override fun onPaused(download: Download) {
            fileAdapter!!.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
            //val mNotificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            //mNotificationManager.cancel(download.id)
            /*stats = EpisodeActivity.StatusPlay.PAUSE
            *//*sendProgressNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                    "Paused",
                    download.progress,
                    this@DownloadViewerActivity,
                    DownloadViewerActivity::class.java,
                    download.id)*//*
            if (DownloadsWidget.isWidgetActive(this@DownloadViewerActivity))
                DownloadsWidget.sendRefreshBroadcast(this@DownloadViewerActivity)*/
        }

        override fun onResumed(download: Download) {
            fileAdapter!!.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
            //stats = EpisodeActivity.StatusPlay.PLAY
            /*sendProgressNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                    "Resumed",
                    download.progress,
                    this@DownloadViewerActivity,
                    DownloadViewerActivity::class.java,
                    download.id)*/
        }

        override fun onCancelled(download: Download) {
            fileAdapter!!.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
            notificationManager.cancel(download.id)
            try {
                deleteFile(download.file)
            } catch (e: IllegalArgumentException) {
                Loged.w(e.message!!)//e.printStackTrace()
            } catch (e: java.lang.NullPointerException) {
                Loged.w(e.message!!)//e.printStackTrace()
            }
        }

        override fun onRemoved(download: Download) {
            fileAdapter!!.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
            //val mNotificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            //mNotificationManager.cancel(download.id)
        }

        override fun onDeleted(download: Download) {
            fileAdapter!!.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
            notificationManager.cancel(download.id)
            try {
                deleteFile(download.file)
            } catch (e: IllegalArgumentException) {
                Loged.w(e.message!!)//e.printStackTrace()
            } catch (e: java.lang.NullPointerException) {
                Loged.w(e.message!!)//e.printStackTrace()
            }
        }
    }

    fun sendNotification(
        context: Context,
        smallIconId: Int,
        title: String,
        message: String,
        channel_id: String,
        gotoActivity: Class<*>,
        notification_id: Int
    ) {
        // The id of the channel.
        val mBuilder = NotificationCompat.Builder(context, channel_id)
            .setSmallIcon(smallIconId)
            .setContentTitle(title)
            .setContentText(message)
            .setGroup("downloaded_group")
            .setChannelId(channel_id)
            .setAutoCancel(true)

        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(context, gotoActivity)
        //resultIntent.putExtra(ConstantValues.DOWNLOAD_NOTIFICATION, false)

        /*for (i in dataToPass) {
            resultIntent.putExtra(i.key, i.value)
        }*/

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your app to the Home screen.
        val stackBuilder = TaskStackBuilder.create(context)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(gotoActivity)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent)
        //stackBuilder.addNextIntent(Intent.createChooser(resultIntent, "Complete action using"))
        val resultPendingIntent = stackBuilder.getPendingIntent(
            notification_id * 2,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        notificationManager.notify(notification_id * 2, mBuilder.build())

    }

    private fun sendGroupNotification(context: Context, smallIconId: Int, title: String, channel_id: String, gotoActivity: Class<*>) {

        // The id of the channel.
        val mBuilder = NotificationCompat.Builder(context, channel_id)
            .setSmallIcon(smallIconId)
            .setContentTitle(title)
            .setChannelId(channel_id)
            .setGroupSummary(true)
            .setGroup("downloaded_group")
            .setAutoCancel(true)
        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(context, gotoActivity)

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your app to the Home screen.
        val stackBuilder = TaskStackBuilder.create(context)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(gotoActivity)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)
        //val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        notificationManager.notify(99, mBuilder.build())
    }

    override fun onPauseDownload(id: Int) {
        fetch.pause(id)
    }

    override fun onResumeDownload(id: Int) {
        fetch.resume(id)
    }

    override fun onRemoveDownload(id: Int) {
        fetch.remove(id)
    }

    override fun onRetryDownload(id: Int) {
        fetch.retry(id)
    }

}