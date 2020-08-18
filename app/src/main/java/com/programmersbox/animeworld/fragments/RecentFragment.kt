package com.programmersbox.animeworld.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.programmersbox.anime_sources.ShowInfo
import com.programmersbox.anime_sources.Sources
import com.programmersbox.animeworld.R
import com.programmersbox.animeworld.adapters.RecentAdapter
import com.programmersbox.animeworld.databinding.RecentItemBinding
import com.programmersbox.animeworld.utils.currentSource
import com.programmersbox.animeworld.utils.sourcePublish
import com.programmersbox.dragswipe.DragSwipeAdapter
import com.programmersbox.gsonutils.toJson
import com.programmersbox.helpfulutils.layoutInflater
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_recent.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [RecentFragment] factory method to
 * create an instance of this fragment.
 */
class RecentFragment : Fragment() {

    private val disposable: CompositeDisposable = CompositeDisposable()
    private val adapter: RecentAdapter by lazy { RecentAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //navController.setGraph(R.navigation.recent_nav)
        //println(navController.graph)
        recentAnimeList.adapter = adapter
        recentRefresh.isRefreshing = true
        //context?.currentSource?.let { sourceLoad(it) }
        sourcePublish
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { sourceLoad(it) }
            .addTo(disposable)

        recentRefresh.setOnRefreshListener { context?.currentSource?.let { sourceLoad(it) } }
    }

    private fun sourceLoad(sources: Sources) {
        println(sources)
        GlobalScope.launch {
            sources.getRecent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    adapter.setListNotify(it)
                    recentRefresh.isRefreshing = false
                }
                .addTo(disposable)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recent, container, false)
    }

    inner class RecentAdapter : DragSwipeAdapter<ShowInfo, RecentHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentHolder =
            RecentHolder(RecentItemBinding.inflate(requireContext().layoutInflater, parent, false))

        override fun RecentHolder.onBind(item: ShowInfo, position: Int) = bind(item)
    }

    inner class RecentHolder(private val binding: RecentItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(info: ShowInfo) {
            binding.show = info
            /*binding.root.setOnClickListener(
                Navigation.createNavigateOnClickListener(RecentFragmentDirections.actionRecentFragmentToShowInfoFragment(info.toJson()))
            )*/
            binding.root.setOnClickListener {
                //println(navController.currentDestination)
                val f = RecentFragmentDirections.actionRecentFragmentToShowInfoFragment(info.toJson())
                println(f)
                findNavController().navigate(f)
            }
            binding.executePendingBindings()
        }

    }

}