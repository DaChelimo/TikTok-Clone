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
//    lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        navView = binding.navView


        navView.visibility = View.GONE
        setUpNavigation()

        object : CountDownTimer(3000, 1000){
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                binding.splashFragment.visibility = View.GONE
                binding.navView.visibility = View.VISIBLE
            }
        }.start()

//        navView.setOnNavigationItemSelectedListener {
//            when (it.itemId) {
//                R.id.homeFragment -> {
//
//                    true
//                }
//
//                R.id.searchFragment -> {
//
//                    true
//                }
//
//                R.id.inboxFragment -> {
//
//                    true
//                }
//
//                R.id.createVideoFragment -> {
//
//                    true
//                }
//
//                R.id.meFragment -> {
//
//                    true
//                }
//
//                else -> {
//                    Timber.d("item id not caught")
//                    false
//                }
//            }
//        }

    }

    private fun setUpNavigation(){
        Timber.d("setUpNavigation called")
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as ViewHolder set of Ids because each
        // menu should be considered as top level destinations.
//        appBarConfiguration = AppBarConfiguration(setOf(
//            R.id.profileWithoutAccountFragment, R.id.searchFragment, R.id.inboxFragment))
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }
}

