package com.programmersbox.animeworld.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.programmersbox.animeworld.R
import com.programmersbox.animeworld.StatePager
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_shows.*


class ShowsFragment(
    private val disposable: CompositeDisposable = CompositeDisposable(),
    private val pager: StatePager
) : Fragment() {


    private var prevMenuItem: MenuItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pager.add(RecentFragment(disposable))
        pager.add(AllFragment(disposable))
        pager.add(SettingsFragment(disposable))

        viewPager.adapter = pager
        viewPager.offscreenPageLimit = 5

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                //TODO("Not yet implemented")
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                //TODO("Not yet implemented")
            }

            override fun onPageSelected(position: Int) {
                /*navLayout.selectedItemId = when(position) {
                    0 -> R.id.recent
                    1 -> R.id.all
                    2 -> R.id.settings
                    else -> 0
                }*/

                (prevMenuItem ?: navLayout.menu.getItem(0))?.isCheckable = false

                /*if (prevMenuItem != null) {
                    prevMenuItem?.isChecked = false ?:
                } else {
                    navLayout.menu.getItem(0).isChecked = false
                }*/
                navLayout.menu.getItem(position).isChecked = true
                prevMenuItem = navLayout.menu.getItem(position)
            }

        })

        navLayout.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.all -> {
                    viewPager.currentItem = pager.indexOfFirst { it is AllFragment }
                }
                R.id.recent -> {
                    viewPager.currentItem = pager.indexOfFirst { it is RecentFragment }
                }
                R.id.settings -> {
                    viewPager.currentItem = pager.indexOfFirst { it is SettingsFragment }
                }
            }
            true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shows, container, false)
    }
}