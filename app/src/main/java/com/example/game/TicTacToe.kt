package com.example.game

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class TicTacToe : ComponentActivity() {
    private var enButtons: MutableList<Button> = mutableListOf()
    private var myButtonsId: MutableList<Pair<Int, Int>> = mutableListOf()
    val GAME_ID = "1"
    private lateinit var buttons: MutableList<Button>
    private var currentPlayer: Int = 1
    private var gameOver: Boolean = false

    private lateinit var tv: TextView

    private lateinit var clientSocket: Socket
    private lateinit var reader: BufferedReader
    private lateinit var writer: PrintWriter

    //private var playerEmail: String ? = FirebaseAuth.getInstance().currentUser!!.email?.removeSuffix("@whatever.ru")
    private var playerEmail: String ? = "login${(0..10).random()}" // !!! временно
    private var player_id: Int = -1

    private lateinit var enemyEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ttt)

        // Получение ссылок на кнопки
        buttons = mutableListOf(
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

        enButtons = buttons

        disableAllButtons()
        // TODO: загрузка сервера - фронтенд @a1sarpi
        GlobalScope.launch(Dispatchers.IO) {
            try {
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
                        currentPlayer = 1
                        tv = findViewById(R.id.currentPlayerTextView)


                        if (player_id != 0) {    // if (currentPlayer != player.id + 1) {
                            // Через сервер ждём ответ другого игрока
                            tv.text =
                                "Сейчас ход игрока ${enemyEmail?.removeSuffix("@whatever.ru")}"
                            waitForOtherPlayer()
                        } else {
                            tv.text = "Сейчас ход игрока ${playerEmail}"
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

    private fun waitForOtherPlayer() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = reader.readLine()
                if (response.toInt() == -111) {
                    Log.d("Error", "Игрок отключился")
                    exitToMenu()
                    // TODO: сообщение о сожалении @a1sarpi
                }
                var number = Pair<Int, Int>(response.toInt(), reader.readLine().toInt())
                buttons[number.first*3 + number.second].text = currentPlayer.toString()
                if (reader.readLine() == "win") {
                    gameOver = true
                    Log.d("Debug", "Враг выиграл")
                    launch(Dispatchers.Main) {
                        showDialog()
                    }
                }


                // UI в основном потоке
                launch(Dispatchers.Main) {
                    enableAllButtons()
                    // Смена текущего игрока
                    currentPlayer = if (currentPlayer == 1) 2 else 1
                    tv.text = "Сейчас ход игрока " +
                            if (currentPlayer == player_id + 1)
                                playerEmail?.removeSuffix("@whatever.ru") else enemyEmail?.removeSuffix("@whatever.ru")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun enableAllButtons() {
        for (button in enButtons) {
            button.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    android.R.color.holo_red_light
                )
            )
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
        val button = view as Button
        enButtons.remove(button)

        // number
        Log.d("Debug", "id: $id, id/3 = ${id / 3}")
        var number = Pair<Int, Int>(id / 3, id - id / 3)
        myButtonsId.add(number)
        buttons[number.first*3 + number.second].text = player_id.toString()

        // посылаем серверу число
        GlobalScope.launch(Dispatchers.IO) {
            try {
                writer.println((id / 3).toString())
                writer.flush()
                writer.println((id - id / 3).toString())
                writer.flush()
                Log.d("Server", "На сервер отправлено число ${number}")
                // UI в основном потоке
                launch(Dispatchers.Main) {
                    // Проверка, угадана ли цифра
                    if (checkWin(Pair<Int, Int>(id / 3, id - id / 3))) {
                        writer.println("win")
                        writer.flush()
                        // TODO: сообщение о победе/ничье (выше) gui @a1sarpi
                        disableAllButtons()
                        gameOver = true
                        showDialog()
                    } else {
                        if (enButtons.size == 0) {
                            writer.println("nobody")
                            writer.flush()
                            disableAllButtons()
                            gameOver = true
                            showDialog()
                        } else {
                            writer.println("dontwin")
                            writer.flush()
                            disableAllButtons()
                            waitForOtherPlayer()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkWin(id: Pair<Int, Int>): Boolean {
        var flag = true
        if (id.first == id.second) {
            for (i in 0..2) {
                if (!myButtonsId.contains(Pair<Int, Int>(i, i))) {
                    flag = false
                    break
                }
            }
        }
        if (flag)
            return true

        flag = true
        if (id.first == 2 - id.second) {
            for (i in 0..2) {
                if (!myButtonsId.contains(Pair<Int, Int>(i, 2 - i))) {
                    flag = false
                    break
                }
            }
        }
        if (flag)
            return true

        flag = true
        for (i in 0..2) {
            if (!myButtonsId.contains(Pair<Int, Int>(i, id.second))) {
                flag = false
                break
            }
        }
        if (flag)
            return true
        flag = true
        for (i in 0..2) {
            if (!myButtonsId.contains(Pair<Int, Int>(id.first, i))) {
                flag = false
                break
            }
        }
        if (flag)
            return true

        return false
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
        enButtons = buttons
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
                            tv.text = "Сейчас ход игрока ${playerEmail}"
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
