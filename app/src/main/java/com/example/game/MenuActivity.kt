package com.example.game

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MenuActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth;
    private lateinit var button: Button;
    private lateinit var textView: TextView;
    private lateinit var user: FirebaseUser;

    private var animationView: LottieAnimationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        animationView = findViewById(R.id.animationView)
        animationView?.setAnimation(R.raw.tumbnail)
        animationView?.playAnimation()

        val playButton: Button = findViewById(R.id.playButton)
        playButton.setOnClickListener {
            startGameNull()
        }

        //RegLogUser part

        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details);
        user = auth.currentUser!!;
        if (user == null) {
            val intent = Intent(applicationContext, Login::class.java);
            startActivity(intent);
            finish();
        }
        else {
            textView.text = user.email;
        }
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
        // TODO("Проверить, что игрок вошёл @a1sarpi")
        val intent = Intent(this, GameNull::class.java)
        startActivity(intent)
        finish() // Закрываем меню после запуска игры
    }


}



