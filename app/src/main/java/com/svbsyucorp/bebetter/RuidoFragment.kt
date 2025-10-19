package com.svbsyucorp.bebetter

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class RuidoFragment : Fragment() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private lateinit var playStopButton: Button
    private val handler = Handler(Looper.getMainLooper())


    private val twoHoursInMillis = 2 * 60 * 60 * 1000L // 2 horas en milisegundos

    private val stopRunnable = Runnable {
        stopSound()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ruido, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playStopButton = view.findViewById(R.id.buttonPlayStop)

        playStopButton.setOnClickListener {
            if (isPlaying) {
                stopSound()
            } else {
                playSound()
            }
        }
    }

    private fun playSound() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.white_noise)
            mediaPlayer?.isLooping = true // Para que se repita indefinidamente
        }

        mediaPlayer?.start()
        isPlaying = true
        playStopButton.text = "Detener"

        handler.postDelayed(stopRunnable, twoHoursInMillis) //para que se detenga en 2 horas
    }

    private fun stopSound() {
        mediaPlayer?.pause()
        mediaPlayer?.seekTo(0)
        isPlaying = false
        playStopButton.text = "Reproducir"
        handler.removeCallbacks(stopRunnable)
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(stopRunnable)
    }
}
