package com.programmersbox.animeworld

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navArgs

class VideoPlayerActivity : AppCompatActivity() {

    private val args by navArgs<VideoPlayerActivityArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)


    }
}