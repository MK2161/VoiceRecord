package com.example.audiorecorder

import android.view.View
import com.example.audiorecorder.model.Recording

interface OnClickListener {
    fun onClickPlay(view: View, record: Recording, recordingList: ArrayList<Recording>, position: Int)

}