package com.programmersbox.animeworld.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.programmersbox.animeworld.R
import com.programmersbox.animeworld.utils.DeleteDialog
import com.programmersbox.animeworld.utils.folderLocation
import com.programmersbox.dragswipe.*
import com.programmersbox.helpfulutils.layoutInflater
import com.programmersbox.helpfulutils.requestPermissions
import com.programmersbox.helpfulutils.stringForTime
import com.programmersbox.loggingutils.Loged
import com.programmersbox.loggingutils.f
import kotlinx.android.synthetic.main.fragment_view_videos.*
import kotlinx.android.synthetic.main.video_layout.view.*
import kotlinx.android.synthetic.main.video_with_text.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 * Use the [ViewVideosFragment] factory method to
 * create an instance of this fragment.
 */
class ViewVideosFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_videos, container, false)
    }

    private val adapter by lazy { VideoAdapter(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoRv.adapter = adapter
        DragSwipeUtils.setDragSwipeUp(
            adapter,
            videoRv,
            listOf(Direction.NOTHING),
            listOf(Direction.START, Direction.END),
            DragSwipeActionBuilder {
                onSwiped { viewHolder, _, dragSwipeAdapter ->
                    val listener: DeleteDialog.DeleteDialogListener = object : DeleteDialog.DeleteDialogListener {
                        override fun onDelete() {
                            val file = dragSwipeAdapter.removeItem(viewHolder.adapterPosition)
                            if (file.exists()) {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                    val deleteRequest = MediaStore.createDeleteRequest(requireContext().contentResolver, listOf(file.toUri()))
                                    startIntentSenderForResult(
                                        deleteRequest.intentSender,
                                        123,
                                        null,
                                        0,
                                        0,
                                        0,
                                        null
                                    )
                                } else {
                                    Toast.makeText(requireContext(), if (file.delete()) "File Deleted" else "File Not Deleted", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }

                        }

                        override fun onCancel() {
                            dragSwipeAdapter.notifyDataSetChanged()
                        }
                    }
                    DeleteDialog(
                        context,
                        dragSwipeAdapter[viewHolder.adapterPosition].name,
                        null,
                        dragSwipeAdapter.dataList[viewHolder.adapterPosition],
                        listener
                    ).show()

                    //TODO: Clean up VideoPlayerActivity
                    //TODO: Add favorites to recent/all items [need to add the action]
                    //TODO: maybe add a watch list?
                    //TODO: maybe add a watch later?
                    //TODO: maybe put in live chart data?
                    //TODO: add multi-delete to video viewer
                    //TODO: Add TileService
                    //TODO: Add app info to settings
                    //TODO: Get an actual search working
                }
            }
        )

        loadVideos()
        view_video_refresh.setOnRefreshListener { loadVideos() }

        multiple_video_delete.setOnClickListener {
            val downloadItems = mutableListOf<File>()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete")
                .setMultiChoiceItems(adapter.dataList.map { it.name }.toTypedArray(), null) { _, i, b ->
                    if (b) downloadItems.add(adapter.dataList[i]) else downloadItems.remove(adapter.dataList[i])
                }
                .setPositiveButton("Delete") { d, _ ->
                    downloadItems.forEach { f ->
                        f.delete()
                        adapter.notifyDataSetChanged()
                    }
                    //adapter.notifyDataSetChanged()
                    d.dismiss()
                }
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            123 -> {
                Toast.makeText(requireContext(), if (resultCode == Activity.RESULT_OK) "File Deleted" else "File Not Deleted", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun loadVideos() {
        view_video_refresh.isRefreshing = true
        val permissions = listOfNotNull(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
        activity?.requestPermissions(*permissions.toTypedArray()) {
            if (it.isGranted) getStuff()
        }
    }

    private fun getStuff() {
        GlobalScope.launch {
            val files = getListFiles2(File(requireContext().folderLocation))

            //to get rid of any preferences of any videos that have been deleted else where
            val prefs = requireContext().getSharedPreferences("videos", Context.MODE_PRIVATE).all.keys
            val fileRegex = "(\\/[^*|\"<>?\\n]*)|(\\\\\\\\.*?\\\\.*)".toRegex()
            val filePrefs = prefs.filter { fileRegex.containsMatchIn(it) }
            for (p in filePrefs) {
                //Loged.i(p)
                if (!files.any { it.path == p }) {
                    requireContext().getSharedPreferences("videos", Context.MODE_PRIVATE).edit().remove(p).apply()
                }
            }
            Loged.f(files)

            requireActivity().runOnUiThread {
                adapter.setListNotify(files)
                view_video_refresh.isRefreshing = false
            }
        }
    }

    private fun getListFiles2(parentDir: File): ArrayList<File> {
        val inFiles = arrayListOf<File>()
        val files = LinkedList<File>()
        files.addAll(parentDir.listFiles())
        while (!files.isEmpty()) {
            val file = files.remove()
            if (file.isDirectory) {
                files.addAll(file.listFiles())
            } else if (file.name.endsWith(".mp4")) {
                inFiles.add(file)
            }
        }
        return inFiles
    }

    class VideoAdapter(private val context: Context) : DragSwipeAdapter<File, VideoHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder =
            VideoHolder(context.layoutInflater.inflate(R.layout.video_layout, parent, false))

        @SuppressLint("SetTextI18n")
        override fun VideoHolder.onBind(item: File, position: Int) {
            val duration = try {
                val retriever = MediaMetadataRetriever()
                //use one of overloaded setDataSource() functions to set your data source
                retriever.setDataSource(context, Uri.fromFile(item))
                val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                retriever.release()
                time?.toLong()
            } catch (e: Exception) {
                try {
                    val mp = MediaPlayer.create(context, Uri.parse(item.path))
                    mp.duration.toLong()
                } catch (e: Exception) {
                    null
                }
            } ?: 0L

            /*convert millis to appropriate time*/
            val runTimeString = if (duration > TimeUnit.HOURS.toMillis(1)) {
                String.format(
                    "%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(duration),
                    TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                    TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                )
            } else {
                String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                )
            }

            itemView.video_runtime.text = runTimeString
            itemView.video_name.text = "${item.name} ${
                if (context.getSharedPreferences("videos", Context.MODE_PRIVATE).contains(item.path)) "\nat ${
                    context.getSharedPreferences(
                        "videos",
                        Context.MODE_PRIVATE
                    ).getLong(item.path, 0).stringForTime()
                }" else ""
            }"

            itemView.setOnClickListener(
                Navigation.createNavigateOnClickListener(
                    ViewVideosFragmentDirections.actionViewVideosFragmentToVideoPlayerActivity(
                        showPath = item.absolutePath,
                        showName = item.name,
                        downloadOrStream = true,
                    )
                )
            )

            Glide.with(context)
                .load(item)
                .override(360, 270)
                .thumbnail(0.5f)
                .transform(RoundedCorners(15))
                .into(itemView.video_thumbnail)

        }

    }

    class VideoHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}