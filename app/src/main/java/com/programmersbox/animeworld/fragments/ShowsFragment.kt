package com.programmersbox.animeworld.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.programmersbox.animeworld.R
import com.programmersbox.animeworld.StatePager
import io.reactivex.disposables.CompositeDisposable


class ShowsFragment(
    //private val disposable: CompositeDisposable = CompositeDisposable(),
    //private val pager: StatePager
) : NavHostFragment() {

    private val disposable: CompositeDisposable = CompositeDisposable()
    private val pager by lazy { StatePager(parentFragmentManager) }

    private var prevMenuItem: MenuItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*pager.add(RecentFragment())
        //pager.add(NavHostFragment.create(R.navigation.recent_nav))
        pager.add(AllFragment())
        pager.add(SettingsFragment())

        viewPager.adapter = pager
        viewPager.offscreenPageLimit = 5*/

        /*viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                //TODO("Not yet implemented")
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                //TODO("Not yet implemented")
            }

            override fun onPageSelected(position: Int) {
                *//*navLayout.selectedItemId = when(position) {
                    0 -> R.id.recent
                    1 -> R.id.all
                    2 -> R.id.settings
                    else -> 0
                }*//**//*

                (prevMenuItem ?: navLayout.menu.getItem(0))?.isCheckable = false

                *//**//*if (prevMenuItem != null) {
                    prevMenuItem?.isChecked = false ?:
                } else {
                    navLayout.menu.getItem(0).isChecked = false
                }*//*
                *//*navLayout.menu.getItem(position).isChecked = true
                prevMenuItem = navLayout.menu.getItem(position)*//*
            }

        })*/

        //NavigationUI.setupWithNavController(navLayout, navController)
        //println(navController.graph)
        //navLayout.setupWithNavController(navController)

        /*navLayout.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.all -> {
                    //viewPager.currentItem = pager.indexOfFirst { it is AllFragment }
                    navController.navigate(R.id.action_showsFragment_to_allFragment)
                }
                R.id.recent -> {
                    //viewPager.currentItem = pager.indexOfFirst { it is RecentFragment }
                    navController.navigate(R.id.action_showsFragment_to_recentFragment)
                }
                R.id.settings -> {
                    //viewPager.currentItem = pager.indexOfFirst { it is SettingsFragment }
                    navController.navigate(R.id.action_showsFragment_to_settingsFragment)
                }
            }
            true
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shows, container, false)
    }

}