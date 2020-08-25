package com.programmersbox.animeworld.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.widget.textChanges
import com.programmersbox.anime_db.ShowDatabase
import com.programmersbox.anime_db.ShowDbModel
import com.programmersbox.anime_sources.ShowInfo
import com.programmersbox.anime_sources.Sources
import com.programmersbox.animeworld.R
import com.programmersbox.animeworld.databinding.RecentItemBinding
import com.programmersbox.animeworld.firebase.FirebaseDb
import com.programmersbox.animeworld.utils.EndlessScrollingListener
import com.programmersbox.animeworld.utils.currentSource
import com.programmersbox.animeworld.utils.sourcePublish
import com.programmersbox.dragswipe.CheckAdapter
import com.programmersbox.dragswipe.CheckAdapterInterface
import com.programmersbox.dragswipe.DragSwipeAdapter
import com.programmersbox.dragswipe.DragSwipeDiffUtil
import com.programmersbox.gsonutils.toJson
import com.programmersbox.helpfulutils.layoutInflater
import com.programmersbox.helpfulutils.runOnUIThread
import com.programmersbox.thirdpartyutils.changeTint
import com.programmersbox.thirdpartyutils.check
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Flowables
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
    private val dao by lazy { ShowDatabase.getInstance(requireContext()).showDao() }
    private val showListener = FirebaseDb.FirebaseListener()
    private var count = 1

    override fun viewCreated(view: View, savedInstanceState: Bundle?) {
        allAnimeList?.adapter = adapter
        allRefresh?.isRefreshing = true
        //context?.currentSource?.let { sourceLoad(it) }
        allAnimeList?.addOnScrollListener(object : EndlessScrollingListener(allAnimeList.layoutManager!!) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                //if (this@RecentFragment.requireContext().currentSource.canScroll && search_info.text.isNullOrEmpty())// loadNewManga()
                if (requireContext().currentSource.canScroll) {
                    count++
                    //loadMore(this@RecentFragment.requireContext().currentSource, count)
                    allRefresh.isRefreshing = true
                    context?.currentSource?.let { sourceLoad(it, count) }
                }
            }
        })
        sourcePublish
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                count = 1
                currentList.clear()
                adapter.setListNotify(emptyList())
                sourceLoad(it)
            }
            .addTo(disposable)
        allRefresh?.setOnRefreshListener { context?.currentSource?.let { sourceLoad(it, count) } }
        search_info
            .textChanges()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .debounce(500, TimeUnit.MILLISECONDS)
            .map { requireContext().currentSource.searchList(it, currentList) }
            .subscribe {
                adapter.setData(it)
                activity?.runOnUiThread {
                    /*GlobalScope.launch {
                        activity?.runOnUiThread { allAnimeList?.smoothScrollToPosition(0) }
                        delay(500)
                        activity?.runOnUiThread { allAnimeList?.scrollToPosition(0) }
                    }*/
                    //allAnimeList?.scrollToPosition(0)
                    search_layout?.suffixText = "${it.size}"
                }
            }
            .addTo(disposable)

        scrollToTop.setOnClickListener {
            GlobalScope.launch {
                activity?.runOnUiThread { allAnimeList?.smoothScrollToPosition(0) }
                delay(500)
                activity?.runOnUiThread { allAnimeList?.scrollToPosition(0) }
            }
        }
        Flowables.combineLatest(
            showListener.getAllShowsFlowable(),
            dao.getAllShow()
        ) { f, d -> (f + d).groupBy(ShowDbModel::showUrl).map { it.value.maxByOrNull(ShowDbModel::numEpisodes)!! } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.update(it) { s, d -> s.url == d.showUrl } }
            .addTo(disposable)
    }

    private fun sourceLoad(sources: Sources, page: Int = 1) {
        sources.getList(page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                adapter.addItems(it)
                //currentList.clear()
                currentList.addAll(it)
                allRefresh?.isRefreshing = false
                activity?.runOnUiThread {
                    search_layout?.suffixText = "${currentList.size}"
                    search_layout?.hint = "Search: ${requireContext().currentSource.name}"
                }
            }
            .addTo(disposable)
    }

    private fun DragSwipeAdapter<ShowInfo, *>.setData(newList: List<ShowInfo>) {
        val diffCallback = object : DragSwipeDiffUtil<ShowInfo>(dataList, newList) {
            override fun areContentsTheSame(oldItem: ShowInfo, newItem: ShowInfo): Boolean = oldItem.url == newItem.url
            override fun areItemsTheSame(oldItem: ShowInfo, newItem: ShowInfo): Boolean = oldItem.url === newItem.url
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        dataList.clear()
        dataList.addAll(newList)
        runOnUIThread { diffResult.dispatchUpdatesTo(this) }
    }

    override val layoutId: Int get() = R.layout.fragment_all

    inner class RecentAdapter(checkAdapter: CheckAdapter<ShowInfo, ShowDbModel> = CheckAdapter()) : DragSwipeAdapter<ShowInfo, RecentHolder>(),
        CheckAdapterInterface<ShowInfo, ShowDbModel> by checkAdapter {
        init {
            checkAdapter.adapter = this
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentHolder =
            RecentHolder(RecentItemBinding.inflate(requireContext().layoutInflater, parent, false))

        override fun RecentHolder.onBind(item: ShowInfo, position: Int) = bind(item, currentList)
    }

    class RecentHolder(private val binding: RecentItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(info: ShowInfo, list: List<ShowDbModel>) {
            binding.show = info
            /*binding.root.setOnClickListener(
                Navigation.createNavigateOnClickListener(RecentFragmentDirections.actionRecentFragmentToShowInfoFragment(info.toJson()))
            )*/
            binding.favoriteHeart.changeTint(binding.animeTitle.currentTextColor)
            binding.favoriteHeart.check(false)
            binding.favoriteHeart.check(list.any { it.showUrl == info.url })
            binding.root.setOnClickListener {
                //println(navController.currentDestination)
                binding.root.findNavController().navigate(AllFragmentDirections.actionAllFragment2ToShowInfoFragment2(info.toJson()))
            }
            binding.executePendingBindings()
        }

    }

    override fun onDestroy() {
        showListener.unregister()
        super.onDestroy()
    }
}
