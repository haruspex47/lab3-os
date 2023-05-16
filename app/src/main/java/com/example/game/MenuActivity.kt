package com.example.game

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView


class MenuActivity : AppCompatActivity() {

    private var animationView: LottieAnimationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        animationView = findViewById(R.id.animationView)
        animationView?.setAnimation(R.raw.tumbnail)
        animationView?.playAnimation()

        val playButton: Button = findViewById(R.id.playButton)
        playButton.setOnClickListener {
            startGame()
        }
    }

    private fun startGame() {
        val intent = Intent(this, GameNull::class.java)
        startActivity(intent)
        finish() // Закрываем меню после запуска игры
    }
}



