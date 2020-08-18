package com.programmersbox.animeworld.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseUser
import com.programmersbox.anime_sources.Sources
import com.programmersbox.animeworld.R
import com.programmersbox.animeworld.firebase.FirebaseAuthentication
import com.programmersbox.animeworld.utils.currentSource
import com.programmersbox.animeworld.utils.sourcePublish
import com.programmersbox.helpfulutils.setEnumSingleChoiceItems
import com.programmersbox.rxutils.invoke
import com.programmersbox.thirdpartyutils.into
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo


class SettingsFragment : PreferenceFragmentCompat() {

    private val disposable: CompositeDisposable = CompositeDisposable()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        FirebaseAuthentication.authenticate(requireContext())

        findPreference<Preference>("folder_storage")?.let { p ->

        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        FirebaseAuthentication.onActivityResult(requestCode, resultCode, data, requireContext())
    }

}