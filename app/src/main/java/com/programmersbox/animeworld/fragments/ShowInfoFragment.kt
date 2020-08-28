package com.programmersbox.animeworld.fragments

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.ncorti.slidetoact.SlideToActView
import com.programmersbox.anime_db.EpisodeWatched
import com.programmersbox.anime_db.ShowDatabase
import com.programmersbox.anime_sources.Episode
import com.programmersbox.anime_sources.EpisodeInfo
import com.programmersbox.anime_sources.ShowInfo
import com.programmersbox.anime_sources.Sources
import com.programmersbox.animeworld.R
import com.programmersbox.animeworld.databinding.ChapterItemBinding
import com.programmersbox.animeworld.databinding.FragmentShowInfoBinding
import com.programmersbox.animeworld.firebase.FirebaseDb
import com.programmersbox.animeworld.utils.downloadOrStream
import com.programmersbox.animeworld.utils.downloadOrStreamPublish
import com.programmersbox.animeworld.utils.folderLocation
import com.programmersbox.animeworld.utils.toShowModel
import com.programmersbox.dragswipe.CheckAdapter
import com.programmersbox.dragswipe.CheckAdapterInterface
import com.programmersbox.dragswipe.DragSwipeAdapter
import com.programmersbox.dragswipe.get
import com.programmersbox.flowutils.collectOnUi
import com.programmersbox.flowutils.invoke
import com.programmersbox.gsonutils.fromJson
import com.programmersbox.gsonutils.toJson
import com.programmersbox.helpfulutils.Range
import com.programmersbox.helpfulutils.animateChildren
import com.programmersbox.helpfulutils.requestPermissions
import com.programmersbox.thirdpartyutils.ChromeCustomTabTransformationMethod
import com.programmersbox.thirdpartyutils.changeTint
import com.programmersbox.thirdpartyutils.check
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.Priority
import com.tonyodev.fetch2.Request
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_show_info.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [ShowInfoFragment] factory method to
 * create an instance of this fragment.
 */
class ShowInfoFragment : Fragment() {

    private val disposable: CompositeDisposable = CompositeDisposable()
    private val adapter: ChapterAdapter by lazy { ChapterAdapter() }

    private lateinit var binding: FragmentShowInfoBinding

    private val dao by lazy { ShowDatabase.getInstance(requireContext()).showDao() }
    private val isFavorite = MutableStateFlow(false)

    private val fetch = Fetch.getDefaultInstance()
    private val showListener = FirebaseDb.FirebaseListener()
    private val episodesListener = FirebaseDb.FirebaseListener()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentShowInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val args: ShowInfoFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println(requireActivity().intent.data)
        val showBuilder = requireActivity().intent.data?.let { url ->
            Sources.getSourceByUrl(url.toString())?.let { s ->
                ShowInfo(url.lastPathSegment.toString(), url.toString(), s)
            }
        }?.toJson()

        println(showBuilder)

        viewDownloads.setOnClickListener { findNavController().navigate(R.id.action_showInfoFragment_to_downloadViewerActivity) }

        favoriteshow.changeTint(viewDownloads.currentTextColor)
        isFavorite.collectOnUi { favoriteshow.check(it) }

        showUrl.transformationMethod = ChromeCustomTabTransformationMethod(requireContext()) {
            //setStartAnimations(this@ShowInfoFragment, com.programmersbox.helpfulutils.R.anim.slide_in_right, com.programmersbox.helpfulutils.R.anim.slide_out_left)
            //swatch?.rgb?.let { setToolbarColor(it) }
            addDefaultShareMenuItem()
        }
        showUrl.movementMethod = LinkMovementMethod.getInstance()

        showInfoChapterList.adapter = adapter

        println(args)
        println(args.showInfo)

        (showBuilder ?: args.showInfo)?.fromJson<ShowInfo>()?.getEpisodeInfo()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.doOnError {
                FirebaseCrashlytics.getInstance().recordException(it)
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Very sorry but this show doesn't work")
                    .setMessage(it.message)
                    .setPositiveButton("Return") { d, _ ->
                        d.dismiss()
                        requireActivity().onBackPressed()
                    }
                    .setNegativeButton("Stay to View Information") { d, _ -> d.dismiss() }
                    .show()
            }
            ?.subscribeBy {
                println(it)
                binding.show = it
                binding.executePendingBindings()
                adapter.addItems(it.episodes)
                activity?.actionBar?.title = it.name
                adapter.showUrl = it.source.url
                showSetup(it)
                shareButton.setOnClickListener { _ -> shareShow(it) }
            }?.addTo(disposable)

