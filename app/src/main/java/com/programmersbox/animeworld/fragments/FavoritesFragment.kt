package com.programmersbox.animeworld.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding2.widget.textChanges
import com.programmersbox.anime_db.ShowDatabase
import com.programmersbox.anime_db.ShowDbModel
import com.programmersbox.anime_sources.Sources
import com.programmersbox.animeworld.R
import com.programmersbox.animeworld.databinding.FavoriteItemBinding
import com.programmersbox.animeworld.firebase.FirebaseDb
import com.programmersbox.animeworld.utils.AutoFitGridLayoutManager
import com.programmersbox.animeworld.utils.toShow
import com.programmersbox.dragswipe.DragSwipeAdapter
import com.programmersbox.dragswipe.DragSwipeDiffUtil
import com.programmersbox.gsonutils.toJson
import com.programmersbox.helpfulutils.layoutInflater
import com.programmersbox.rxutils.behaviorDelegate
import com.programmersbox.rxutils.toLatestFlowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_favorites.*
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 * Use the [FavoritesFragment] factory method to
 * create an instance of this fragment.
 */
class FavoritesFragment : BaseFragment() {

    /*override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }*/

    override val layoutId: Int get() = R.layout.fragment_favorites

    private val sourcePublisher = BehaviorSubject.createDefault(mutableListOf(*Sources.values()))
    private var sourcesList by behaviorDelegate(sourcePublisher)
    private val dao by lazy { ShowDatabase.getInstance(requireContext()).showDao() }
    private val disposable = CompositeDisposable()
    private val adapter by lazy { FavoriteAdapter() }
    private val fireListener = FirebaseDb.FirebaseListener()

    override fun viewCreated(view: View, savedInstanceState: Bundle?) {
        uiSetup()

        val fired = fireListener.getAllShowsFlowable()

        val dbFire = Flowables.combineLatest(
            fired,
            dao.getAllShow()
        ) { fire, db -> (db + fire).groupBy(ShowDbModel::showUrl).map { it.value.maxByOrNull(ShowDbModel::numEpisodes)!! } }

        Flowables.combineLatest(
            source1 = dbFire
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()),
            source2 = sourcePublisher.toLatestFlowable(),
            source3 = fav_search_info
                .textChanges()
                .debounce(500, TimeUnit.MILLISECONDS)
                .toLatestFlowable()
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { pair -> pair.first.sortedBy(ShowDbModel::title).filter { it.source in pair.second && it.title.contains(pair.third, true) } }
            .map { it.size to it.toGroup() }
            .distinctUntilChanged()
            .subscribe {
                adapter.setData(it.second.toList())
                fav_search_layout?.hint = resources.getQuantityString(R.plurals.numFavorites, it.first, it.first)
                favRv?.smoothScrollToPosition(0)
            }
            .addTo(disposable)
    }

    private fun uiSetup() {
        favRv.layoutManager = AutoFitGridLayoutManager(requireContext(), 360).apply { orientation = GridLayoutManager.VERTICAL }
        favRv.adapter = adapter
        favRv.setItemViewCacheSize(20)
        favRv.setHasFixedSize(true)

        sourceList.addView(Chip(requireContext()).apply {
            text = "ALL"
            isCheckable = true
            isClickable = true
            isChecked = true
            setOnClickListener { sourceList.children.filterIsInstance<Chip>().forEach { it.isChecked = true } }
        })

        Sources.values().forEach {
            sourceList.addView(Chip(requireContext()).apply {
                text = it.name
                isCheckable = true
                isClickable = true
                isChecked = true
                setOnCheckedChangeListener { _, isChecked -> addOrRemoveSource(isChecked, it) }
                setOnLongClickListener {
                    sourceList.clearCheck()
                    isChecked = true
                    true
                }
            })
        }
    }

    private fun List<ShowDbModel>.toGroup() = groupBy(ShowDbModel::title)

    private fun addOrRemoveSource(isChecked: Boolean, sources: Sources) {
        sourcesList = sourcesList?.apply { if (isChecked) add(sources) else remove(sources) }
    }

    private fun DragSwipeAdapter<Pair<String, List<ShowDbModel>>, *>.setData(newList: List<Pair<String, List<ShowDbModel>>>) {
        val diffCallback = object : DragSwipeDiffUtil<Pair<String, List<ShowDbModel>>>(dataList, newList) {
            override fun areContentsTheSame(oldItem: Pair<String, List<ShowDbModel>>, newItem: Pair<String, List<ShowDbModel>>): Boolean =
                oldItem.second == newItem.second

            override fun areItemsTheSame(oldItem: Pair<String, List<ShowDbModel>>, newItem: Pair<String, List<ShowDbModel>>): Boolean =
                oldItem.second === newItem.second
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        dataList.clear()
        dataList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class FavoriteAdapter : DragSwipeAdapter<Pair<String, List<ShowDbModel>>, FavoriteHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteHolder =
            FavoriteHolder(FavoriteItemBinding.inflate(requireContext().layoutInflater, parent, false))

        override fun FavoriteHolder.onBind(item: Pair<String, List<ShowDbModel>>, position: Int) = bind(item.second)
    }

    class FavoriteHolder(private val binding: FavoriteItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(info: List<ShowDbModel>) {
            binding.show = info.random()
            /*binding.root.setOnClickListener(
                Navigation.createNavigateOnClickListener(RecentFragmentDirections.actionRecentFragmentToShowInfoFragment(info.toJson()))
            )*/
            Glide.with(itemView.context)
                .asBitmap()
                .load(info.random().imageUrl)
                //.override(360, 480)
                .fitCenter()
                .transform(RoundedCorners(15))
                .fallback(R.mipmap.big_logo)
                .placeholder(R.mipmap.big_logo)
                .error(R.mipmap.big_logo)
                .into(binding.galleryListCover)

            binding.root.setOnClickListener {
                //println(navController.currentDestination)
                if (info.size == 1) {
                    binding.root.findNavController()
                        .navigate(
                            FavoritesFragmentDirections.actionFavoritesFragmentToShowInfoFragment3(
                                info.first().toShow().toJson(),
                                info.first().toShow()
                            )
                        )
                } else {
                    MaterialAlertDialogBuilder(itemView.context)
                        .setTitle("Choose Source")
                        .setItems(info.map { "${it.source} - ${it.title}" }.toTypedArray()) { d, i ->
                            binding.root.findNavController()
                                .navigate(
                                    FavoritesFragmentDirections.actionFavoritesFragmentToShowInfoFragment3(
                                        info[i].toShow().toJson(),
                                        info[i].toShow()
                                    )
                                )
                            d.dismiss()
                        }
                        .show()
                }
            }
            binding.executePendingBindings()
        }

    }

    override fun onDestroy() {
        disposable.dispose()
        fireListener.unregister()
        super.onDestroy()
    }

}
