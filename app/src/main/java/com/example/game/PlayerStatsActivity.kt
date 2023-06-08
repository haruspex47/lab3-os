package com.example.game

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.game.SocketHelper.clientSocket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PlayerStatsActivity : AppCompatActivity() {

    private lateinit var emailTextView: TextView
    private lateinit var bestPlayerEmailTextView: TextView
    private lateinit var playerStatsTextView: TextView
    private lateinit var playerStatsTableLayout: TextView
    private lateinit var topPlayerStatsTableLayout: TextView
    private lateinit var bestPlayerTextView: TextView
    private lateinit var buttonCls: Button

    private lateinit var bestPlayerEmail: String
    private var bestPlayerScore: Int = -111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_stats)

        Log.d("Debug", "Вошли в класс статистики")

        buttonCls = findViewById(R.id.buttonCls)
        // Находим RecyclerView по его идентификатору в макете
        //val playerStatsRecyclerView: RecyclerView = findViewById(R.id.playerStatsRecyclerView)

// Устанавливаем менеджер компоновки (layout manager) для RecyclerView
//        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
//        playerStatsRecyclerView.layoutManager = layoutManager

// Создаем список для хранения данных статистики игроков
        val playerStatsList: MutableList<PlayerStats> = mutableListOf()

// Создаем адаптер для RecyclerView и устанавливаем его
//        val adapter: PlayerStatsAdapter = PlayerStatsAdapter(playerStatsList)
//        playerStatsRecyclerView.adapter = adapter

        // Находим TextView по их идентификаторам в макете
        emailTextView = findViewById(R.id.emailTextView)
        //playerStatsTextView = findViewById(R.id.playerStatsTextView)
        playerStatsTableLayout = findViewById(R.id.playerStatsTableLayout)
        topPlayerStatsTableLayout = findViewById(R.id.topPlayerStatsTableLayout)
        bestPlayerTextView = findViewById(R.id.bestPlayerTextView)

        // Получаем данные текущего игрока
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentPlayerId = currentUser?.uid
        val currentEmail = currentUser?.email?.removeSuffix("@whatever.ru")

        // Отображаем email текущего игрока
        emailTextView.text = "Вы: ${currentEmail?.removeSuffix("@whatever.ru")}"


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

        Log.d("Debug", "Получили ссылку на базу данных")

        buttonCls.setOnClickListener {
            exitToMenu();
        }

        currentPlayerStatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentPlayerStats = dataSnapshot.getValue(GameStats::class.java)

                // Создаем новую строку для статистики текущего игрока
                val currentPlayerRow = TableRow(this@PlayerStatsActivity)

                // Создаем ячейку для отображения игры 1
                val game1Cell = TextView(this@PlayerStatsActivity)
                val game1Score = currentPlayerStats?.game1Score ?: 0
                game1Cell.text = "Результат игры крестики-нолики: $game1Score"
                currentPlayerRow.addView(game1Cell)

                // Создаем ячейку для отображения игры 2
                val game2Cell = TextView(this@PlayerStatsActivity)
                val game2Score = currentPlayerStats?.game2Score ?: 0
                game2Cell.text = "Результат игры КНБ: $game2Score"
                currentPlayerRow.addView(game2Cell)

                // Создаем ячейку для отображения игры 3
                val game3Cell = TextView(this@PlayerStatsActivity)
                val game3Score = currentPlayerStats?.game3Score ?: 0
                game3Cell.text = "Результат игры ВИКТОРИНА: $game3Score"
                currentPlayerRow.addView(game3Cell)

                // Добавляем строку в таблицу
                playerStatsTableLayout.text = game1Cell.text as String + "\n" + game2Cell.text + "\n" + game3Cell.text
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибок при получении данных из базы данных
            }
        })

