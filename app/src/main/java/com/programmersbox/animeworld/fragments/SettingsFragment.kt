package com.programmersbox.animeworld.fragments

import android.Manifest
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseUser
import com.mikepenz.aboutlibraries.LibsBuilder
import com.obsez.android.lib.filechooser.ChooserDialog
import com.programmersbox.anime_db.ShowDatabase
import com.programmersbox.anime_sources.Sources
import com.programmersbox.animeworld.AnimeWorldApp
import com.programmersbox.animeworld.R
import com.programmersbox.animeworld.firebase.FirebaseAuthentication
import com.programmersbox.animeworld.firebase.FirebaseDb
import com.programmersbox.animeworld.utils.*
import com.programmersbox.gsonutils.fromJson
import com.programmersbox.helpfulutils.requestPermissions
import com.programmersbox.helpfulutils.setEnumSingleChoiceItems
import com.programmersbox.rxutils.invoke
import com.programmersbox.thirdpartyutils.into
import com.programmersbox.thirdpartyutils.openInCustomChromeBrowser
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.concurrent.TimeUnit


class SettingsFragment : PreferenceFragmentCompat() {

    private val disposable: CompositeDisposable = CompositeDisposable()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        accountSettings()
        generalSettings()
        aboutSettings()
        syncSettings()
    }

    private fun accountSettings() {
        FirebaseAuthentication.authenticate(requireContext())
        findPreference<Preference>("user_account")?.let { p ->

            fun accountChanges(user: FirebaseUser?) {
                Glide.with(this@SettingsFragment)
                    .load(user?.photoUrl ?: R.mipmap.round_logo)
                    .placeholder(R.mipmap.round_logo)
                    .error(R.mipmap.round_logo)
                    .fallback(R.mipmap.round_logo)
                    .circleCrop()
                    .into<Drawable> { resourceReady { image, _ -> p.icon = image } }
                p.title = user?.displayName ?: "User"
            }

            FirebaseAuthentication.auth.addAuthStateListener {
                accountChanges(it.currentUser)
                findPreference<Preference>("upload_favorites")?.isEnabled = it.currentUser != null
                findPreference<Preference>("upload_favorites")?.isVisible = it.currentUser != null
            }

            accountChanges(FirebaseAuthentication.currentUser)

            p.setOnPreferenceClickListener {
                FirebaseAuthentication.currentUser?.let {
                    MaterialAlertDialogBuilder(this@SettingsFragment.requireContext())
                        .setTitle("Log Out?")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes") { d, _ ->
                            FirebaseAuthentication.signOut()
                            d.dismiss()
                        }
                        .setNegativeButton("No") { d, _ -> d.dismiss() }
                        .show()
                } ?: FirebaseAuthentication.signIn(requireActivity())
                true
            }
        }
    }

    private fun aboutSettings() {

        findPreference<Preference>("about_version")?.let { p ->
            p.summary = context?.packageManager?.getPackageInfo(requireContext().packageName, 0)?.versionName
            p.setOnPreferenceClickListener {
                GlobalScope.launch {
                    @Suppress("BlockingMethodInNonBlockingContext") val info = try {
                        withContext(Dispatchers.Default) {
                            URL("https://raw.githubusercontent.com/jakepurple13/AnimeWorld/master/app/src/main/res/raw/update_changelog.json").readText()
                        }
                    } catch (e: Exception) {
                        resources.openRawResource(R.raw.update_changelog).bufferedReader().readText()
                    }.fromJson<AppInfo>()!!
                    requireActivity().runOnUiThread {
                        MaterialAlertDialogBuilder(this@SettingsFragment.requireContext())
                            .setTitle("Update notes for ${info.version}")
                            .setItems(info.releaseNotes.toTypedArray(), null)
                            .setPositiveButton("OK") { d, _ -> d.dismiss() }
                            .setNeutralButton("View Libraries Used") { d, _ ->
                                d.dismiss()
                                LibsBuilder().start(this@SettingsFragment.requireContext())
                            }
                            .setNegativeButton(R.string.gotoBrowser) { d, _ ->
                                requireContext().openInCustomChromeBrowser("https://github.com/jakepurple13/AnimeWorld")
                                d.dismiss()
                            }
                            .show()
                    }

                }
                true
            }
        }

    }

    private fun generalSettings() {

        findPreference<Preference>("folder_storage")?.let { p ->
            p.summary = requireContext().folderLocation
            p.setOnPreferenceClickListener {
                requireActivity().requestPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ) {
                    if (it.isGranted) {
                        ChooserDialog(requireActivity())
                            .withIcon(R.mipmap.round_logo)
                            .withStringResources("Choose a Directory", "CHOOSE", "CANCEL")
                            .withFilter(true, false)
                            .withStartFile(requireContext().folderLocation)
                            .enableOptions(true)
                            .withChosenListener { dir, _ ->
                                requireContext().folderLocation = "$dir/"
                                println(dir)
                                p.summary = requireContext().folderLocation
                            }
                            .build()
                            .show()
                    }
                }
                //requireContext().folderLocation
                true
            }
        }

        findPreference<Preference>("view_downloads")?.setOnPreferenceClickListener {
            //context?.startActivity(Intent(requireContext(), DownloadViewerActivity::class.java))
            findNavController().navigate(R.id.action_settingsFragment2_to_downloadViewerActivity3)
            true
        }

        findPreference<Preference>("view_videos")?.setOnPreferenceClickListener {
            //context?.startActivity(Intent(requireContext(), DownloadViewerActivity::class.java))
            findNavController().navigate(R.id.action_settingsFragment_to_viewVideosFragment)
            true
        }

        findPreference<Preference>("view_favorites")?.setOnPreferenceClickListener {
            //context?.startActivity(Intent(requireContext(), DownloadViewerActivity::class.java))
            findNavController().navigate(R.id.action_settingsFragment_to_favoritesFragment)
            true
        }

        findPreference<SeekBarPreference>("battery_alert")?.let { s ->
            s.showSeekBarValue = true
            s.setDefaultValue(requireContext().batteryAlertPercentage)
            s.value = requireContext().batteryAlertPercentage
            s.max = 100
            s.setOnPreferenceChangeListener { _, newValue ->
                if (newValue is Int) {
                    requireContext().batteryAlertPercentage = newValue
                }
                true
            }
        }

        findPreference<Preference>("current_source")?.let { p ->
            //it.entries = Sources.values().map { it.name }.toTypedArray()
            //it.value = requireContext().currentSource.name
            p.setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Choose a source")
                    .setEnumSingleChoiceItems(
                        Sources.values().map { it.name }.toTypedArray(),
                        requireContext().currentSource
                    ) { i, d ->
                        sourcePublish(i)
                        d.dismiss()
                    }
                    .show()
                true
            }
            sourcePublish.subscribe { p.title = "Current Source: ${it.name}" }
                .addTo(disposable)
        }

        findPreference<SwitchPreferenceCompat>("download_or_stream")?.let { s ->
            s.icon = ContextCompat.getDrawable(
                requireContext(),
                if (requireContext().downloadOrStream) R.drawable.ic_baseline_vertical_align_bottom_24 else R.drawable.ic_baseline_view_stream_24
            )
            s.setOnPreferenceChangeListener { _, newValue ->
                if (newValue is Boolean) {
                    //requireContext().downloadOrStream = newValue
                    downloadOrStreamPublish(newValue)
                    s.icon = ContextCompat.getDrawable(
                        requireContext(),
                        if (newValue) R.drawable.ic_baseline_vertical_align_bottom_24 else R.drawable.ic_baseline_view_stream_24
                    )
                }
                true
            }

        }

        findPreference<Preference>("goto_browser")?.let { p ->
            p.setOnPreferenceClickListener {
                context?.let { c ->
                    c.openInCustomChromeBrowser(c.currentSource.baseUrl) {
                        addDefaultShareMenuItem()
                    }
                }

                true
            }
        }
    }

    private fun syncSettings() {

        findPreference<Preference>("start_check")?.let { p ->
            p.setOnPreferenceClickListener {
                WorkManager.getInstance(requireContext()).enqueueUniqueWork(
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
                true
            }
        }

        findPreference<SwitchPreferenceCompat>("sync")?.let { s ->
            s.setDefaultValue(requireContext().updateCheck)
            s.setOnPreferenceChangeListener { _, newValue ->
                if (newValue is Boolean) {
                    requireContext().updateCheck = newValue
                    AnimeWorldApp.setupUpdate(requireContext(), newValue)
                }
                true
            }
        }

        findPreference<Preference>("upload_favorites")?.let { p ->
            p.setOnPreferenceClickListener {
                GlobalScope.launch {
                    val dao = ShowDatabase.getInstance(requireContext()).showDao()
                    dao.getAllShowSync().forEach { FirebaseDb.insertShow(it) }
                    dao.getAllEpisodesWatched().forEach { FirebaseDb.insertEpisodeWatched(it) }
                }
                true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        FirebaseAuthentication.onActivityResult(requestCode, resultCode, data, requireContext())
    }

}