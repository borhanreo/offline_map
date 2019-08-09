package com.peterlaurence.trekme.ui.record.components

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.peterlaurence.trekme.R
import com.peterlaurence.trekme.core.map.maploader.MapLoader
import com.peterlaurence.trekme.ui.record.components.events.*
import com.peterlaurence.trekme.ui.tools.RecyclerItemClickListener
import com.peterlaurence.trekme.viewmodel.record.RecordingData
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.util.*

/**
 * List of recordings. It displays each recordings showing only the file name at first, then adds
 * the statistics.
 *
 * @author peterLaurence on 23/12/17 -- Converted to Kotlin on 30/09/18
 */
class RecordListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : CardView(context, attrs, defStyleAttr) {
    private var isMultiSelectMode = false
    private var selectedRecordings = ArrayList<File>()
    private lateinit var recordingAdapter: RecordingAdapter
    private val recordingDataList = arrayListOf<RecordingData>()

    init {
        init(context)
    }

    fun setRecordingData(data: List<RecordingData>) {
        recordingDataList.clear()
        /* For instance, only fill the file attribute, the gpx data will be retrieved later */
        recordingDataList.addAll(data)

        /* Update the recycle view */
        recordingAdapter.setRecordingsData(recordingDataList)
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.record_list_layout, this)

        val ctx = getContext()
        val recyclerView = findViewById<RecyclerView>(R.id.recordings_recycler_id)
        val editNameButton = findViewById<ImageButton>(R.id.edit_recording_button)
        val importButton = findViewById<ImageButton>(R.id.import_track_button)
        val shareButton = findViewById<ImageButton>(R.id.share_track_button)
        val deleteRecordingButton = findViewById<ImageButton>(R.id.delete_recording_button)

        editNameButton.isEnabled = false
        editNameButton.setOnClickListener {
            if (selectedRecordings.size == 1) {
                val recording = selectedRecordings[0]
                EventBus.getDefault().post(RequestEditRecording(recording))
            }
        }

        importButton.isEnabled = false
        importButton.setOnClickListener { EventBus.getDefault().post(RequestChooseMap()) }

        shareButton.isEnabled = false
        shareButton.setOnClickListener {
            if (selectedRecordings.size >= 1) {
                EventBus.getDefault().post(RequestShareRecording(selectedRecordings))
            }
        }

        deleteRecordingButton.setOnClickListener {
            EventBus.getDefault().post(RequestDeleteRecordings(selectedRecordings))

            /* Remove immediately the corresponding views (for better responsiveness) */
            for (file in selectedRecordings) {
                recordingDataList.removeAll { it.recording == file }
            }
            recordingAdapter.notifyDataSetChanged()
        }

        val llm = LinearLayoutManager(ctx)
        recyclerView.layoutManager = llm

        recordingAdapter = RecordingAdapter(recordingDataList, selectedRecordings)
        recyclerView.adapter = recordingAdapter

        recyclerView.addOnItemTouchListener(RecyclerItemClickListener(this.context,
                recyclerView, object : RecyclerItemClickListener.OnItemClickListener {
            private fun updateShareAndDeleteButtons() {
                if (selectedRecordings.isEmpty()) {
                    shareButton.isEnabled = false
                    shareButton.drawable.setTint(Color.GRAY)

                    deleteRecordingButton.visibility = View.GONE
                } else {
                    shareButton.isEnabled = true
                    shareButton.drawable.setTint(resources.getColor(R.color.colorAccent, null))

                    deleteRecordingButton.visibility = View.VISIBLE
                }
            }

            override fun onItemClick(view: View, position: Int) {
                if (isMultiSelectMode) {
                    multiSelect(position)

                    recordingAdapter.setSelectedRecordings(selectedRecordings)
                    recordingAdapter.notifyItemChanged(position)
                } else {
                    singleSelect(position)
                    editNameButton.isEnabled = true
                    editNameButton.drawable.setTint(resources.getColor(R.color.colorAccent, null))

                    importButton.isEnabled = true
                    importButton.drawable.setTint(resources.getColor(R.color.colorAccent, null))

                    recordingAdapter.setSelectedRecordings(selectedRecordings)
                    recordingAdapter.notifyDataSetChanged()
                }

                updateShareAndDeleteButtons()
            }

            override fun onItemLongClick(view: View, position: Int) {
                selectedRecordings = ArrayList()
                if (!isMultiSelectMode) {
                    editNameButton.isEnabled = false
                    editNameButton.drawable.setTint(Color.GRAY)
                    importButton.isEnabled = false
                    importButton.drawable.setTint(Color.GRAY)
                    multiSelect(position)
                    recordingAdapter.setSelectedRecordings(selectedRecordings)
                    recordingAdapter.notifyDataSetChanged()
                } else {
                    recordingAdapter.setSelectedRecordings(selectedRecordings)
                    recordingAdapter.notifyDataSetChanged()
                }
                isMultiSelectMode = !isMultiSelectMode

                updateShareAndDeleteButtons()
            }
        }))
    }

    private fun multiSelect(position: Int) {
        val recording = recordingDataList[position].recording
        if (selectedRecordings.contains(recording)) {
            selectedRecordings.remove(recording)
        } else {
            selectedRecordings.add(recording)
        }
    }

    private fun singleSelect(position: Int) {
        val recording = recordingDataList[position].recording
        selectedRecordings.clear()
        selectedRecordings.add(recording)
    }

    @Subscribe
    fun onMapSelectedForRecord(event: MapSelectedForRecord) {
        val map = MapLoader.getMap(event.mapId) ?: return
        val recording = selectedRecordings[0]

        EventBus.getDefault().post(RequestImportRecording(recording, map))

        /* Tell the user that the track will be shortly available in the map */
        val snackbar = Snackbar.make(rootView, R.string.track_is_being_added, Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    @Subscribe
    fun onRecordingDeletionFail(event: RecordingDeletionFailed) {
        /* Alert the user that some files could not be deleted */
        val snackbar = Snackbar.make(rootView, R.string.files_could_not_be_deleted,
                Snackbar.LENGTH_SHORT)
        snackbar.show()
    }
}
