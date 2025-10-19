package com.svbsyucorp.bebetter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class RuidoFragment : Fragment() {

    private lateinit var playStopButton: Button
    private var isServiceRunning = false

    private val serviceStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isServiceRunning = intent?.getBooleanExtra("is_running", false) ?: false
            updateUI()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ruido, container, false)
        playStopButton = view.findViewById(R.id.buttonPlayStop)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playStopButton.setOnClickListener {
            if (isServiceRunning) {
                stopSoundService()
            } else {
                startSoundService()
            }
        }
    }

    private fun startSoundService() {
        val intent = Intent(context, RuidoService::class.java).apply {
            action = RuidoService.ACTION_PLAY
        }
        context?.startService(intent)
        isServiceRunning = true
        updateUI()
    }

    private fun stopSoundService() {
        val intent = Intent(context, RuidoService::class.java).apply {
            action = RuidoService.ACTION_STOP
        }
        context?.startService(intent)
        isServiceRunning = false
        updateUI()
    }

    private fun updateUI() {
        if (isServiceRunning) {
            playStopButton.text = "Detener"
        } else {
            playStopButton.text = "Reproducir"
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            serviceStateReceiver,
            IntentFilter("ruido_service_status")
        )
        val intent = Intent(context, RuidoService::class.java).apply {
            action = "get_status"
        }
        context?.startService(intent)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(serviceStateReceiver)
    }
}
