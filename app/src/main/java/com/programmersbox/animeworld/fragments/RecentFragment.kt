package com.programmersbox.animeworld.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.programmersbox.anime_db.ShowDatabase
import com.programmersbox.anime_db.ShowDbModel
import com.programmersbox.anime_sources.Episode
import com.programmersbox.anime_sources.ShowInfo
import com.programmersbox.anime_sources.Sources
import com.programmersbox.animeworld.R
import com.programmersbox.animeworld.databinding.RecentItemBinding
import com.programmersbox.animeworld.firebase.FirebaseDb
import com.programmersbox.animeworld.utils.currentSource
import com.programmersbox.animeworld.utils.sourcePublish
import com.programmersbox.animeworld.utils.toShowModel
import com.programmersbox.dragswipe.CheckAdapter
import com.programmersbox.dragswipe.CheckAdapterInterface
import com.programmersbox.dragswipe.DragSwipeAdapter
import com.programmersbox.gsonutils.toJson
import com.programmersbox.helpfulutils.layoutInflater
import com.programmersbox.thirdpartyutils.changeTint
import com.programmersbox.thirdpartyutils.check
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_recent.*

/**
 * A simple [Fragment] subclass.
 * Use the [RecentFragment] factory method to
 * create an instance of this fragment.
 */
class RecentFragment : BaseFragment() {

    private val disposable: CompositeDisposable = CompositeDisposable()
    private val adapter: RecentAdapter by lazy { RecentAdapter() }
    private val dao by lazy { ShowDatabase.getInstance(requireContext()).showDao() }
    private val showListener = FirebaseDb.FirebaseListener()

    override fun viewCreated(view: View, savedInstanceState: Bundle?) {
        //navController.setGraph(R.navigation.recent_nav)
        //println(navController.graph)
        recentAnimeList?.adapter = adapter
        recentRefresh?.isRefreshing = true
        //context?.currentSource?.let { sourceLoad(it) }
        sourcePublish
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .subscribe { sourceLoad(it) }
            .addTo(disposable)

        recentRefresh.setOnRefreshListener { context?.currentSource?.let { sourceLoad(it) } }

        Flowables.combineLatest(
            showListener.getAllShowsFlowable(),
            dao.getAllShow()
        ) { f, d -> (f + d).groupBy(ShowDbModel::showUrl).map { it.value.maxByOrNull(ShowDbModel::numEpisodes)!! } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.update(it) { s, d -> s.url == d.showUrl } }
            .addTo(disposable)
    }

    private fun sourceLoad(sources: Sources) {
        sources.getRecent()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                adapter.setListNotify(it)
                recentRefresh?.isRefreshing = false
            }
            .addTo(disposable)
    }

    private fun addShow(show: Episode) = Completable.concatArray(
        FirebaseDb.insertShow(show.toShowModel()),
        dao.insertShow(show.toShowModel()).subscribeOn(Schedulers.io())
    ).showAction()

    private fun removeShow(show: Episode) = Completable.concatArray(
        FirebaseDb.removeShow(show.toShowModel()),
        dao.deleteShow(show.toShowModel()).subscribeOn(Schedulers.io())
    ).showAction()

    private fun removeShow(show: ShowDbModel) = Completable.concatArray(
        FirebaseDb.removeShow(show),
        dao.deleteShow(show).subscribeOn(Schedulers.io())
    ).showAction()

    private fun Completable.showAction() = subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe()
        .addTo(disposable)

    override val layoutId: Int get() = R.layout.fragment_recent

    inner class RecentAdapter(checkAdapter: CheckAdapter<ShowInfo, ShowDbModel> = CheckAdapter()) : DragSwipeAdapter<ShowInfo, RecentHolder>(),
        CheckAdapterInterface<ShowInfo, ShowDbModel> by checkAdapter {
        init {
            checkAdapter.adapter = this
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentHolder =
            RecentHolder(RecentItemBinding.inflate(requireContext().layoutInflater, parent, false))

        override fun RecentHolder.onBind(item: ShowInfo, position: Int) = bind(item, currentList).also {
            /*binding.favoriteHeart.setOnClickListener {
                if (currentList.any { it.showUrl == item.url }) {
                    currentList.find { it.showUrl == item.url }?.let { removeShow(it) }
                } else {
                    GlobalScope.launch {
                        item.getEpisodeInfo()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy { addShow(it) }
                            .addTo(disposable)
                    }
                }
            }*/
        }
    }

    class RecentHolder(val binding: RecentItemBinding) : RecyclerView.ViewHolder(binding.root) {

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
                binding.root.findNavController().navigate(RecentFragmentDirections.actionRecentFragmentToShowInfoFragment(info.toJson()))
            }
            binding.executePendingBindings()
        }

    }

    override fun onDestroy() {
        showListener.unregister()
        super.onDestroy()
    }

}