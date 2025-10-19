package com.svbsyucorp.bebetter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.svbsyucorp.bebetter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replacefragment(HabitFragment())
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId){
                R.id.habit -> replacefragment(HabitFragment())
                R.id.sleep -> replacefragment(SleepFragment())
                R.id.ruido -> replacefragment(RuidoFragment())
                else -> {

                }
            }
            true
        }
    }
    private fun replacefragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}