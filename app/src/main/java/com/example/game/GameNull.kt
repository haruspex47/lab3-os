package com.example.game;

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class GameNull : AppCompatActivity() {
    private lateinit var player: Player
    private lateinit var buttons: Array<Button>
    private var currentPlayer: Int = 1
    private var gameOver: Boolean = false
    var guessedNumber: Int = 0

    private lateinit var tv: TextView

    private lateinit var clientSocket: Socket
    private lateinit var reader: BufferedReader
    private lateinit var writer: PrintWriter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        for (button in buttons) {
            button.setOnClickListener { view -> onButtonClicked(view) }
        }

        disableAllButtons()
        // TODO: загрузка сервера - фронтенд

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val serverAddress = "10.0.2.2" // IP-адрес сервера
                val serverPort = 1234 // Порт сервера

                clientSocket = Socket(serverAddress, serverPort)
                if (clientSocket.isConnected) {
                    Log.d("Server","Try to connect to the server.")

                    reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                    writer = PrintWriter(clientSocket.getOutputStream())

                    writer.println("0") // !!!
                    writer.flush()

                    // Получаем идентификатор игрока от сервера
                    val playerId = reader.readLine().toInt()
                    Log.d("Debug.", "Id ${playerId}")
                    player = Player(playerId)

                    // Получаем загаданное число от сервера
                    guessedNumber = reader.readLine().toInt()
                    Log.d("Debug", "Полученное загаднное число: ${guessedNumber}")

                    // Обновляем UI в основном потоке
                    launch(Dispatchers.Main) {
                        currentPlayer = 1
                        tv = findViewById(R.id.currentPlayerTextView)
                        tv.text = "Сейчас ход игрока номер ${currentPlayer}"

                        if (player.id != 0) {    // if (currentPlayer != player.id + 1) {
                            // Через сервер ждём ответ другого игрока
                            waitForOtherPlayer()
                        } else {
                            for (button in buttons) {
                                button.setBackgroundColor(ContextCompat.getColor(this@GameNull, android.R.color.holo_red_light))
                                button.isEnabled = true
                            }
                        }
                    }
                } else {
                    // Обработка ошибки подключения
                    Log.d("Server","Failed to connect to the server.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // функция для ожидания ответа от другого игрока через сервер
    private fun waitForOtherPlayer() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = reader.readLine().toInt()

                // Обновляем UI в основном потоке
                launch(Dispatchers.Main) {
                    for (button in buttons) {
                        button.setBackgroundColor(ContextCompat.getColor(this@GameNull, android.R.color.holo_red_light))
                        button.isEnabled = true
                    }

                    // Смена текущего игрока
                    currentPlayer = if (currentPlayer == 1) 2 else 1
                    tv.text = "Сейчас ход игрока номер ${currentPlayer}"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun onButtonClicked(view: View) {
        val button = view as Button

        // Проверка, что игра не окончена и кнопка еще не выбрана
        if (!gameOver && button.isEnabled) {
            val number = button.text.toString().toInt()

            // посылаем серверу число
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    writer.println(number.toString())
                    writer.flush()
                    Log.d("Server", "На сервер отправлено число ${number}")
                    // Обновите UI в основном потоке
                    launch(Dispatchers.Main) {
                        // Проверка, угадана ли цифра
                        if (guessedNumber == number) {
                            // Игра окончена
                            button.setBackgroundColor(ContextCompat.getColor(this@GameNull, android.R.color.holo_green_light))
                            Toast.makeText(this@GameNull, "Игра окончена. Игрок ${currentPlayer} победил!", Toast.LENGTH_SHORT).show()
                            disableAllButtons()
                            gameOver = true
                            showDialog()
                        } else {
                            // Увеличение счетчика попыток угадывания
                            player.attemptsCount = player.attemptsCount.plus(1)

                            // Ограничение на 3 попытки
                            if (player.attemptsCount ?: 0 > 3) {
                                // Игра окончена без победителя
                                button.setBackgroundColor(ContextCompat.getColor(this@GameNull, android.R.color.holo_red_light))
                                Toast.makeText(this@GameNull, "Игра окончена. Никто не победил!", Toast.LENGTH_SHORT).show()
                                disableAllButtons()
                                gameOver = true
                                showDialog()
                            } else {
                                // Подсветка кнопки
                                if (guessedNumber ?: 0 > number) {
                                    button.setBackgroundColor(ContextCompat.getColor(this@GameNull, android.R.color.holo_blue_light))
                                } else {
                                    button.setBackgroundColor(ContextCompat.getColor(this@GameNull, android.R.color.holo_red_light))
                                }

                                // Смена текущего игрока
                                currentPlayer = if (currentPlayer == 1) 2 else 1
                                tv.text = "Сейчас ход игрока номер ${currentPlayer}"

                                disableAllButtons()

                                // Через сервер ждём ответ другого игрока
                                waitForOtherPlayer()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun disableAllButtons() {
        for (button in buttons) {
            button.isEnabled = false
        }
    }

    private fun resetGame() {
        for (button in buttons) {
            button.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
            button.isEnabled = true
        }

        player.reset()
        gameOver = false

        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Получаем идентификатор игрока от сервера
                val playerId = reader.readLine().toInt()
                player = Player(playerId)

                // Получаем загаданное число от сервера
                guessedNumber = reader.readLine().toInt()

                // Обновляем UI в основном потоке
                launch(Dispatchers.Main) {
                    currentPlayer = 1
                    tv = findViewById(R.id.currentPlayerTextView)
                    tv.text = "Сейчас ход игрока номер ${currentPlayer}"

                    if (currentPlayer != player.id) {
                        disableAllButtons()
                        // Через сервер ждём ответ другого игрока
                        waitForOtherPlayer()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private inner class Player(val id: Int) {
        var attemptsCount: Int = 0

        fun reset() {
            attemptsCount = 0
        }
    }

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Начать новую игру?")
            .setCancelable(false)
            .setPositiveButton("Да") { dialog, id ->
                // TODO: ждём реакции другого игрока. Игра перезапустится, только если оба будут за
                // ...
                resetGame()
                dialog.dismiss()
            }
            .setNegativeButton("Нет") { dialog, id ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.show()
    }
}
