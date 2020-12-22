package com.andre_max.tiktokclone

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.andre_max.tiktokclone.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    lateinit var navView: BottomNavigationView
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        navView = binding.navView
        navView.visibility = View.VISIBLE
        setUpNavigation()

        object : CountDownTimer(3000, 1000){
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                binding.splashFragment.visibility = View.GONE
            }
        }.start()


    }

    private fun setUpNavigation(){
        Timber.d("setUpNavigation called")
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

    }
}

