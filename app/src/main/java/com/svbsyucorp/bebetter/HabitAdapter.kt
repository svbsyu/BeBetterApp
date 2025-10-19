package com.svbsyucorp.bebetter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.svbsyucorp.bebetter.Habit

class HabitAdapter(
    private val habits: MutableList<Habit>,
    private val onDeleteHabit: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.habit_item, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.bind(habit)
    }

    override fun getItemCount(): Int = habits.size

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)
        private val textViewHabitName: TextView = itemView.findViewById(R.id.textViewHabitName)
        private val buttonDeleteHabit: ImageButton = itemView.findViewById(R.id.buttonDeleteHabit)

        fun bind(habit: Habit) {
            textViewTime.text = String.format("%02d:%02d", habit.hour, habit.minute)
            textViewHabitName.text = habit.title
            buttonDeleteHabit.setOnClickListener {
                onDeleteHabit.invoke(habit)
            }
        }
    }
}