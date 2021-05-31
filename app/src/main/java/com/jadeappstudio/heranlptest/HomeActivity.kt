package com.jadeappstudio.heranlptest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_predict_post.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btnPredictPost.setOnClickListener {
            startActivity(Intent(this@HomeActivity, PredictPostActivity::class.java))
        }

        btnPredictLaporan.setOnClickListener {
            startActivity(Intent(this@HomeActivity, PredictLaporanActivity::class.java))
        }

        btnToSOS.setOnClickListener {
            startActivity(Intent(this@HomeActivity, SOSActivity::class.java))
        }

        btnToLocation.setOnClickListener {
            startActivity(Intent(this@HomeActivity, LocationActivity::class.java))
        }
    }
}