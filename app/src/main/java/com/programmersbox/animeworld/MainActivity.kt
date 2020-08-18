package com.programmersbox.animeworld

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager.widget.ViewPager
import com.programmersbox.animeworld.fragments.AllFragment
import com.programmersbox.animeworld.fragments.RecentFragment
import com.programmersbox.animeworld.fragments.ShowsFragment
import com.programmersbox.animeworld.utils.currentSource
import com.programmersbox.animeworld.utils.sourcePublish
import com.programmersbox.rxutils.invoke
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_shows.*

class MainActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()

    //private val pager by lazy { StatePager(supportFragmentManager) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //supportFragmentManager.beginTransaction().replace(R.id.container, ShowsFragment()).commit()
        val h = supportFragmentManager.findFragmentById(R.id.mainShows) as NavHostFragment
        navLayout2.setupWithNavController(h.navController)

        navLayout2.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.all -> {
                    //viewPager.currentItem = pager.indexOfFirst { it is AllFragment }
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
            }
            true
        }

        sourcePublish.onNext(currentSource)

        sourcePublish
            .subscribe { currentSource = it }
            .addTo(disposable)

    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }
}