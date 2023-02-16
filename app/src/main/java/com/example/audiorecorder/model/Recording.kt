package com.example.audiorecorder.model

import java.io.Serializable

data class Recording(var uri: String, var fileName: String, var isPlaying: Boolean) : Serializable {}