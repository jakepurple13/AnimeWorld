package com.programmersbox.animeworld.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.widget.textChanges
import com.programmersbox.anime_sources.ShowInfo
import com.programmersbox.anime_sources.Sources
import com.programmersbox.animeworld.R
import com.programmersbox.animeworld.databinding.RecentItemBinding
import com.programmersbox.animeworld.utils.currentSource
import com.programmersbox.animeworld.utils.sourcePublish
import com.programmersbox.dragswipe.DragSwipeAdapter
import com.programmersbox.dragswipe.DragSwipeDiffUtil
import com.programmersbox.gsonutils.toJson
import com.programmersbox.helpfulutils.layoutInflater
import com.programmersbox.helpfulutils.runOnUIThread
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_all.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 * Use the [AllFragment] factory method to
 * create an instance of this fragment.
 */
class AllFragment : BaseFragment() {

    private val disposable: CompositeDisposable = CompositeDisposable()
    private val adapter: RecentAdapter by lazy { RecentAdapter() }
    private val currentList = mutableListOf<ShowInfo>()

    override fun viewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        allAnimeList?.adapter = adapter
        allRefresh?.isRefreshing = true
        //context?.currentSource?.let { sourceLoad(it) }
        sourcePublish
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { sourceLoad(it) }
            .addTo(disposable)
        allRefresh?.setOnRefreshListener { context?.currentSource?.let { sourceLoad(it) } }
        search_info
            .textChanges()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .debounce(500, TimeUnit.MILLISECONDS)
            .map { requireContext().currentSource.searchList(it, currentList) }
            .subscribe {
                adapter.setData(it)
                activity?.runOnUiThread {
                    GlobalScope.launch {
                        activity?.runOnUiThread { allAnimeList?.smoothScrollToPosition(0) }
                        delay(500)
                        activity?.runOnUiThread { allAnimeList?.scrollToPosition(0) }
                    }
                    //allAnimeList?.scrollToPosition(0)
                    search_layout?.suffixText = "${it.size}"
                }
            }
            .addTo(disposable)
    }

    private fun sourceLoad(sources: Sources) {
        println(sources)
        GlobalScope.launch {
            sources.getList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    adapter.setListNotify(it)
                    currentList.clear()
                    currentList.addAll(it)
                    allRefresh?.isRefreshing = false
                    activity?.runOnUiThread {
                        search_layout?.suffixText = "${it.size}"
                        search_layout?.hint = "Search: ${requireContext().currentSource.name}"
                    }
                }
                .addTo(disposable)
        }
    }

    override val layoutId: Int get() = R.layout.fragment_all

    inner class RecentAdapter : DragSwipeAdapter<ShowInfo, RecentHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentHolder =
            RecentHolder(RecentItemBinding.inflate(requireContext().layoutInflater, parent, false))

        override fun RecentHolder.onBind(item: ShowInfo, position: Int) = bind(item)
    }

    class RecentHolder(private val binding: RecentItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(info: ShowInfo) {
            binding.show = info
            /*binding.root.setOnClickListener(
                Navigation.createNavigateOnClickListener(RecentFragmentDirections.actionRecentFragmentToShowInfoFragment(info.toJson()))
            )*/
            binding.root.setOnClickListener {
                //println(navController.currentDestination)
                val f = AllFragmentDirections.actionAllFragment2ToShowInfoFragment2(info.toJson())
                println(f)
                binding.root.findNavController().navigate(f)
            }
            binding.executePendingBindings()
        }

    }
}

fun DragSwipeAdapter<ShowInfo, *>.setData(newList: List<ShowInfo>) {
    val diffCallback = object : DragSwipeDiffUtil<ShowInfo>(dataList, newList) {
        override fun areContentsTheSame(oldItem: ShowInfo, newItem: ShowInfo): Boolean = oldItem.url == newItem.url
        override fun areItemsTheSame(oldItem: ShowInfo, newItem: ShowInfo): Boolean = oldItem.url === newItem.url
    }
    val diffResult = DiffUtil.calculateDiff(diffCallback)
    dataList.clear()
    dataList.addAll(newList)
    runOnUIThread { diffResult.dispatchUpdatesTo(this) }
}