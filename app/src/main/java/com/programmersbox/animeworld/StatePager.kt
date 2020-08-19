package com.programmersbox.animeworld

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter

class StatePager(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT), MutableList<Fragment> by mutableListOf() {
    //private val mFragmentList = mutableListOf<Fragment>()

    //operator fun Fragment.unaryPlus() = addFragment(this)
    //fun addFragments(vararg fragment: Fragment) = mFragmentList.addAll(fragment)
    //fun addFragment(fragment: Fragment) = mFragmentList.add(fragment)

    //operator fun get(index: Int) = getItem(index)
    override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE
    override fun getItem(p0: Int): Fragment = this[p0]//mFragmentList[p0]
    override fun getCount(): Int = size//mFragmentList.size
}
