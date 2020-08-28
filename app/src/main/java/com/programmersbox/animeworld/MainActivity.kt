package com.programmersbox.animeworld

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.programmersbox.animeworld.utils.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()

    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            setupBottomNavBar()
        }

        GlobalScope.launch { AppUpdateChecker(this@MainActivity).checkForUpdate() }

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavBar()
    }

    private fun setupBottomNavBar() {
        val navGraphIds = listOf(R.navigation.recent_nav, R.navigation.all_nav, R.navigation.settings_nav)

        val controller = navLayout2.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.mainShows,
            intent = intent
        )

        currentNavController = controller

        sourcePublish.onNext(currentSource)

        sourcePublish
            .subscribe { currentSource = it }
            .addTo(disposable)

        downloadOrStreamPublish
            .subscribe { downloadOrStream = it }
            .addTo(disposable)

        updateCheckPublish
            .subscribe { lastUpdateCheck = it }
            .addTo(disposable)
    }

    override fun onSupportNavigateUp(): Boolean = currentNavController?.value?.navigateUp() ?: false

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }
}
