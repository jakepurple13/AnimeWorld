package com.programmersbox.animeworld.utils

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.ComponentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.programmersbox.animeworld.R
import com.programmersbox.gsonutils.fromJson
import com.programmersbox.helpfulutils.DownloadDslManager
import com.programmersbox.helpfulutils.downloadManager
import com.programmersbox.helpfulutils.requestPermissions
import com.programmersbox.helpfulutils.runOnUIThread
import com.programmersbox.loggingutils.Loged
import com.programmersbox.loggingutils.f
import com.programmersbox.thirdpartyutils.openInCustomChromeBrowser
import java.io.File
import java.net.URL

data class AppInfo(val version: String, val url: String, val releaseNotes: List<String> = emptyList())

class AppUpdateChecker(private val activity: ComponentActivity) {
    private val context: Context = activity
    private val updateUrl = "https://raw.githubusercontent.com/jakepurple13/AnimeWorld/master/app/src/main/res/raw/update_changelog.json"

    @Suppress("RedundantSuspendModifier", "BlockingMethodInNonBlockingContext")
    suspend fun checkForUpdate() {
        try {
            val url = URL(updateUrl).readText()
            val info = url.fromJson<AppInfo>()
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val version = pInfo.versionName
            Loged.f("Current Version: $version | Server Version: ${info?.version}")
            info?.let { if (version.toDouble() < it.version.toDouble()) installUpdate(it) else deleteApk(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteApk(info: AppInfo) {
        val file =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + "animeworld${info.version}.apk")
        if (file.exists()) file.delete()
    }

    private fun installUpdate(info: AppInfo) {
        activity.requestPermissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) {
            if (it.isGranted) {
                runOnUIThread {
                    MaterialAlertDialogBuilder(context)
                        .setTitle(context.getString(R.string.updateTitle, info.version))
                        .setItems(info.releaseNotes.toTypedArray(), null)
                        .setPositiveButton(R.string.update) { d, _ ->
                            download2(info)
                            d.dismiss()
                        }
                        .setNeutralButton(R.string.gotoBrowser) { d, _ ->
                            context.openInCustomChromeBrowser("https://github.com/jakepurple13/AnimeWorld/releases/latest")
                            d.dismiss()
                        }
                        .setNegativeButton(R.string.notNow) { d, _ -> d.dismiss() }
                        .show()
                }
            }
        }

    }

    private fun download2(info: AppInfo) {
        val direct = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath + "/")

        if (!direct.exists()) {
            direct.mkdir()
        }

        val request = DownloadDslManager(context) {
            downloadUri = Uri.parse(info.url)
            allowOverRoaming = true
            networkType = DownloadDslManager.NetworkType.WIFI_MOBILE
            title = context.getString(R.string.animeWorldUpdate, info.version)
            mimeType = "application/vnd.android.package-archive"
            visibility = DownloadDslManager.NotificationVisibility.COMPLETED
            destinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, File.separator + "mangaworld${info.version}.apk"
            )
        }

        context.downloadManager.enqueue(request)
    }

}