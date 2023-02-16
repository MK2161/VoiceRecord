package com.example.audiorecorder

import android.Manifest
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.*
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var audio: MediaRecorder? = null
    private var media: MediaPlayer? = null
    private var fileName: String ? = null
    private var lastProgress = 0
    private var time:Long = 0
    private var playTime:Long = 0
    private val mHandler = Handler()
   // private val RECORD_AUDIO_REQUEST_CODE = 101
    private var isPlaying = false

    private val activityResultLauncher by lazy {
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        {
            validatePermission(it)
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBtRecord -> {
                prepareRecording()
                startRecording()
            }

            R.id.imgBtStop -> {
                prepareStop()
                stopRecording()
            }
            R.id.uiBtnPause -> {
                pauseRecording()
            }

            R.id.imgViewPlay -> {
                if (!isPlaying && fileName != null) {
                    isPlaying = true
                    startPlaying()
                } else {
                    isPlaying = false
                    stopPlaying()
                }
            }

            R.id.imgBtRecordList -> {
                startActivity(Intent(this, RecordListActivity::class.java))
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityResultLauncher
        checkMultiplePermission()

        imgBtRecord.setOnClickListener(this)
        imgBtStop.setOnClickListener(this)
        imgViewPlay.setOnClickListener(this)
        imgBtRecordList.setOnClickListener(this)
        uiBtnPause.setOnClickListener(this)
    }

    private fun prepareStop() {
        TransitionManager.beginDelayedTransition(llRecorder)
        imgBtRecord.visibility = View.VISIBLE
        imgBtStop.visibility = View.GONE
        uiRecentlyPlaySection.visibility = View.VISIBLE
        uiBtnPause.visibility = View.GONE
        uiTvRecentlyRecord.visibility = View.VISIBLE
    }


    private fun prepareRecording() {
        TransitionManager.beginDelayedTransition(llRecorder)
        imgBtRecord.visibility = View.GONE
        imgBtStop.visibility = View.VISIBLE
        uiRecentlyPlaySection.visibility = View.GONE
        uiBtnPause.visibility = View.VISIBLE
        uiTvRecentlyRecord.visibility = View.GONE

    }

    private fun stopPlaying() {
        if(!isPlaying){
            playTime = audioChronometer.base - SystemClock.elapsedRealtime()
        }
        try {
            media?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        media = null
        imgViewPlay.setImageResource(R.drawable.ic_play_circle)
       // audioChronometer.stop()
        time = audioChronometer.base - SystemClock.elapsedRealtime()
        audioChronometer.stop()
       // audioChronometer.base = SystemClock.elapsedRealtime()
    }

    private fun startRecording() {
        audio = MediaRecorder()
        audio?.setAudioSource(MediaRecorder.AudioSource.MIC)
        audio?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        var contextWrapper = ContextWrapper(this)
        val music = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val file = File(music?.absolutePath+ "/music/Audios")
       /* val root = android.os.Environment.getExternalStorageDirectory()
        val file = File(root.absolutePath + "/AndroidCodility/Audios")*/
        if (!file.exists()) {
            file.mkdirs()
        }

       // fileName = root.absolutePath + "/AndroidCodility/Audios/" + (System.currentTimeMillis().toString() + ".mp3")
        fileName = music?.absolutePath + "/music/Audios/" + (System.currentTimeMillis().toString() + ".mp3")
        Log.d("filename", fileName!!)
        audio?.setOutputFile(fileName)
        audio?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        try {
            audio?.prepare()
            audio?.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        lastProgress = 0
        seekBar.progress = 0
        stopPlaying()
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun pauseRecording(){
        if (!isPlaying){
            uiBtnPause.setImageResource(R.drawable.ic_pause_circle)
            audio?.pause()
            time = chronometer.base - SystemClock.elapsedRealtime()
            chronometer.stop()
            isPlaying = true
        } else {
            uiBtnPause.setImageResource(R.drawable.ic_play_circle)
            audio?.resume()
            chronometer.base =SystemClock.elapsedRealtime() + time
            chronometer.start()
            isPlaying = false
        }

    }



    private fun stopRecording() {
        try {
            audio?.stop()
            audio?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audio = null
        chronometer.stop()
        chronometer.base = SystemClock.elapsedRealtime()
        Toast.makeText(this, "Recording saved successfully.", Toast.LENGTH_SHORT).show()
    }


    private fun startPlaying() {
        audioChronometer.visibility = View.VISIBLE
        audioChronometer.base = SystemClock.elapsedRealtime()
        if(isPlaying){
            audioChronometer.base =SystemClock.elapsedRealtime() + playTime
        }
        audioChronometer.base =SystemClock.elapsedRealtime() + playTime
        media = MediaPlayer()
        try {
            media?.setDataSource(fileName)
            media?.prepare()
            media?.start()
        } catch (e: IOException) {
            Log.e("LOG_TAG", "prepare() failed")
        }

        imgViewPlay.setImageResource(R.drawable.ic_pause_circle)

        seekBar.progress = lastProgress
        media?.seekTo(lastProgress)
        seekBar.max = media?.duration ?: 0
        audioChronometer.start()
        seekBarUpdate()



        media?.setOnCompletionListener {
            imgViewPlay.setImageResource(R.drawable.ic_play_circle)
            isPlaying = false
            audioChronometer.stop()
            audioChronometer.base = SystemClock.elapsedRealtime()
            media?.seekTo(0)
        }


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (media != null && fromUser) {
                    audioChronometer.base =SystemClock.elapsedRealtime() - (media?.currentPosition ?: 0)
                    media?.seekTo(progress)
                    lastProgress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        if(!isPlaying){
            audioChronometer.visibility = View.GONE
        }
    }

    private var runnable: Runnable = Runnable { seekBarUpdate() }

    private fun seekBarUpdate() {
        if (media != null) {
            val currentPosition = media?.currentPosition
            seekBar.progress = currentPosition ?: 0
            lastProgress = currentPosition ?: 0
        }
        mHandler.postDelayed(runnable, 100)
    }
    private fun checkMultiplePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            validatePermission(mapOf("" to true) as MutableMap<String, Boolean>)
        } else {
            activityResultLauncher.launch(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            )
        }
    }

    private fun validatePermission(permission: Map<String, Boolean>) {
        permission.onEach { (_, permission) ->
            if (!permission) {
                finishAffinity()
                return Toast.makeText(this, "please Allow Permission", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
