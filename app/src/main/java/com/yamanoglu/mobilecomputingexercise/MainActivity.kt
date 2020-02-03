package com.yamanoglu.mobilecomputingexercise

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fab_time.setOnClickListener {
            startActivity(Intent(this, TimeActivity::class.java))
        }
        fab_map.setOnClickListener{
            startActivity(Intent(this, MapActivity::class.java))
        }

        var fabopened = false
        fab.setOnClickListener {
            if (!fabopened){
                fabopened = true
                fab_time.animate().translationY(-resources.getDimension(R.dimen.standart_66))
                fab_map.animate().translationY(-resources.getDimension(R.dimen.standart_116))
            }else{
                fabopened = false
                fab_time.animate().translationY(0f)
                fab_map.animate().translationY(0f)

            }
        }

        val data = listOf("Oulu", "Helsinki", "Tampere")
        val adapter = ReminderAdapter(this, data)
        lv_main.adapter = adapter

    }
}
