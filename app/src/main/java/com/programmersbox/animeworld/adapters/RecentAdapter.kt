package com.programmersbox.animeworld.adapters

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.programmersbox.anime_sources.ShowInfo
import com.programmersbox.animeworld.R
import com.programmersbox.animeworld.databinding.RecentItemBinding
import com.programmersbox.animeworld.fragments.ShowInfoFragment
import com.programmersbox.dragswipe.DragSwipeAdapter
import com.programmersbox.helpfulutils.layoutInflater
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.view.*

class RecentAdapter(private val context: Fragment, private val disposable: CompositeDisposable) : DragSwipeAdapter<ShowInfo, RecentHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentHolder =
        RecentHolder(RecentItemBinding.inflate(context.layoutInflater, parent, false))

    override fun RecentHolder.onBind(item: ShowInfo, position: Int) = bind(item, context, disposable)
}

class RecentHolder(private val binding: RecentItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(info: ShowInfo, context: Fragment, disposable: CompositeDisposable) {
        binding.show = info
        binding.root.setOnClickListener {
            val f = ShowInfoFragment(info, disposable)
            context.fragmentManager?.beginTransaction()
                //?.replace(R.id.container, f)
                ?.add(R.id.container, f)
                //?.add(f, null)
                //?.hide(context)
                ?.addToBackStack(null)
                ?.commit()
        }
        binding.executePendingBindings()
    }

}