package com.programmersbox.animeworld.adapters

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.programmersbox.anime_sources.ShowInfo
import com.programmersbox.animeworld.databinding.RecentItemBinding
import com.programmersbox.dragswipe.DragSwipeAdapter
import io.reactivex.disposables.CompositeDisposable

class RecentAdapter(private val context: Fragment, private val disposable: CompositeDisposable) : DragSwipeAdapter<ShowInfo, RecentHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentHolder =
        RecentHolder(RecentItemBinding.inflate(context.layoutInflater, parent, false))

    override fun RecentHolder.onBind(item: ShowInfo, position: Int) = bind(item, context, disposable)
}

class RecentHolder(private val binding: RecentItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(info: ShowInfo, context: Fragment, disposable: CompositeDisposable) {
        binding.show = info
        /*binding.root.setOnClickListener {
            //val f = ShowInfoFragment(info, disposable)
            *//*context.fragmentManager?.beginTransaction()
                ?.replace(R.id.container, f)
                //?.add(R.id.container, f)
                //?.add(f, null)
                //?.hide(context)
                ?.addToBackStack(null)
                ?.commit()*//*

            //f?.let { it1 -> context.findNavController().navigate(it1) }
        }*/
        /*binding.root.setOnClickListener(
            when (context) {
                is RecentFragment -> RecentFragmentDirections.actionRecentFragmentToShowInfoFragment(info.toJson())
                is AllFragment -> AllFragmentDirections.actionAllFragmentToShowInfoFragment(info.toJson())
                else -> null
            }?.let { it1 -> Navigation.createNavigateOnClickListener(it1) }
        )*/
        /*binding.root.setOnClickListener(
            Navigation.createNavigateOnClickListener(RecentFragmentDirections.actionRecentFragmentToShowInfoFragment(info.toJson()))
        )*/
        binding.root.setOnClickListener {
            //NavHostFragment.findNavController(context).navigate(RecentFragmentDirections.actionRecentFragmentToShowInfoFragment(info.toJson()))
        }
        binding.executePendingBindings()
    }

}