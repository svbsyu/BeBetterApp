package com.svbsyucorp.bebetter

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class RuidoService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val twoHoursInMillis = 2 * 60 * 60 * 1000L

    private val stopRunnable = Runnable {
        stopSelf() // Detiene el servicio
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                startSound()
                handler.postDelayed(stopRunnable, twoHoursInMillis)
            }
            ACTION_STOP -> {
                stopSound()
                handler.removeCallbacks(stopRunnable)
                stopSelf()
            }
        }
        return START_STICKY // El servicio se reinicia si el sistema lo mata
    }

    private fun startSound() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.white_noise)
            mediaPlayer?.isLooping = true
        }
        mediaPlayer?.start()
    }

    private fun stopSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSound()
        handler.removeCallbacks(stopRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Innecesario
    }

    companion object {
        const val ACTION_PLAY = "com.svbsyucorp.bebetter.ACTION_PLAY"
        const val ACTION_STOP = "com.svbsyucorp.bebetter.ACTION_STOP"
    }
}
