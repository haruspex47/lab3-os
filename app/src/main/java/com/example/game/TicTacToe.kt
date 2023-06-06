package com.example.game

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
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

class TicTacToe : ComponentActivity() {
    private var enButtons: MutableList<ImageButton> = mutableListOf()
    private var myButtonsId: MutableList<Pair<Int, Int>> = mutableListOf()
    val GAME_ID = "1"
    private lateinit var buttons: Array<ImageButton>
    private var currentPlayer: Int = 1
    private var gameOver: Boolean = false

    private lateinit var tv: TextView

    private lateinit var buttonCls: Button

    private lateinit var clientSocket: Socket
    private lateinit var reader: BufferedReader
    private lateinit var writer: PrintWriter

    private var playerEmail: String ? = FirebaseAuth.getInstance().currentUser!!.email
    //private var playerEmail: String ? = "login${(0..10).random()}" // !!! временно
    private var player_id: Int = -1

    private lateinit var enemyEmail: String

    private var remainingTime = 10 // оставшееся время в секундах
    private lateinit var timerTextView: TextView

    private val timer = object : CountDownTimer(10000, 1000) { // 30 секунд, с интервалом 1 секунда
        override fun onTick(millisUntilFinished: Long) {
            // Обновление отображения оставшегося времени
            remainingTime = (millisUntilFinished / 1000).toInt()
            updateTimerDisplay()
        }

        override fun onFinish() {
            // Время истекло, игрок проигрывает
            handleTimeout()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ttt)

        buttonCls = findViewById(R.id.buttonCls)

        timerTextView = findViewById(R.id.timerTextView)

        Log.d("Debug", "Вошли в игру крестики-нолики с почтой ${playerEmail}")

        // Получение ссылок на кнопки
        buttons = arrayOf(
            findViewById(R.id.button1),
            findViewById(R.id.button2),
            findViewById(R.id.button3),
            findViewById(R.id.button4),
            findViewById(R.id.button5),
            findViewById(R.id.button6),
            findViewById(R.id.button7),
            findViewById(R.id.button8),
            findViewById(R.id.button9)
        )

        // Назначение слушателя нажатия кнопок
        for (i in 0..8) {
            buttons[i].setOnClickListener {view -> onButtonClicked(view, i) }
        }

        buttonCls.setOnClickListener {
            exitToMenu();
        }

        enButtons.clear()
        for (bt in buttons) {
            enButtons.add(bt)
        }

        disableAllButtons()
        // TODO: загрузка сервера - фронтенд @a1sarpi
        GlobalScope.launch(Dispatchers.IO) {
            try {
                //val serverAddress = "10.0.2.2" // IP-адрес сервера
                val serverAddress = "plasmaa0.fvds.ru"// IP-адрес сервера
                val serverPort = 4747 // Порт сервера

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
                        currentPlayer = 1
                        tv = findViewById(R.id.currentPlayerTextView)

                        if (player_id != 0) {    // if (currentPlayer != player.id + 1) {
                            // Через сервер ждём ответ другого игрока
                            tv.text =
                                "Сейчас ход игрока ${enemyEmail?.removeSuffix("@whatever.ru")}"
                            waitForOtherPlayer()
                        } else {
                            startTurn()
                            tv.text = "Сейчас ход игрока ${playerEmail?.removeSuffix("@whatever.ru")}"
                            enableAllButtons()
                        }
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

    @SuppressLint("SetTextI18n")
    private fun waitForOtherPlayer() {
        var tm = false
        startTurn()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = reader.readLine()
                if (response.toInt() == -111) {
                    Log.d("Error", "Игрок отключился")
                    tm = true
                    exitToMenu()
                    // TODO: сообщение о сожалении @a1sarpi
                }
                var number = Pair<Int, Int>(response.toInt(), reader.readLine().toInt())
                Log.d("Debug", "Были получены данные $number")
                enButtons.remove(buttons[number.first*3 + number.second])

                var correct = reader.readLine()
                Log.d("Debug", "Были получены данные $correct")
                if (correct == "win") {
                    gameOver = true
                }
                if (correct == "nobody") {
                    gameOver = true
                }

                // UI в основном потоке
                launch(Dispatchers.Main) {
                    if (tm) {
                        tm = false
                        timer.cancel()
                    }
                    //buttons[number.first*3 + number.second].text = if (player_id == 1) "0" else "1"
                    val w: Drawable = if (player_id == 1) {
                        resources.getDrawable(R.drawable.circle_red)

                    } else {
                        resources.getDrawable(R.drawable.cross_blue)
                    }
                    //buttons[number.first*3 + number.second].background = w
                    //buttons[number.first*3 + number.second].setImageResource(R.drawable.cross_blue)
                    val iv: ImageView = findViewById(R.id.imageView)
                    iv.setImageResource(R.drawable.cross_blue)
                    enableAllButtons()
                    // Смена текущего игрока
                    currentPlayer = if (currentPlayer == 1) 2 else 1
                    endTurn()
                    startTurn()
                    tv.text = "Сейчас ход игрока " +
                            if (currentPlayer == player_id + 1)
                                playerEmail?.removeSuffix("@whatever.ru") else enemyEmail?.removeSuffix("@whatever.ru")
                    if (gameOver) {
                        Log.d("Debug", "Враг выиграл")
                        showDialog()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun enableAllButtons() {
        for (button in enButtons) {
//            button.setBackgroundColor(
//                ContextCompat.getColor(
//                    this,
//                    android.R.color.holo_red_light
//                )
//            )
            button.isEnabled = true
        }
    }

    private fun disableAllButtons() {
        for (button in buttons) {
                button.isEnabled = false
        }
    }

    private fun onButtonClicked(view: View?, id: Int) {
        // TODO: gui @a1sarpi
        val button = view as ImageButton
        enButtons.remove(button)

        // number
        Log.d("Debug", "id: $id, id/3 = ${id / 3}")
        var number = Pair<Int, Int>(id / 3, id % 3)
        myButtonsId.add(number)



        // посылаем серверу число
        GlobalScope.launch(Dispatchers.IO) {
            try {
                writer.println((id / 3).toString())
                writer.flush()
                writer.println((id % 3).toString())
                writer.flush()
                Log.d("Server", "На сервер отправлено число ${number}")

                if (checkWin(Pair<Int, Int>(id / 3, id % 3))) {
                    writer.println("win")
                    writer.flush()
                    // TODO: сообщение о победе/ничье (выше) gui @a1sarpi
                    //disableAllButtons()
                    gameOver = true
                    //showDialog()
                } else {
                    if (enButtons.size == 0) {
                        writer.println("nobody")
                        writer.flush()
                        //disableAllButtons()
                        gameOver = true
                        //showDialog()
                    } else {
                        writer.println("dontwin")
                        writer.flush()
                        //disableAllButtons()
                        waitForOtherPlayer()
                    }
                }
                // UI в основном потоке
                launch(Dispatchers.Main) {
                    Log.d("Debug", "number.first * 3 + number.second = ${number.first * 3 + number.second}")
                    Log.d("Debug", "butt: ${buttons[number.first*3 + number.second]}")
//                    buttons[number.first * 3 + number.second].text = player_id.toString()
                    val w: Drawable = if (player_id == 1) {
                        resources.getDrawable(R.drawable.cross_blue)

                    } else {
                        resources.getDrawable(R.drawable.circle_red)
                    }
                    buttons[number.first*3 + number.second].background = w
                    endTurn()
                    startTurn()
                    disableAllButtons()
                    if (gameOver) {
                        incrementStats()
                        gameOver = false
                        showDialog()
                    }
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
        Log.d("Firebase", "Статистика увеличена (можно считать)-1")


        // Проверяем, что пользователь авторизован
        if (userId != null) {
            Log.d("Firebase", "Статистика увеличена (можно считать)-2")
            // Получаем ссылку на узел "gameStats" для данного пользователя
            val gameStatsRef = database.reference.child("users").child(userId).child("gameStats")

            // Увеличиваем значение параметра "game1Score" на единицу
            gameStatsRef.child("game1Score").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val currentScore = dataSnapshot.getValue(Int::class.java) ?: 0
                    val newScore = currentScore + 1
                    gameStatsRef.child("game1Score").setValue(newScore)
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

    private fun checkWin(id: Pair<Int, Int>): Boolean {
        Log.d("Debug", "Вошли в функцию проверки. myButtonsId: $myButtonsId, id: $id")
        var flag = true
        if (id.first == id.second) {
            for (i in 0..2) {
                if (!myButtonsId.contains(Pair<Int, Int>(i, i))) {
                    flag = false
                    break
                } else {
                    Log.d("Debug", "Элемент с id ${Pair<Int, Int>(i, i)} присутствует")
                }
            }
        } else flag = false
        if (flag) {
            Log.d("Debug", "Проверка пройдена по первой диагонали")
            return true
        }

        flag = true
        if (id.first == 2 - id.second) {
            for (i in 0..2) {
                if (!myButtonsId.contains(Pair<Int, Int>(i, 2 - i))) {
                    flag = false
                    break
                } else {
                    Log.d("Debug", "Элемент с id ${Pair<Int, Int>(i, 2-i)} присутствует")
                }
            }
        } else flag = false
        if (flag) {
            Log.d("Debug", "Проверка пройдена по второй диагонали")
            return true
        }

        flag = true
        for (i in 0..2) {
            if (!myButtonsId.contains(Pair<Int, Int>(i, id.second))) {
                flag = false
                break
            } else {
                Log.d("Debug", "Элемент с id ${Pair<Int, Int>(i, id.second)} присутствует")
            }
        }
        if (flag) {
            Log.d("Debug", "Проверка пройдена по вертикали")
            return true
        }

        flag = true
        for (i in 0..2) {
            if (!myButtonsId.contains(Pair<Int, Int>(id.first, i))) {
                flag = false
                break
            } else {
                Log.d("Debug", "Элемент с id ${Pair<Int, Int>(id.first, i)} присутствует")
            }
        }
        if (flag) {
            Log.d("Debug", "Проверка пройдена по горизонтали")
            return true
        }
        return false
    }

    fun showDialog() {
        timer.cancel()
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

    @SuppressLint("SetTextI18n")
    private fun resetGame() {
        for (bt in buttons)
            bt.setBackgroundResource(R.drawable.button_background)
        myButtonsId.clear()
        enButtons.clear()
        for (bt in buttons) {
            enButtons.add(bt)
        }
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

                        if (player_id != 0) {
                            // Через сервер ждём ответ другого игрока
                            tv.text = "Сейчас ход игрока ${enemyEmail?.removeSuffix("@whatever.ru")}"
                            waitForOtherPlayer()
                        } else {
                            tv.text = "Сейчас ход игрока ${playerEmail?.removeSuffix("@whatever.ru")}"
                            enableAllButtons()
                        }
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
        if (clientSocket.isConnected) {
            clientSocket.close()
            writer.close()
            reader.close()
        }
        timer.cancel()
        finish() // Закрываем меню после запуска игры
    }

    override fun onDestroy() {
        super.onDestroy()

        clientSocket.close()
        writer.close()
        reader.close()
    }


    private fun updateTimerDisplay() {
        // Обновление отображения времени в вашем пользовательском интерфейсе
        timerTextView.text = remainingTime.toString()
    }

    fun startTurn() {
        Log.d("Debug", "Пошёл таймер")
        timer.start()
    }

    fun endTurn() {
        timer.cancel()
    }


    private fun handleTimeout() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (clientSocket.isConnected) {
                    if (currentPlayer - 1 != player_id) {
                        writer.println("111")
                        writer.flush()
                    }

                    // Обновляем UI в основном потоке
                    launch(Dispatchers.Main) {
                        showDialog()
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
}
