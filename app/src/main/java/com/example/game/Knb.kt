package com.example.game

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class Knb : ComponentActivity() {
    private var ewons: Int = 0
    private var wons: Int = 0
    val GAME_ID = "2"
    private lateinit var buttons: Array<ImageButton>
    private lateinit var buttonCls: Button
    private lateinit var winnerTextView: TextView
    private var currentPlayer: Int = 1
    private var gameOver: Boolean = false
    private var whoWon: Int = -10

    private lateinit var imageWin: ImageView

    private lateinit var tv: TextView

    private lateinit var clientSocket: Socket
    private lateinit var reader: BufferedReader
    private lateinit var writer: PrintWriter

    private var round: Int = 3 // !!!

    private var playerEmail: String ? = FirebaseAuth.getInstance().currentUser!!.email
    //private var playerEmail: String ? = "login${(0..10).random()}" // !!! временно
    private var player_id: Int = -1

    private lateinit var enemyEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knb)

        buttonCls = findViewById(R.id.buttonCls)
        imageWin = findViewById(R.id.winImage)
        winnerTextView = findViewById(R.id.winner)

        Log.d("Debug", "Вошли в игру камень-ножницы-бумага с почтой ${playerEmail}")

        // Получение ссылок на кнопки
        buttons = arrayOf(
            findViewById(R.id.button1),
            findViewById(R.id.button2),
            findViewById(R.id.button3),
        )


        // Назначение слушателя нажатия кнопок
        for (i in 0..2) {
            buttons[i].setOnClickListener {view -> onButtonClicked(view, i) }
        }

        disableAllButtons()
        // TODO: загрузка сервера - фронтенд @a1sarpi
        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d("Server", "Пытаемся подключиться к серверу")
                val serverAddress = "10.0.2.2" // IP-адрес сервера
                val serverPort = 1234 // Порт сервера

                clientSocket = Socket(serverAddress, serverPort)
                if (clientSocket.isConnected) {
                    Log.d("Server", "Try to connect to the server.")

                    reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                    writer = PrintWriter(clientSocket.getOutputStream())

                    writer.println(GAME_ID) // !!!
                    writer.flush()
                    writer.println(playerEmail)
                    writer.flush()

                    // Получаем идентификатор игрока от сервера
                    player_id = reader.readLine().toInt()
                    enemyEmail = reader.readLine()
                    Log.d("Debug.", "Id ${player_id}, почта: $playerEmail")

                    // Обновляем UI в основном потоке
                    launch(Dispatchers.Main) {
                        // TODO: конец ожидания сервера (фронтенд) @a1sarpi
                        buttonCls.setOnClickListener {
                            exitToMenu();
                        }
                        currentPlayer = 1
                        tv = findViewById(R.id.currentPlayerTV)
                        tv.text = "Противник ${enemyEmail.removeSuffix("@whatever.ru")}"
                        enableAllButtons()
                    }
                } else {
                    // Обработка ошибки подключения
                    Log.d("Server", "Failed to connect to the server.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun enableAllButtons() {
        Log.d("Debug", "Пытаемся включить кнопки")
        for (button in buttons) {
            button.isEnabled = true
        }
    }

    private fun disableAllButtons() {
        Log.d("Debug", "Пытаемся выключить кнопки")
        for (button in buttons) {
            button.isEnabled = false
        }
    }

    @SuppressLint("ResourceType", "UseCompatLoadingForDrawables")
    private fun onButtonClicked(view: View?, id: Int) {
        // TODO: gui @a1sarpi
        var number = id
        disableAllButtons()

        // посылаем серверу число
        GlobalScope.launch(Dispatchers.IO) {
            try {
                writer.println(id.toString())
                writer.flush()
                Log.d("Server", "На сервер отправлено число ${number}")
                var _ans = reader.readLine() // от врага
                var ans = _ans.toInt()

                if (checkWin(id, ans) == 1) {
                    Log.d("Debug", "В данной битве пользователь выиграл, round = ${round - 1}")
                    round--
                    wons++
                    writer.println("win")
                    writer.flush()
                    // TODO: сообщение о победе/ничье (выше) gui @a1sarpi
                    if (round == 0) {
                        gameOver = true
                        if (wons > ewons) {
                            Log.d("Debug", "Мы выиграли!")
                            incrementStats()
                            writer.println("win")
                            whoWon = 1
                            wons = 0; ewons = 0;
                        } else {
                            Log.d("Debug", "Мы проиграли!")
                            writer.println("lose")
                            whoWon = 2
                            wons = 0; ewons = 0;
                        }
                    }
                } else if (checkWin(id, ans) == 0) {
                    Log.d("Debug", "В данной битве ничья, round = $round")
                    writer.println("dontwin")
                    writer.flush()
                } else if (checkWin(id, ans) == 2) {
                    Log.d("Debug", "В данной битве пользователь проиграл, round = ${round - 1}")
                    round--
                    ewons++
                    writer.println("lose")
                    writer.flush()
                    if (round == 0) {
                        gameOver = true
                        if (wons > ewons) {
                            Log.d("Debug", "Мы выиграли!")
                            incrementStats()
                            writer.println("win")
                            whoWon = 1
                            wons = 0; ewons = 0;
                        } else {
                            Log.d("Debug", "Мы проиграли!")
                            writer.println("lose")
                            whoWon = 2
                            wons = 0; ewons = 0;
                        }
                    }
                }
                // UI в основном потоке
                launch(Dispatchers.Main) {
                    val spannable1 = SpannableString("$wons")
                    val spannable2 = SpannableString("$ewons")
                    val coloredText = Html.fromHtml("<font color='#0000FF'>$wons</font>:<font color='#FF0000'>$ewons</font>, Html.FROM_HTML_MODE_LEGACY")
                    winnerTextView.text = "Игра идёт со счётом $wons:$ewons"
                    if (gameOver) {
                        Log.d("Debug", "Игра окончена")
                        lateinit var w: Drawable
                        if (whoWon == 1) {
                            w = resources.getDrawable(R.drawable.win)
                            winnerTextView.setText("${playerEmail?.removeSuffix("@whatever.ru")} выиграл")
                        }
                        else if (whoWon == 0) {
                            w = resources.getDrawable(R.drawable.draw)
                            winnerTextView.setText("Ничья")
                        }
                        else if (whoWon == 2) {
                            Log.d("Debug", "Вошли в функцию проигрыша")
                            w = resources.getDrawable(R.drawable.cross_red)
                            winnerTextView.setText("${enemyEmail?.removeSuffix("@whatever.ru")} выиграл")
                        }
                        imageWin.setImageDrawable(w)
                        disableAllButtons()
                        gameOver = false
                        showDialog()
                    }
                    enableAllButtons()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun incrementStats() {
        // Получаем ссылку на базу данных Firebase
        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid


        // Проверяем, что пользователь авторизован
        if (userId != null) {
            Log.d("Firebase", "Статистика увеличена (можно считать)")
            // Получаем ссылку на узел "gameStats" для данного пользователя
            val gameStatsRef = database.reference.child("users").child(userId).child("gameStats")

            // Увеличиваем значение параметра "game1Score" на единицу
            gameStatsRef.child("game2Score").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val currentScore = dataSnapshot.getValue(Int::class.java) ?: 0
                    val newScore = currentScore + 1
                    gameStatsRef.child("game2Score").setValue(newScore)
                        .addOnSuccessListener {
                            // Успешно обновлено значение параметра "game1Score"
                        }
                        .addOnFailureListener { error ->
                            // Ошибка при обновлении значения параметра "game1Score"
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Обработка ошибок при чтении данных
                }
            })
        }
    }

    private fun checkWin(me: Int, enemy: Int): Int {
        Log.d("Debug", "Вошли в функцию проверки.")
        return ((enemy - me + 3) % 3)
    }

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Начать новую игру?")
            .setCancelable(false)
            .setPositiveButton("Да") { dialog, id ->
                GlobalScope.launch(Dispatchers.IO) {
                    sendData("${player_id}")
                }
                // TODO: показать ещё окно о переподключении @a1sarpi
                resetGame()
            }
            .setNegativeButton("Нет") { dialog, id ->
                GlobalScope.launch(Dispatchers.IO) {
                    sendData("-1")
                }
                dialog.dismiss()
                exitToMenu()
            }

        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun resetGame() {
        winnerTextView.text = "Игра идёт со счётом 0:0"
        imageWin.setImageDrawable(null)

        wons = 0; ewons = 0
        round = 3 // !!!
        disableAllButtons()
        gameOver = false

        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (clientSocket.isConnected) {
                    // Получаем идентификатор игрока от сервера
                    player_id = reader.readLine().toInt()
                    enemyEmail = reader.readLine()
                    Log.d("Debug.", "Id ${player_id}, Почта ${playerEmail}")

                    // Обновляем UI в основном потоке
                    launch(Dispatchers.Main) {
                        // TODO: конец ожидания сервера (фронтенд) @a1sarpi
                        currentPlayer = 1

                        enableAllButtons()
                    }
                } else {
                    // Обработка ошибки подключения
                    Log.d("Server", "Failed to connect to the server.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendData(message: String) {
        try {
            writer.println(message)
            writer.flush()
        } catch (e: IOException) {
            // ошибка отправки данных
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        exitToMenu()
    }

    private fun exitToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        clientSocket.close()
        writer.close()
        reader.close()
        finish() // Закрываем меню после запуска игры
    }

    override fun onDestroy() {
        super.onDestroy()

        clientSocket.close()
        writer.close()
        reader.close()
    }
}
