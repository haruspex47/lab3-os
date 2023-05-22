package com.example.game

import android.os.Bundle
import android.util.Log
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// com.example.game.PlayerStatsActivity.kt

class PlayerStatsActivity : AppCompatActivity() {

    private lateinit var emailTextView: TextView
    private lateinit var playerStatsTextView: TextView
    private lateinit var playerStatsTableLayout: TableLayout
    private lateinit var bestPlayerTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_stats)

        // Находим TextView по их идентификаторам в макете
        emailTextView = findViewById(R.id.emailTextView)
        //playerStatsTextView = findViewById(R.id.playerStatsTextView)
        playerStatsTableLayout = findViewById<TableLayout>(R.id.playerStatsTableLayout)
        bestPlayerTextView = findViewById(R.id.bestPlayerTextView)

        // Получаем данные текущего игрока
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentPlayerId = currentUser?.uid
        val currentEmail = currentUser?.email

        // Отображаем email текущего игрока
        emailTextView.text = currentEmail

//        val usersRef = FirebaseDatabase.getInstance().reference
//        val currentUserRef = usersRef.child(userId!!)
//        currentUserRef.child("gameStats").child("game1score").addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val game1score = dataSnapshot.getValue(Int::class.java)
//                // Доступ к значению game1score здесь
//                // Делайте что-то с полученным значением статистики
//                playerStatsTextView.text = "Ваш результат в игре: $game1score"
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Обработка ошибок при чтении данных из Firebase
//            }
//        })
//
//        val bestPlayerStatsRef = usersRef.child("users").orderByChild("gameStats/game1score").limitToLast(1)

        // Получаем статистику текущего игрока из базы данных
        val database = FirebaseDatabase.getInstance().reference
        val currentPlayerStatsRef = database.child("users").child(currentPlayerId!!).child("gameStats")

        currentPlayerStatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentPlayerStats = dataSnapshot.getValue(GameStats::class.java)

                // Создаем новую строку для статистики текущего игрока
                val currentPlayerRow = TableRow(this@PlayerStatsActivity)

                // Создаем ячейку для отображения игры 1
                val game1Cell = TextView(this@PlayerStatsActivity)
                val game1Score = currentPlayerStats?.game1Score ?: 0
                game1Cell.text = "Игра 1: $game1Score"
                currentPlayerRow.addView(game1Cell)

                // Создаем ячейку для отображения игры 2
                val game2Cell = TextView(this@PlayerStatsActivity)
                val game2Score = currentPlayerStats?.game2Score ?: 0
                game2Cell.text = "Игра 2: $game2Score"
                currentPlayerRow.addView(game2Cell)

                // Создаем ячейку для отображения игры 3
                val game3Cell = TextView(this@PlayerStatsActivity)
                val game3Score = currentPlayerStats?.game3Score ?: 0
                game3Cell.text = "Игра 3: $game3Score"
                currentPlayerRow.addView(game3Cell)

                // Добавляем строку в таблицу
                playerStatsTableLayout.addView(currentPlayerRow)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибок при получении данных из базы данных
            }
        })

        // Получаем данные самого успешного игрока из базы данных
        val bestPlayerStatsRef = database.child("users").orderByChild("gameStats/game1score").limitToLast(1)

        bestPlayerStatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Обработка полученных данных самого успешного игрока
                for (playerSnapshot in dataSnapshot.children) {
                    val bestPlayerId = playerSnapshot.key
                    val bestPlayerEmail = playerSnapshot.child("email").getValue(String::class.java)
                    val bestPlayerStats = playerSnapshot.child("gameStats").getValue(GameStats::class.java)
                    val bestPlayerScore = bestPlayerStats?.game1Score ?: 0

                    // Отображаем данные самого успешного игрока
                    bestPlayerTextView.text = "Лучший результат: $bestPlayerEmail, $bestPlayerScore побед"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибок при получении данных из базы данных
            }
        })
    }
}

data class GameStats(
    val game1Score: Int? = null,
    val game2Score: Int? = null,
    val game3Score: Int? = null
)
