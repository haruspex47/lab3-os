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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

var userId: String? = null

class MenuActivity : AppCompatActivity(), View.OnClickListener {

    //private lateinit var auth: FirebaseAuth;
    private lateinit var button: Button;
    private lateinit var textView: TextView;
    private lateinit var email: String;
    private lateinit var password: String;
    private  var user: FirebaseUser? = null;

    val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    // Аутентификация пользователя
    var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private var animationView: LottieAnimationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        Log.d("Debug", "Привет, мир!")

//        animationView = findViewById(R.id.animationView)
//        animationView?.setAnimation(R.raw.tumbnail)
//        animationView?.playAnimation()

//        val playButton: Button = findViewById(R.id.playButton)
//        playButton.setOnClickLКНБistener {
//            startGameNull()
//        }

        val quizButton: Button = findViewById(R.id.quizButton)
        quizButton.setOnClickListener {
            startQuiz()
        }

        val xoButton: Button = findViewById(R.id.xoButton)
        xoButton.setOnClickListener {
            startXO()
        }

        val knbButton: Button = findViewById(R.id.knbButton)
        knbButton.setOnClickListener {
            startKNB()
        }

//         RegLogUser part
        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details)
        user = auth.currentUser;
        if (user == null) {
            val intent = Intent(applicationContext, Login::class.java);
            startActivity(intent);
            finish();
        }
        else {
            email = user!!.email?.removeSuffix("@whatever.ru").toString()
            textView.text = email;
        }
        button.setOnClickListener(View.OnClickListener {
            onClick(it);
        })
//        val username = email
        val playerStatsButton: Button = findViewById(R.id.playerStatsButton)
        playerStatsButton.setOnClickListener {
            val intent = Intent(this, PlayerStatsActivity::class.java)
            startActivity(intent)
            finish()
        }
//
//        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
//            if (task.isSuccessful) {
//                // Регистрация успешна, сохраняем данные пользователя и обновляем game1Score
//                val userId = auth.currentUser?.uid
//                if (userId != null) {
//                    val userRef = database.child("users").child(userId)
//                    val gameStatsRef = userRef.child("gameStats")
//
//                    userRef.child("username").setValue(username)
//                    gameStatsRef.child("game1Score").setValue(0)
//                        .addOnCompleteListener { game1ScoreTask ->
//                            if (game1ScoreTask.isSuccessful) {
//                                // Успешно обновлено значение параметра "game1Score"
//                                // updateGame1Score(gameStatsRef)
//                            } else {
//                                // Обработка ошибок при обновлении значения параметра "game1Score"
//                            }
//                        }
//                }
//            } else {
//                // Обработка ошибок при регистрации пользователя
//            }
//        }

//        val loginButton: Button = findViewById(R.id.loginButton)
//        loginButton.setOnClickListener {
//            val email = "check@mail.ru"// получите значение email от пользователя
//            val password = "check1234567"// получите значение пароля от пользователя
//            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { signInTask ->
//                if (signInTask.isSuccessful) {
//                    val userId = auth.currentUser?.uid
//                    Log.d("Firebase", "$userId, вход успешен")
//                // Вход успешен
//                // Вы можете выполнить дополнительные действия после успешного входа пользователя, например, переход на другой экран
//                } else {
//                    Log.d("Firebase", "Не удалось войти: ${signInTask.exception}")
//                // Обработка ошибок при входе пользователя
//                }
//            }
//        }

//        val debugButton: Button = findViewById(R.id.debugButton)
//        debugButton.setOnClickListener {
//            // Получаем ссылку на базу данных Firebase
////            createDB()
//        }

//        // Запись статистики игры пользователя
//        val gameStatsRef = database.child("users").child(auth.currentUser?.uid ?: "").child("gameStats")
//        gameStatsRef.child("game1Score").setValue((0..100).random())
//        gameStatsRef.child("game2Score").setValue((0..100).random())
//        gameStatsRef.child("game3Score").setValue((0..100).random())

//        // Чтение статистики игры пользователя
//        gameStatsRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val game1Score = dataSnapshot.child("game1Score").value
//                val game2Score = dataSnapshot.child("game2Score").value
//                val game3Score = dataSnapshot.child("game3Score").value
//                // Обновление UI с полученной статистикой игр
//            }




//        // Получаем ссылку на корневой узел базы данных
//        val databaseRef = FirebaseDatabase.getInstance().reference
//
//        // Создаем новый узел "users" и получаем ссылку на него
//        val usersRef = databaseRef.child("users")

//        // Создаем новый уникальный идентификатор для пользователя
//        val userId = usersRef.push().key
//
//// Создаем объект данных пользователя
//        val userData = HashMap<String, Any>()
//        userData["username"] = "John"
//        userData["email"] = "check@mail.ru"
//        userData["password"] = "check1234567"
//        userData["age"] = 25
//
//// Создаем объект данных статистики игры
//        val gameStatsData = HashMap<String, Any>()
//        gameStatsData["game1Score"] = 1
//        gameStatsData["game2Score"] = 2
//        gameStatsData["game3Score"] = 3
//
//// Добавляем данные статистики игры в данные пользователя
//        userData["gameStats"] = gameStatsData

// Добавляем данные пользователя в базу данных
//        usersRef.child(userId!!).setValue(userData)
//            .addOnSuccessListener {
//                // Данные успешно добавлены в базу данных
//            }
//            .addOnFailureListener { error ->
//                // Ошибка при добавлении данных в базу данных
//            }
    }



    override fun onClick(v: View?) {
        FirebaseAuth.getInstance().signOut();
        val intent = Intent(applicationContext, Login::class.java);
        startActivity(intent);
        finish();
    }


//    private fun startGameNull() {
//        Log.d("Debug", "Вход в игру 'Угадай число'!")
//        val intent = Intent(this, GameNull::class.java)
//        startActivity(intent)
//        finish() // Закрываем меню после запуска игры
//    }

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

    private fun startKNB() {
        Log.d("Debug", "Вход в игру 'КНБ'!")
        val intent = Intent(this, Knb::class.java)
        startActivity(intent)
        finish()
    }
}

fun createDB() {
    val database = FirebaseDatabase.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    Log.d("Firebase", "${userId}")
    val email = database.reference.child("users").child(userId!!).child("email")
    email.setValue(FirebaseAuth.getInstance().currentUser?.email)

//    // Проверяем, что пользователь авторизован
//    if (userId != null) {
//        Log.d("Firebase", "Статистика увеличена (можно считать)-2")
//        // Получаем ссылку на узел "gameStats" для данного пользователя
//        val gameStatsRef = database.reference.child("users").child(userId).child("gameStats")
//
//        // Увеличиваем значение параметра "game1Score" на единицу
//        gameStatsRef.child("game1Score").addListenerForSingleValueEvent(object :
//            ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val currentScore = dataSnapshot.getValue(Int::class.java) ?: 0
//                val newScore = currentScore + 1
//                gameStatsRef.child("game1Score").setValue(newScore)
//                    .addOnSuccessListener {
//                        Log.d("Debug", "Успешно обновлено значение параметра game1Score")
//                    }
//                    .addOnFailureListener { error ->
//                        Log.d("Debug", "Ошибка при обновлении значения параметра game1Score")
//                    }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Обработка ошибок при чтении данных
//            }
//        })
//    }
}



