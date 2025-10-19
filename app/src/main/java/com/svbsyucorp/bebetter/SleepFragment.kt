package com.svbsyucorp.bebetter

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.concurrent.TimeUnit

class SleepFragment : Fragment() {

    private lateinit var sleepStatusTextView: TextView
    private lateinit var startSleepButton: Button
    private lateinit var sleepScoreTextView: TextView

    private var sleeping = false
    private var sleepStartTime: Long = 0

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var compName: ComponentName
    private val REQUEST_CODE_ENABLE_ADMIN = 1

    private val screenOnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_ON) {
                if (sleeping) {
                    stopSleep()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)

        devicePolicyManager = requireActivity().getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(requireActivity(), SleepDeviceAdminReceiver::class.java)

        sleepStatusTextView = view.findViewById(R.id.sleep_status_textview)
        startSleepButton = view.findViewById(R.id.start_sleep_button)
        sleepScoreTextView = view.findViewById(R.id.sleep_score_textview)

        startSleepButton.setOnClickListener {
            if (sleeping) {
                stopSleep()
            } else {
                checkAdminAndStartSleep()
            }
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sleeping) {
            requireActivity().unregisterReceiver(screenOnReceiver)
        }
    }

    private fun checkAdminAndStartSleep() {
        val active = devicePolicyManager.isAdminActive(compName)
        if (active) {
            startSleep()
        } else {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Se necesita permiso para bloquear la pantalla.")
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            if (resultCode == Activity.RESULT_OK) {
                startSleep()
            } else {
                Toast.makeText(requireContext(), "Permiso de administrador denegado.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startSleep() {
        sleeping = true
        sleepStartTime = SystemClock.elapsedRealtime()
        sleepStatusTextView.text = "Durmiendo..."
        startSleepButton.text = "Despertar"
        sleepScoreTextView.text = ""

        val intentFilter = IntentFilter(Intent.ACTION_SCREEN_ON)
        requireActivity().registerReceiver(screenOnReceiver, intentFilter)

        devicePolicyManager.lockNow()
    }

    private fun stopSleep() {
        if (!sleeping) return // Evita que se ejecute varias veces

        sleeping = false
        requireActivity().unregisterReceiver(screenOnReceiver)

        val sleepEndTime = SystemClock.elapsedRealtime()
        val sleepDurationMillis = sleepEndTime - sleepStartTime
        val sleepDurationHours = TimeUnit.MILLISECONDS.toHours(sleepDurationMillis)

        val sleepScore = when (sleepDurationHours) {
            in 7..8 -> "Excelente"
            in 6..9 -> "Bueno"
            in 5..10 -> "Regular"
            else -> "Pobre"
        }

        sleepStatusTextView.text = "Presiona el botón para empezar a dormir"
        startSleepButton.text = "Comenzar a Dormir"
        sleepScoreTextView.text = "Puntuación de sueño: $sleepScore"
    }
}