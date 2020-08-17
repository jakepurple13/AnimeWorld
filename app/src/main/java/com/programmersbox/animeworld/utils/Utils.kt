package com.programmersbox.animeworld.utils

import android.content.Context
import android.os.Environment
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.programmersbox.anime_sources.Sources
import com.programmersbox.gsonutils.sharedPrefNotNullObjectDelegate
import com.programmersbox.helpfulutils.sharedPrefNotNullDelegate
import io.reactivex.subjects.BehaviorSubject

var Context.currentSource: Sources by sharedPrefNotNullObjectDelegate(Sources.values().random())

var Context.folderLocation: String by sharedPrefNotNullDelegate(
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString() + "/Fun/"
)

val sourcePublish = BehaviorSubject.create<Sources>()

@BindingAdapter("coverImage")
fun loadImage(view: ImageView, imageUrl: String?) {
    Glide.with(view)
        .load(imageUrl)
        .override(360, 480)
        .transform(RoundedCorners(15))
        .into(view)
}