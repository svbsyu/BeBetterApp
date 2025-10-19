package com.svbsyucorp.bebetter

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class HabitFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private val habits = mutableListOf<Habit>()
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {

        } else {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_habit, container, false)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        recyclerView = view.findViewById(R.id.recyclerViewHabits)
        recyclerView.layoutManager = LinearLayoutManager(context)
        habitAdapter = HabitAdapter(
            habits,
            onDeleteHabit = {
                if (currentUser != null) {
                    deleteHabit(it)
                }
            }
        )
        recyclerView.adapter = habitAdapter

        view.findViewById<View>(R.id.buttonAddHabit).setOnClickListener {
            if (currentUser != null) {
                askNotificationPermission()
                showAddHabitDialog()
            } else {
                Toast.makeText(context, "Debes iniciar sesión para agregar un hábito.", Toast.LENGTH_SHORT).show()
            }
        }

        if (currentUser != null) {
            val userId = currentUser.uid
            database = FirebaseDatabase.getInstance().getReference("habits").child(userId)
            loadHabits()
        }

        return view
    }

    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_habit, null)
        val editTextHabitTitle = dialogView.findViewById<EditText>(R.id.editTextHabitTitle)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val checkLun = dialogView.findViewById<CheckBox>(R.id.check_lun)
        val checkMar = dialogView.findViewById<CheckBox>(R.id.check_mar)
        val checkMie = dialogView.findViewById<CheckBox>(R.id.check_mie)
        val checkJue = dialogView.findViewById<CheckBox>(R.id.check_jue)
        val checkVie = dialogView.findViewById<CheckBox>(R.id.check_vie)
        val checkSab = dialogView.findViewById<CheckBox>(R.id.check_sab)
        val checkDom = dialogView.findViewById<CheckBox>(R.id.check_dom)

        AlertDialog.Builder(requireContext())
            .setTitle("Agregar Hábito")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = editTextHabitTitle.text.toString()
                val hour = timePicker.hour
                val minute = timePicker.minute
                val days = mutableListOf<String>()
                if (checkLun.isChecked) days.add("Lun")
                if (checkMar.isChecked) days.add("Mar")
                if (checkMie.isChecked) days.add("Mie")
                if (checkJue.isChecked) days.add("Jue")
                if (checkVie.isChecked) days.add("Vie")
                if (checkSab.isChecked) days.add("Sab")
                if (checkDom.isChecked) days.add("Dom")


                if (title.isNotEmpty()) {
                    val habitId = database.push().key!!
                    val newHabit = Habit(habitId, title, hour, minute, days)
                    saveHabit(newHabit)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveHabit(habit: Habit) {
        database.child(habit.id).setValue(habit).addOnSuccessListener {
            scheduleHabitAlarm(habit)
        }
    }

    private fun loadHabits() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                habits.clear()
                for (habitSnapshot in snapshot.children) {
                    val habit = habitSnapshot.getValue(Habit::class.java)
                    habit?.let {
                        habits.add(it)
                        scheduleHabitAlarm(it)
                    }
                }
                habitAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun deleteHabit(habit: Habit) {
        database.child(habit.id).removeValue().addOnSuccessListener {
            cancelHabitAlarm(habit)
        }
    }

    private fun scheduleHabitAlarm(habit: Habit) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("HABIT_TITLE", habit.title)
        }

        habit.days.forEach { day ->
            val dayOfWeek = getDayOfWeek(day)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (habit.id + day).hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                set(Calendar.HOUR_OF_DAY, habit.hour)
                set(Calendar.MINUTE, habit.minute)
                set(Calendar.SECOND, 0)

                if (before(Calendar.getInstance())) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
            )
        }
    }

    private fun cancelHabitAlarm(habit: Habit) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        habit.days.forEach { day ->
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (habit.id + day).hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }
    }


    private fun getDayOfWeek(day: String): Int {
        return when (day) {
            "Lun" -> Calendar.MONDAY
            "Mar" -> Calendar.TUESDAY
            "Mie" -> Calendar.WEDNESDAY
            "Jue" -> Calendar.THURSDAY
            "Vie" -> Calendar.FRIDAY
            "Sab" -> Calendar.SATURDAY
            "Dom" -> Calendar.SUNDAY
            else -> -1
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}