package com.example.myeaty

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RationDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ration_detail)

        val rationId = intent.getStringExtra("RATION_ID") ?: return

        val title = NativeLib.getRationTitle(rationId)
        val shortDesc = NativeLib.getRationShortDescription(rationId)
        val fullPlan = NativeLib.getRationFullPlan(rationId)

        Log.d("RationDetailActivity", "== Рацион $rationId ==")
        Log.d("RationDetailActivity", "Title: $title")
        Log.d("RationDetailActivity", "Short Desc: $shortDesc")
        Log.d("RationDetailActivity", "Full Plan: $fullPlan")

        findViewById<TextView>(R.id.ration_detail_title).text = title
        findViewById<TextView>(R.id.ration_detail_short_desc).text = shortDesc
        findViewById<TextView>(R.id.ration_detail_full_plan).text = fullPlan
    }
}
