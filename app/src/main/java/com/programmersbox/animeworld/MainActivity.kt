package com.programmersbox.animeworld

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.programmersbox.animeworld.utils.currentSource
import com.programmersbox.animeworld.utils.setupWithNavController
import com.programmersbox.animeworld.utils.sourcePublish
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()

    //private val pager by lazy { StatePager(supportFragmentManager) }

    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //supportFragmentManager.beginTransaction().replace(R.id.container, ShowsFragment()).commit()
        //val h = supportFragmentManager.findFragmentById(R.id.mainShows) as NavHostFragment
        //navLayout2.setupWithNavController(h.navController)

        /*navLayout2.setOnNavigationItemSelectedListener {
            NavigationUI.onNavDestinationSelected(it, h.navController)
            *//*when (it.itemId) {
                R.id.all -> {
                    //viewPager.currentItem = pager.indexOfFirst { it is AllFragment }
                    NavigationUI.onNavDestinationSelected(it, h.navController)
                    h.navController.navigate(R.id.allFragment)
                }
                R.id.recent -> {
                    //viewPager.currentItem = pager.indexOfFirst { it is RecentFragment }
                    h.navController.navigate(R.id.recentFragment)
                }
                R.id.settings -> {
                    //viewPager.currentItem = pager.indexOfFirst { it is SettingsFragment }
                    h.navController.navigate(R.id.settingsFragment)
                }
            }*//*
            true
        }*/

        if (savedInstanceState == null) {
            setupBottomNavBar()
        }

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

        //controller.observe(this) { navController -> setupActionBarWithNavController(navController) }
        currentNavController = controller

        sourcePublish.onNext(currentSource)

        sourcePublish
            .subscribe { currentSource = it }
            .addTo(disposable)
    }

    override fun onSupportNavigateUp(): Boolean = currentNavController?.value?.navigateUp() ?: false

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }
}