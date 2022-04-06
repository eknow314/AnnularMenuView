package com.eknow.annularmenu.demo

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.eknow.annularmenu.AnnularMenuView
import com.eknow.annularmenu.listener.OnMenuClickListener
import com.eknow.annularmenu.listener.OnMenuLongClickListener
import com.eknow.annularmenu.listener.OnMenuTouchListener

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val annularMenuView = findViewById<AnnularMenuView>(R.id.annularMenuView)

        annularMenuView.setOnMenuClickListener(object : OnMenuClickListener {
            override fun OnMenuClick(position: Int) {
                Log.e(TAG, "onClick position: $position")
            }
        })

        annularMenuView.setOnMenuLongClickListener(object : OnMenuLongClickListener {
            override fun OnMenuLongClick(position: Int) {
                Log.e(TAG, "onLongClick position: $position")
            }

        })

        annularMenuView.setOnMenuTouchListener(object : OnMenuTouchListener {
            override fun OnTouch(event: MotionEvent?, position: Int) {
                Log.e(TAG, "onTouch position: $position  event: ${event?.action}")
            }

        })

        annularMenuView.setMenuDrawable(0, ContextCompat.getDrawable(this, R.drawable.ic_rotate_left))
        annularMenuView.setMenuDrawable(1, ContextCompat.getDrawable(this, R.drawable.ic_arrow_up))
        annularMenuView.setMenuDrawable(2, ContextCompat.getDrawable(this, R.drawable.ic_rotate_right))
    }

    companion object {
        const val TAG = "AnnularMenuView"
    }
}