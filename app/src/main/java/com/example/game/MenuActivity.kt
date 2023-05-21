package com.example.game

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.ktx.initialize


class MenuActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth;
    private lateinit var button: Button;
    private lateinit var textView: TextView;
    private  var user: FirebaseUser? = null;

    private var animationView: LottieAnimationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        Log.d("Debug", "Привет, мир!")

        animationView = findViewById(R.id.animationView)
        animationView?.setAnimation(R.raw.tumbnail)
        animationView?.playAnimation()

        val playButton: Button = findViewById(R.id.playButton)
        playButton.setOnClickListener {
            startGameNull()
        }

        val quizButton: Button = findViewById(R.id.quizButton)
        quizButton.setOnClickListener {
            startQuiz()
        }

        val xoButton: Button = findViewById(R.id.xoButton)
        xoButton.setOnClickListener {
            startXO()
        }

        // RegLogUser part
        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details);
        user = auth.currentUser;
//        if (user == null) {
//            val intent = Intent(applicationContext, Login::class.java);
//            startActivity(intent);
//            finish();
//        }
//        else {
//            val text = user!!.email?.removeSuffix("@whatever.ru")
//            textView.text = text;
//        }
        button.setOnClickListener(View.OnClickListener {
            onClick(it);
        });

    }

    override fun onClick(v: View?) {
        FirebaseAuth.getInstance().signOut();
        val intent = Intent(applicationContext, Login::class.java);
        startActivity(intent);
        finish();
    }


    private fun startGameNull() {
        Log.d("Debug", "Вход в игру 'Угадай число'!")
        val intent = Intent(this, GameNull::class.java)
        startActivity(intent)
        finish() // Закрываем меню после запуска игры
    }

    private fun startQuiz() {
        Log.d("Debug", "Вход в игру 'Викторина'!")
        val intent = Intent(this, Quiz::class.java)
        startActivity(intent)
        finish()
    }

    private fun startXO() {
        Log.d("Debug", "Вход в игру 'Крестики-нолики'!")
        val intent = Intent(this, TicTacToe::class.java)
        startActivity(intent)
        finish()
    }
}



