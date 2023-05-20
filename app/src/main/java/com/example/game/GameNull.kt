package com.example.game;

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
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

import com.google.firebase.auth.FirebaseAuth
import java.io.IOException


/*
Общие TODO: Кнопка выхода в меню из игры @a1sarpi

*/


//Еще один мой кусок
//class User() {
//    private var wins: Int = 0
//    private var loses: Int = 0
//    private var draws: Int = 0
//    constructor(pWins: Int, pLoses: Int, pDraws: Int) : this() {
//        this.wins += pWins
//        this.loses += pLoses
//        this.draws += pDraws
//    }
//}


class GameNull : AppCompatActivity() {
    val GAME_ID = "0"
    private lateinit var player: Player
    private lateinit var buttons: Array<Button>
    private var currentPlayer: Int = 1
    private var gameOver: Boolean = false
    var guessedNumber: Int = 0
    //var button_cls: Button = findViewById(R.id.logout)


    private lateinit var tv: TextView

    private lateinit var clientSocket: Socket
    private lateinit var reader: BufferedReader
    private lateinit var writer: PrintWriter

    //private var playerEmail: String ? = FirebaseAuth.getInstance().currentUser!!.email?.removeSuffix("@whatever.ru")
    private var playerEmail: String ? = "login${(0..10).random()}" // !!! временно