        moreInfoSetup()

    }

    private fun shareShow(episode: Episode) {
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, episode.source.url)
            putExtra(Intent.EXTRA_TITLE, episode.name)
        }, "Share ${episode.name}"))
    }

    private fun showSetup(episode: Episode) {
        favoriteshow.setOnClickListener {

            fun addShow(show: Episode) {
                Completable.concatArray(
                    FirebaseDb.insertShow(show.toShowModel()),
                    dao.insertShow(show.toShowModel()).subscribeOn(Schedulers.io())
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { isFavorite(true) }
                    .addTo(disposable)
            }

            fun removeShow(show: Episode) {
                Completable.concatArray(
                    FirebaseDb.removeShow(show.toShowModel()),
                    dao.deleteShow(show.toShowModel()).subscribeOn(Schedulers.io())
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { isFavorite(false) }
                    .addTo(disposable)
            }

            if (isFavorite()) removeShow(episode) else addShow(episode)
        }

        Flowables.combineLatest(
            showListener.findShowByUrl(episode.source.url),
            dao.getShowByIdFlow(episode.source.url)
        ) { f, d -> f || d > 0 }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(isFavorite::invoke)
            .addTo(disposable)

        Flowables.combineLatest(
            episodesListener.getAllEpisodesByShow(episode.toShowModel()),
            dao.getWatchedEpisodesById(episode.source.url).subscribeOn(Schedulers.io())
        ) { f, d -> (f + d).distinctBy { it.url } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            //.distinct { it }
            .subscribe { adapter.update(it) { c, m -> c.url == m.url } }
            .addTo(disposable)

        markChapters.setOnClickListener {
            val checked = adapter.currentList
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Mark Episodes As...")
                .setMultiChoiceItems(
                    episode.episodes.map { it.name }.toTypedArray(),
                    BooleanArray(adapter.itemCount) { i -> checked.any { it.url == adapter[i].url } }
                ) { _, i, _ ->
                    (showInfoChapterList.findViewHolderForAdapterPosition(i) as? ChapterAdapter.ChapterHolder)?.watchedButton?.performClick()
                }
                .setPositiveButton("Done") { d, _ -> d.dismiss() }
                .show()
        }

        moreOptions.setOnClickListener { v ->
            PopupMenu(requireContext(), v).apply {
                inflate(R.menu.show_info_menu)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.downloadMultiple -> {
                            val downloadItems = mutableListOf<EpisodeInfo>()
                            val eps = episode.episodes
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Batch Download")
                                .setMultiChoiceItems(eps.map { it.name }.toTypedArray(), null) { _, i, b ->
                                    if (b) downloadItems.add(eps[i]) else downloadItems.remove(eps[i])
                                }
                                .setPositiveButton("Start Download") { d, _ ->
                                    activity?.requestPermissions(
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    ) {
                                        if (it.isGranted) {
                                            downloadItems.forEach { GlobalScope.launch { fetchIt(it) } }
                                        }
                                    }
                                    d.dismiss()
                                }
                                .show()
                        }
                    }
                    true
                }
            }.show()

        }

    }

    private fun moreInfoSetup() {
        var set = ConstraintRangeSet(
            showInfoFullLayout,
            ConstraintRanges(
                showInfoFullLayout,
                ConstraintSet().apply { clone(showInfoFullLayout) },
                ConstraintSet().apply { clone(this@ShowInfoFragment.requireContext(), R.layout.fragment_show_info_alt) }
            ),
            ConstraintRanges(
                showInfoLayout,
                ConstraintSet().apply { clone(showInfoLayout) },
                ConstraintSet().apply { clone(this@ShowInfoFragment.requireContext(), R.layout.show_info_detail_layout_alt) }
            )
        )
        moreInfo.setOnClickListener { set++ }
    }

    private class ConstraintRangeSet(private val rootLayout: ConstraintLayout, vararg items: ConstraintRanges) : Range<ConstraintRanges>() {

        override val itemList: List<ConstraintRanges> = items.toList()

        override operator fun inc(): ConstraintRangeSet {
            super.inc()
            rootLayout.animateChildren {
                itemList.forEach {
                    it.inc()
                    it.item.applyTo(it.layout)
                }
            }
            return this
        }

        override operator fun dec(): ConstraintRangeSet {
            super.dec()
            rootLayout.animateChildren {
                itemList.forEach {
                    it.dec()
                    it.item.applyTo(it.layout)
                }
            }
            return this
        }

        override fun onChange(current: Int, item: ConstraintRanges) = Unit
    }

    private class ConstraintRanges(val layout: ConstraintLayout, vararg items: ConstraintSet) : Range<ConstraintSet>() {
        override val itemList: List<ConstraintSet> = items.toList()
        override fun onChange(current: Int, item: ConstraintSet) = Unit
    }

    inner class ChapterAdapter(check: CheckAdapter<EpisodeInfo, EpisodeWatched> = CheckAdapter()) :
        DragSwipeAdapter<EpisodeInfo, ChapterAdapter.ChapterHolder>(), CheckAdapterInterface<EpisodeInfo, EpisodeWatched> by check {
        init {
            check.adapter = this
        }

        var showUrl: String? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterHolder =
            ChapterHolder(ChapterItemBinding.inflate(layoutInflater, parent, false))

        override fun ChapterHolder.onBind(item: EpisodeInfo, position: Int) = bind(item, showUrl)

        inner class ChapterHolder(private val binding: ChapterItemBinding) : RecyclerView.ViewHolder(binding.root) {
            val watchedButton = binding.watchedButton
            fun bind(episodeInfo: EpisodeInfo, showUrl: String?) {
                binding.episode = episodeInfo
                binding.executePendingBindings()
                binding.watchedButton.setOnCheckedChangeListener(null)
                binding.watchedButton.isChecked = currentList.any { it.url == episodeInfo.url }
                binding.watchedButton.setOnCheckedChangeListener { _, b ->
                    showUrl?.let { EpisodeWatched(url = episodeInfo.url, name = episodeInfo.name, showUrl = it) }
                        ?.let {
                            Completable.mergeArray(
                                if (b) FirebaseDb.insertEpisodeWatched(it) else FirebaseDb.removeEpisodeWatched(it),
                                if (b) dao.insertEpisode(it) else dao.deleteEpisode(it)
                            )
                        }
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe {
                            Snackbar.make(
                                moreInfo,
                                if (b) "Added Episode" else "Removed Episode",
                                Snackbar.LENGTH_SHORT
                            )
                                .setAction("Undo") { binding.watchedButton.isChecked = !binding.watchedButton.isChecked }
                                .show()
                        }
                }

                downloadOrStreamPublish
                    .startWith(itemView.context.downloadOrStream)
                    .subscribe { binding.okayToDownload.text = if (it) "Download" else "Stream" }
                    .addTo(disposable)
                binding.okayToDownload.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
                    override fun onSlideComplete(view: SlideToActView) {
                        if (requireContext().downloadOrStream) {
                            downloadVideo(view, episodeInfo)
                        } else {
                            streamVideo(view, episodeInfo)
                        }
                    }
                }
            }

            private fun downloadVideo(view: SlideToActView, episodeInfo: EpisodeInfo) {
                activity?.requestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE) {
                    if (it.isGranted) {
                        GlobalScope.launch {
                            fetchIt(episodeInfo)
                            delay(500)
                            activity?.runOnUiThread {
                                view.resetSlider()
                                binding.watchedButton.isChecked = true
                            }
                        }
                    }
                }
            }

            private fun streamVideo(view: SlideToActView, episodeInfo: EpisodeInfo) {
                GlobalScope.launch {
                    val link = episodeInfo.getVideoLink().blockingGet()
                    delay(500)
                    activity?.runOnUiThread {
                        view.resetSlider()
                        binding.watchedButton.isChecked = true
                        link.firstOrNull()?.let {
                            findNavController()
                                .navigate(
                                    ShowInfoFragmentDirections.actionShowInfoFragment2ToVideoPlayerActivity2(
                                        it.link.orEmpty(),
                                        episodeInfo.name,
                                        false
                                    )
                                )
                        }
                    }
                }
            }
        }
    }

    private fun fetchIt(ep: EpisodeInfo) {

        fetch.setGlobalNetworkType(NetworkType.ALL)

        fun getNameFromUrl(url: String): String {
            return Uri.parse(url).lastPathSegment?.let { if (it.isNotEmpty()) it else ep.name } ?: ep.name
        }

        val requestList = arrayListOf<Request>()
        val url = ep.getVideoLink().blockingGet()
        for (i in url) {

            val filePath = requireContext().folderLocation + getNameFromUrl(i.link!!) + "${ep.name}.mp4"
            //Loged.wtf("${File(filePath).exists()}")
            val request = Request(i.link!!, filePath)
            request.priority = Priority.HIGH
            request.networkType = NetworkType.ALL
            //request.enqueueAction = EnqueueAction.DO_NOT_ENQUEUE_IF_EXISTING
            request.extras.map.toProperties()["URL_INTENT"] = ep.url
            request.extras.map.toProperties()["NAME_INTENT"] = ep.name

            request.addHeader("Accept-Language", "en-US,en;q=0.5")
            request.addHeader("User-Agent", "\"Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0\"")
            request.addHeader("Accept", "text/html,video/mp4,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            request.addHeader("Access-Control-Allow-Origin", "*")
            request.addHeader("Referer", "http://thewebsite.com")
            request.addHeader("Connection", "keep-alive")

            requestList.add(request)

        }
        fetch.enqueue(requestList) {}
    }

    override fun onDestroy() {
        disposable.dispose()
        showListener.unregister()
        episodesListener.unregister()
        super.onDestroy()
    }

}
