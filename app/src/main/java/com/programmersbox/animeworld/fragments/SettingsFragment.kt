package com.programmersbox.animeworld.fragments

import android.content.Intent
import android.os.Bundle
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.programmersbox.anime_sources.Sources
import com.programmersbox.animeworld.DownloadViewerActivity
import com.programmersbox.animeworld.R
import com.programmersbox.animeworld.utils.currentSource
import com.programmersbox.animeworld.utils.sourcePublish
import com.programmersbox.helpfulutils.setEnumSingleChoiceItems
import com.programmersbox.rxutils.invoke
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

class SettingsFragment(private val disposable: CompositeDisposable) : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<Preference>("view_downloads")?.setOnPreferenceClickListener {
            context?.startActivity(Intent(requireContext(), DownloadViewerActivity::class.java))
            true
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
    }
}