//        val allPlayerStatsRef = database.child("users")
//
//        allPlayerStatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (playerSnapshot in dataSnapshot.children) {
//                    val playerId = playerSnapshot.key
//                    val playerEmail = playerSnapshot.child("email").getValue(String::class.java)
//                    val playerStats = playerSnapshot.child("gameStats").getValue(GameStats::class.java)
//
//                    if (playerId != null && playerEmail != null && playerStats != null) {
//                        val playerStatsData = PlayerStats(playerEmail, playerStats.game1Score, playerStats.game2Score, playerStats.game3Score)
//                        playerStatsList.add(playerStatsData)
//                    }
//                }
//
//                // Уведомляем адаптер об изменении данных
//                adapter.notifyDataSetChanged()
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Обработка ошибок при получении данных из базы данных
//            }
//        })
//
//
        // Получаем данные самого успешного игрока из базы данных
        val bestPlayerStatsRef = database.child("users").orderByChild("gameStats/game1Score").limitToLast(1)

        bestPlayerStatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var bestbest = dataSnapshot.children.first()
                // Обработка полученных данных самого успешного игрока
                for (playerSnapshot in dataSnapshot.children) {
                    val email = playerSnapshot.child("email").getValue(String::class.java)
                    val bestPlayerStats = playerSnapshot.child("gameStats").getValue(GameStats::class.java)
                    val game1Score = bestPlayerStats?.game1Score ?: 0
                    val game2Score = bestPlayerStats?.game2Score ?: 0
                    val game3Score = bestPlayerStats?.game3Score ?: 0
                    val gameScore = game1Score + game2Score + game3Score
                    Log.d("Debug", game1Score.toString())

                    // Проверяем, является ли текущий игрок лучшим
                    if (gameScore > bestPlayerScore) {
                        bestPlayerEmail = email ?: ""
                        bestPlayerScore = gameScore
                        Log.d("Debug", "$bestPlayerEmail")
                        bestbest = playerSnapshot
                    }
                }
                //Log.d("Debug", bestPlayerEmail + " " + bestPlayerScore.toInt())
                topPlayerStatsTableLayout.text = "$bestPlayerEmail ${bestPlayerScore.toString()}"

                // Отображаем данные самого успешного игрока
                //bestPlayerTextView.text = "Лучший результат: $bestPlayerEmail, $bestPlayerScore побед"


//                // Обработка полученных данных самого успешного игрока
               // for (playerSnapshot in dataSnapshot.children) {
                    val bestPlayerEmail = bestbest.child("email").getValue(String::class.java)
                    Log.d("Firebase", "best email: $bestPlayerEmail")
                    val bestPlayerStats =
                        bestbest.child("gameStats").getValue(GameStats::class.java)
                    val bestPlayerGame1Score = bestPlayerStats?.game1Score ?: 0
                    val bestPlayerGame2Score = bestPlayerStats?.game2Score ?: 0
                    val bestPlayerGame3Score = bestPlayerStats?.game3Score ?: 0

                    // Создаем новую строку для лучшего игрока
                    val bestPlayerRow = TableRow(this@PlayerStatsActivity)

                    // Создаем ячейку для отображения лучшего результата игры 1
                    val bestGame1Cell = TextView(this@PlayerStatsActivity)
                    bestGame1Cell.text =
                        "Результат игры крестики-нолики: $bestPlayerGame1Score"
                    bestPlayerRow.addView(bestGame1Cell)

                    // Создаем ячейку для отображения лучшего результата игры 2
                    val bestGame2Cell = TextView(this@PlayerStatsActivity)
                    bestGame2Cell.text =
                        "Результат игры КНБ: $bestPlayerGame2Score"
                    bestPlayerRow.addView(bestGame2Cell)

                    // Создаем ячейку для отображения лучшего результата игры 3
                    val bestGame3Cell = TextView(this@PlayerStatsActivity)
                    bestGame3Cell.text =
                        "Результат игры ВИКТОРИНА: $bestPlayerGame3Score"
                    bestPlayerRow.addView(bestGame3Cell)

                    // Добавляем строку в таблицу
                    topPlayerStatsTableLayout.text = bestGame1Cell.text as String + "\n" + bestGame2Cell.text + "\n" + bestGame3Cell.text
                //}
                bestPlayerTextView.text = "Лучший игрок: ${bestPlayerEmail?.removeSuffix("@whatever.ru")}"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибок при получении данных из базы данных
            }
        })
    }

    private fun exitToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish() // Закрываем меню после запуска игры
    }

}

data class GameStats(
    val game1Score: Int? = null,
    val game2Score: Int? = null,
    val game3Score: Int? = null
)



//class PlayerStatsAdapter(private val playerStatsList: List<PlayerStats>) :
//    RecyclerView.Adapter<PlayerStatsAdapter.ViewHolder>() {
//
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        // Определение TextView для отображения идентификатора игрока
//        val playerIdTextView: TextView = itemView.findViewById(R.id.playerIdTextView)
//        // Определение TextView для отображения статистики игроков
//        val game1TextView: TextView = itemView.findViewById(R.id.game1TextView)
//        val game2TextView: TextView = itemView.findViewById(R.id.game2TextView)
//        val game3TextView: TextView = itemView.findViewById(R.id.game3TextView)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_player_stats, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val playerStats = playerStatsList[position]
//
//        // Отображаем статистику игрока в соответствующих TextView
//        holder.playerIdTextView.text = "Игрок ${playerStats.playerId}"
//        holder.game1TextView.text = "Игра 1: ${playerStats.game1score}"
//        holder.game2TextView.text = "Игра 2: ${playerStats.game2score}"
//        holder.game3TextView.text = "Игра 3: ${playerStats.game3score}"
//    }
//
//
//    override fun getItemCount(): Int {
//        return playerStatsList.size
//    }
//}



data class PlayerStats(
    val playerId: String,
    val game1score: Int?,
    val game2score: Int?,
    val game3score: Int?
)