        private lateinit var enemyEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        button_cls.setOnClickListener(View.OnClickListener {
//            exitToMenu()
//        })

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
                    val playerId = reader.readLine().toInt()
                    enemyEmail = reader.readLine()
                    Log.d("Debug.", "Id ${playerId}")
                    player = Player(playerId, playerEmail)

                    // Получаем загаданное число от сервера
                    guessedNumber = reader.readLine().toInt()
                    Log.d("Debug", "Полученное загаднное число: ${guessedNumber}")


                    // Обновляем UI в основном потоке
                    launch(Dispatchers.Main) {
                        // TODO: конец ожидания сервера (фронтенд) @a1sarpi
                        currentPlayer = 1
                        tv = findViewById(R.id.currentPlayerTextView)


                        if (player.id != 0) {    // if (currentPlayer != player.id + 1) {
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

    // функция для ожидания ответа от другого игрока через сервер
    private fun waitForOtherPlayer() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = reader.readLine()
                if (response.toInt() == 0) {
                    Log.d("Error", "Игрок отключился")
                    // TODO: сообщение о сожалении @a1sarpi

                    //Тоже надеюсь что правильно сделал, не было возможности проверить

//                    AlertDialog.Builder(applicationContext)
//                        .setTitle("Игрок отключился")
//                        .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener() { dialogInterface: DialogInterface, i: Int ->
//                            fun onClick(dialog: DialogInterface, which: Int) {
//                                exitToMenu()
//                            }
//                        })

                    //Тоже надеюсь что правильно сделал, не было возможности проверить

                }
                if (response.toInt() == guessedNumber) {
                    // Toast.makeText(this@GameNull, "Игра окончена. Игрок ${currentPlayer} победил!", Toast.LENGTH_SHORT).show()
                    gameOver = true
                    Log.d("Debug", "Игрок выбрал правильное число")
                    launch(Dispatchers.Main) {
                        showDialog()
                    }
                }
                var number = response.toInt()

                // Обновляем UI в основном потоке
                launch(Dispatchers.Main) {
                    enableAllButtons()
                    // Смена текущего игрока
                    currentPlayer = if (currentPlayer == 1) 2 else 1
                    tv.text = "Сейчас ход игрока " +
                            if (currentPlayer == player.id + 1) playerEmail?.removeSuffix("@whatever.ru") else enemyEmail?.removeSuffix("@whatever.ru")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun exitToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        clientSocket.close()
        writer.close()
        reader.close()
        finish() // Закрываем меню после запуска игры
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
                    // UI в основном потоке
                    launch(Dispatchers.Main) {
                        // Проверка, угадана ли цифра
                        if (guessedNumber == number) {
                            // Игра окончена
                            button.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@GameNull,
                                    android.R.color.holo_green_light
                                )
                            )
                            Toast.makeText(
                                this@GameNull,
                                "Игра окончена. Игрок ${currentPlayer} победил!",
                                Toast.LENGTH_SHORT
                            ).show()
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

                                // Здесь я буду пытаться записать во вторую базу данных результаты
//
//                                var ref: DatabaseReference = secondaryDatabase.reference
//                                var userToAddRef: DatabaseReference = ref.child("users").child(playerEmail.removeSuffix("@whatever.ru"))
//                                var new_user: User = User(0, 0, 1);
//                                userToAddRef(new_user)

                                button.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this@GameNull,
                                        android.R.color.holo_red_light
                                    )
                                )
                                Toast.makeText(
                                    this@GameNull,
                                    "Игра окончена. Никто не победил!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                disableAllButtons()
                                gameOver = true
                                showDialog()
                            } else {
                                // Подсветка кнопки
                                if (guessedNumber ?: 0 > number) {
                                    button.setBackgroundColor(
                                        ContextCompat.getColor(
                                            this@GameNull,
                                            android.R.color.holo_blue_light
                                        )
                                    )
                                } else {
                                    button.setBackgroundColor(
                                        ContextCompat.getColor(
                                            this@GameNull,
                                            android.R.color.holo_red_light
                                        )
                                    )
                                }
                                // Смена текущего игрока
                                currentPlayer = if (currentPlayer == 1) 2 else 1
                                tv.text = "Сейчас ход игрока " +
                                        if (currentPlayer == player.id + 1) playerEmail else enemyEmail

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

    private fun enableAllButtons() {
        for (button in buttons) {
            button.setBackgroundColor(
                ContextCompat.getColor(
                    this@GameNull,
                    android.R.color.holo_red_light
                )
            )
            button.isEnabled = true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun resetGame() {
        // TODO: ожидание сервера @a1sarpi
        // Так..?
        //val intent = Intent(this, SplashScreen::class.java)
        //startActivity(intent)

        disableAllButtons()

        player.reset()
        gameOver = false

        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Получаем идентификатор игрока от сервера
                val playerId = reader.readLine().toInt()
                enemyEmail = reader.readLine()
                Log.d("Debug.", "Id ${playerId}")
                player = Player(playerId, playerEmail)

                // Получаем загаданное число от сервера
                guessedNumber = reader.readLine().toInt()
                Log.d("Debug", "Полученное загаднное число: ${guessedNumber}")


                // Обновляем UI в основном потоке
                launch(Dispatchers.Main) {
                    // TODO: конец ожидания сервера (фронтенд) @a1sarpi
                    //finish() // КОНЕЦ ожидания
                    currentPlayer = 1
                    tv = findViewById(R.id.currentPlayerTextView)
                    tv.text = "Сейчас ход игрока " +
                            if (currentPlayer == player.id + 1) playerEmail?.removeSuffix("@whatever.ru") else enemyEmail?.removeSuffix("@whatever.ru")


                    if (player.id != 0) {
                        // Через сервер ждём ответ другого игрока
                        tv.text = "Сейчас ход игрока ${enemyEmail}"
                        waitForOtherPlayer()
                    } else {
                        tv.text = "Сейчас ход игрока ${playerEmail}"
                        enableAllButtons()
                    }
                } // FIXME: жёсткое дублирование кода, но вроде бы этого не избежать
                // Да забей, норм мне кажется
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private inner class Player(val id: Int, val email: String?) {
        var attemptsCount: Int = 0

        fun reset() {
            attemptsCount = 0
        }
    }

    suspend fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Начать новую игру?")
            .setCancelable(false)
            .setPositiveButton("Да") { dialog, id ->
                GlobalScope.launch(Dispatchers.IO) {
                    sendData("${player.id}")
                }
                // TODO: показать ещё окно о переподключении @a1sarpi

                // Еще одна попытка написать что-то вменяемое

//                AlertDialog.Builder(applicationContext)
//                    .setTitle("Пытаемся переподключиться")
//                    .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener() { dialogInterface: DialogInterface, i: Int ->
//                        fun onClick(dialog: DialogInterface, which: Int) {
//                            resetGame()
//                        }
//                    })

                // Еще одна попытка написать что-то вменяемое


                //resetGame()
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

    suspend fun sendData(message: String) {
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

    override fun onDestroy() {
        super.onDestroy()

        clientSocket.close()
        writer.close()
        reader.close()
    }
}
