package com.programmersbox.animeworld

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
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

class MainActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()

    private val pager by lazy { StatePager(supportFragmentManager) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().replace(R.id.container, ShowsFragment(disposable, pager)).commit()

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