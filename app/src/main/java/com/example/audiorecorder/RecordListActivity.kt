package com.example.audiorecorder

import android.content.ContextWrapper
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiorecorder.adapter.MyAdapter
import com.example.audiorecorder.model.Recording
import kotlinx.android.synthetic.main.item_recording.view.*
import kotlinx.android.synthetic.main.record_list.*
import java.io.File
import java.io.IOException


class RecordListActivity : AppCompatActivity(), OnClickListener {
    private var mediaPlayer: MediaPlayer? = null
    private var lastProgress = 0
    private val handler = Handler()
    private var isPlaying = false
    private var lastIndex = -1
    private var myAdapter: MyAdapter? = null

    override fun onClickPlay(view: View, record: Recording, recordingList: ArrayList<Recording>, position: Int) {
        playRecordItem(view, record, recordingList, position)
    }

    private fun playRecordItem(view: View, recordItem: Recording, recordingList: ArrayList<Recording>, position: Int) {
        val recording = recordingList[position]

        if (isPlaying) {
            stopPlaying()
            if (position == lastIndex) {
                recording.isPlaying = false
                stopPlaying()
                myAdapter?.notifyItemChanged(position)
            } else {
                markAllPaused(recordingList)
                recording.isPlaying = true
                myAdapter?.notifyItemChanged(position)
                startPlaying(recording.uri, recordItem, view.seekBar, position)
                lastIndex = position
            }
            seekUpdate(view)
        } else {
            if (recording.isPlaying) {
                recording.isPlaying = false
                stopPlaying()
            } else {
                startPlaying(recording.uri, recordItem, view.seekBar, position)
                recording.isPlaying = true
                view.seekBar.max = mediaPlayer?.duration ?: 0
            }
            myAdapter?.notifyItemChanged(position)
            lastIndex = position
        }
        manageSeekBar(view.seekBar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_list)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getAllRecordings()
    }

    private fun getAllRecordings() {
        val recordArrayList = ArrayList<Recording>()
        val contextWrapper = ContextWrapper(this)
        val music = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val path = music?.absolutePath + "/music/Audios/"
        val directory = File(path)
        val files = directory.listFiles()
        if (files != null) {
            for (i in files.indices) {
                val fileName = files[i].name
                val recordingUri = music?.absolutePath + "/music/Audios/" + fileName
                recordArrayList.add(Recording(recordingUri, fileName, false))
            }
            uiTvNoData.visibility = View.GONE
            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            myAdapter = MyAdapter(recordArrayList)
            myAdapter?.setListener(this)
            recyclerView.adapter = myAdapter
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun seekUpdate(itemView: View) {
        if (mediaPlayer != null) {
            val mCurrentPosition = mediaPlayer?.currentPosition
            itemView.seekBar.max = mediaPlayer?.duration ?: 0
            itemView.seekBar.progress = mCurrentPosition ?: 0
            lastProgress = mCurrentPosition ?: 0
        }
        handler.postDelayed( { seekUpdate(itemView) }, 100)
    }

    private fun manageSeekBar(seekBar: SeekBar?) {
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer?.seekTo(progress)

                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun stopPlaying() {
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mediaPlayer = null
        isPlaying = false
    }

    private fun startPlaying(uri: String?, audio: Recording?, seekBar: SeekBar?, position: Int) {
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer?.setDataSource(uri)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        } catch (e: IOException) {
            Log.e("LOG_TAG", "prepare() failed")
        }
        seekBar?.max = mediaPlayer?.duration ?: 0
        isPlaying = true

        mediaPlayer?.setOnCompletionListener {
            audio?.isPlaying = false
            myAdapter?.notifyItemChanged(position)
        }
    }

    private fun markAllPaused(recordingList: ArrayList<Recording>) {
        for (i in recordingList.indices) {
            recordingList[i].isPlaying = false
            recordingList[i] = recordingList[i]
        }
        myAdapter?.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        stopPlaying()
    }
}