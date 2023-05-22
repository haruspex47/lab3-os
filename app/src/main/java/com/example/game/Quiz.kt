package com.example.game

import android.R.id
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

// TODO: ничья по окончании вопросов + gui


const val MAX_ROW: Int = 5
const val MAX_COLUMN: Int = 2
var enableQue: MutableList<Question> = mutableListOf()
var flag: Boolean = true

class Quiz : AppCompatActivity() {
    private val GAME_ID = "3"
    private var buttons: MutableList<CustomButton> = mutableListOf()
    private var enButtons: MutableList<CustomButton> = mutableListOf()

    private var currentPlayer: Int = 1
    private var player_id: Int = 0
    private lateinit var enemyEmail: String
    private var gameOver: Boolean = false

    private lateinit var tv: TextView

    private lateinit var clientSocket: Socket
    private lateinit var reader: BufferedReader
    private lateinit var writer: PrintWriter

    //private var playerEmail: String ? = FirebaseAuth.getInstance().currentUser!!.email?.removeSuffix("@whatever.ru")
    private var playerEmail: String ? = "login${(0..10).random()}" // !!! временно
    private var currentBattleId: Pair<Int, Int> = Pair<Int, Int>(0,0)
    private lateinit var myCastle: CustomButton
    private lateinit var enemyCastle: CustomButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Debug", "Вошли в класс Quiz!")
        setContentView(R.layout.activity_quiz)

        var castle1But = findViewById<Button>(R.id.castle1)
        var _castle1But = CustomButton(castle1But, Pair<Int, Int>(0, 1))
        var castle2But = findViewById<Button>(R.id.castle2)
        var _castle2But = CustomButton(castle2But, Pair<Int, Int>(MAX_ROW, 1)) // TODO: у всех замок снизу, и сервер инверсирует номера?
        buttons.add(_castle1But);

        // Настройка обработчиков нажатия для кнопок
        castle1But.setOnClickListener {
            onButtonClick(Pair<Int, Int>(0, 1))
        }

        castle2But.setOnClickListener {
            onButtonClick(Pair<Int, Int>(MAX_ROW, 1))
        }

        // Настройка обработчиков нажатия для кнопок в сетке
        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)
        for (row in 0 until gridLayout.rowCount) {
            for (column in 0 until gridLayout.columnCount) {
                val button =
                    gridLayout.getChildAt(row * gridLayout.columnCount + column) as Button
                button.setOnClickListener {
                    onButtonClick(Pair<Int, Int>(row + 1, column))
                }
                var _button = CustomButton(button, Pair<Int, Int>(row + 1, column))
                buttons.add(_button)
            }
        }
        buttons.add(_castle2But) // добавляем именно здесь

        for (butt in buttons)
            butt.bt.isEnabled = false
//        enableButtons(myCastle.getNeighbors())
//        disableAllButtons()
        //TODO: gui @a1sarpi


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
                    if (player_id == 1) {
                        myCastle = buttons.last()
                        enemyCastle = buttons[0]
                    } else {
                        myCastle = buttons[0]
                        enemyCastle = buttons.last()
                    }
                    myCastle.status = 1
                    enButtons.clear()
                    //enButtons.add(myCastle)

                    enemyEmail = reader.readLine()
                    Log.d("Debug.", "Id $player_id, email $playerEmail")
                    //player = Player(playerId, playerEmail)

                    // Обновляем UI в основном потоке
                    launch(Dispatchers.Main) {
                        // TODO: конец ожидания сервера (фронтенд) @a1sarpi
                        currentPlayer = 1
                        tv = findViewById(R.id.currentPlayerTV)


                        if (player_id != 0) {
                            // Через сервер ждём ответ другого игрока
                            tv.text = "Сейчас ход игрока ${enemyEmail?.removeSuffix("@whatever.ru")}"
                            for (bt in myCastle.getNeighbors()) {
                                if (getButton(bt).status != 1)
                                    enButtons.add(getButton(bt))
                            }
                            waitForOtherPlayer()
                        } else {
                            tv.text = "Сейчас ход игрока ${playerEmail}"
                            enableButtons(myCastle.getNeighbors())
                            //enableAllButtons()
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


        enableQue.add(
            Question(
                "Какая формула определяет комплексную экспоненту?",
                arrayOf("e^(i*x)", "sin(x)", "cos(x)", "ln(x)"),
                0
            )
        )
        enableQue.add(
            Question(
                "Какая функция является гармонической?",
                arrayOf("f(z) = u(x, y) + i*v(x, y)", "f(z) = u(x, y) - i*v(x, y)", "f(z) = u(x, y) * v(x, y)", "f(z) = u(x, y) / v(x, y)"),
                0
            )
        )
        enableQue.add(
            Question(
                "Какой оператор используется для определения интеграла по контуру?",
                arrayOf("∮", "∫", "∂", "∇"),
                0
            )
        )
        enableQue.add(
            Question(
                "Что такое принцип максимума модуля?",
                arrayOf("Если f(z) голоморфна в ограниченной и замкнутой области, то |f(z)| достигает своего максимума на границе области.", "Если f(z) голоморфна во всей комплексной плоскости, то |f(z)| неограничена.", "Если f(z) является гармонической функцией, то |f(z)| неотрицательна.", "Если f(z) аналитична в точке z_0, то |f(z)| имеет непрерывную производную в этой точке."),
                0
            )
        )
        enableQue.add(
            Question(
                "Что такое сопряженное пространство в функциональном анализе?",
                arrayOf("Для банахова пространства X, сопряженным пространством X* является пространство линейных непрерывных функционалов на X.", "Сопряженным пространством является пространство функций, у которых все производные равны нулю.", "Сопряженным пространством является пространство функций, у которых интегралы равны нулю.", "Сопряженным пространством является пространство функций, у которых все точки являются особенностями."),
                0
            )
        )
        enableQue.add(
            Question(
                "Что такое компактный оператор?",
                arrayOf("Оператор, переводящий ограниченное множество в предкомпактное.", "Оператор, переводящий компактное множество в ограниченное.", "Оператор, переводящий предкомпактное множество в компактное.", "Оператор, переводящий ограниченное множество в компактное."),
                0 // 2
            )
        )
        enableQue.add(
            Question(
                "Какая теорема устанавливает связь между дифференцируемостью и голоморфностью функции?",
                arrayOf("Теорема Коши-Римана", "Теорема Римана", "Теорема Коши", "Теорема Дарбу"),
                0
            )
        )
        enableQue.add(
            Question(
                "Какое условие должно выполняться для функции, чтобы она была аналитической в точке?",
                arrayOf("Функция должна быть дифференцируемой в окрестности данной точки.", "Функция должна быть непрерывной в окрестности данной точки.", "Функция должна быть голоморфной в окрестности данной точки.", "Функция должна быть гармонической в окрестности данной точки."),
                0
            )
        )
        enableQue.add(
            Question(
                "Что такое функционал в функциональном анализе?",
                arrayOf("Линейный оператор, переводящий векторное пространство в скаляры.", "Линейный оператор, переводящий скаляры в векторное пространство.", "Функция, отображающая векторное пространство в скаляры.", "Функция, отображающая скаляры в векторное пространство."),
                0 // 2
            )
        )
        enableQue.add(
            Question(
                "Что такое базис в функциональном анализе?",
                arrayOf("Семейство элементов, линейная комбинация которых может представлять любой элемент векторного пространства.", "Семейство элементов, для которых выполняется равенство f(x) = 0.", "Семейство элементов, для которых выполняется неравенство f(x) > 0.", "Семейство элементов, линейная комбинация которых равна нулю."),
                0
            )
        )

        enableQue.shuffle()
    }

    private fun waitForOtherPlayer() {
        Log.d("Debug", "Игрок ожидает ответа другого игрока...")
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // дуэль?
                val response = reader.readLine()
                Log.d("Debug", "Игрок ожидает ответа другого игрока и получил данные $response")
                if (response == "-111") {
                    Log.d("Error", "Игрок отключился")
                    // TODO: сообщение о сожалении @a1sarpi
                    exitToMenu()
                }
                val xy = mutableListOf<Int>(response.toInt(), reader.readLine().toInt())
                val correct = reader.readLine() // duel, true, false
                Log.d("Debug", "Игрок получил данные $correct")
                currentBattleId = Pair<Int, Int>(xy[0].toInt(), xy[1].toInt())
                Log.d("Debug", "Id битвы сменился на $currentBattleId")
                if (getButton(currentBattleId).status == 1) {
                    writer.println("duel")
                    writer.flush()
                    toDuelQuestion()
                }
                if ((correct == "true") and (getButton(currentBattleId) == myCastle)) { // TODO: !!! поменять
                    // Toast.makeText(this@GameNull, "Игра окончена. Игрок ${currentPlayer} победил!", Toast.LENGTH_SHORT).show()
                    Log.d("Debug", "Вражеский игрок победил!")
                    gameOver = true
                    launch(Dispatchers.Main) {
                        showDialog()
                    }
                }

                // Обновляем UI в основном потоке
                launch(Dispatchers.Main) {
                    Log.d("Debug", "Вражеский игрок завершил битву ...")
                    if (correct == "true") {
                        Log.d("Debug", "... и выиграл её")
                        getButton(currentBattleId).status = -1
                        // TODO: gui @a1sarpi
                    }
                    enableAllButtons()
                    // Смена текущего игрока
                    currentPlayer = if (currentPlayer == 1) 2 else 1
                    Log.d("Debug", "currentPlayer = $currentPlayer, playerEmail = $playerEmail, enemyEmail = $enemyEmail, playerId = $player_id")
                    tv.text = "Сейчас ход игрока " +
                            if (currentPlayer == player_id + 1)
                                playerEmail?.removeSuffix("@whatever.ru")
                            else
                                enemyEmail?.removeSuffix("@whatever.ru")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

//    suspend private fun onDuel(id: Pair<Int, Int>) {
//        toDuelQuestion()
//        if (ret == 1) {
//            onWin(id)
//        } else if (ret == -1){
//            onLose(id)
//        }
//    }

    private fun toDuelQuestion() {
        Log.d("Debug", "Кажется, время для дуэли")
        SocketHelper.clientSocket = clientSocket
//        val someActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val data: Intent? = result.data
//                ret = data?.getIntExtra("ret", -1)
//            }
//        }

        val intent = Intent(this, QuizQuestion::class.java)
        intent.putExtra("flag", true) // !!! нужно ли?
        Log.d("Debug", "Пытаемся войти в класс QuizQuestion")
        startActivityForResult(intent, 1)

//
//        val intent = Intent(this, QuizQuestion::class.java)
//        intent.putExtra("flag", true)
//        someActivityResultLauncher.launch(intent)

//        // TODO: новая активность
//        val requestCode = 1 // Вы можете использовать любое число
//        val i = Intent(this, QuizQuestion::class.java)

        //startActivityForResult(i, requestCode)

//        setContentView(R.layout.activity_quiz_question)
//        var ret: Int = 0
//
//        var Qtv = findViewById<TextView>(R.id.QuestionTV) as TextView
//
//        var ansButtons: Array<Button> = arrayOf()
//        ansButtons[0] = findViewById<Button>(R.id.ans1) as Button
//        ansButtons[1] = findViewById<Button>(R.id.ans2) as Button
//        ansButtons[2] = findViewById<Button>(R.id.ans3) as Button
//        ansButtons[3] = findViewById<Button>(R.id.ans4) as Button
//
//        var num = (0..enableQue.size).random()
//        Qtv.text = enableQue[num].que
//        var ans = enableQue[num].ans
//        ansButtons[0].text = ans[0]
//        ansButtons[1].text = ans[1]
//        ansButtons[2].text = ans[2]
//        ansButtons[3].text = ans[3]
//        ansButtons[0].setOnClickListener {
//            enableQue[num].checkAnswer(0)
//            for (buttons in ansButtons)
//                buttons.isEnabled = false
//        }
//        ansButtons[1].setOnClickListener {
//            enableQue[num].checkAnswer(1)
//            for (buttons in ansButtons)
//                buttons.isEnabled = false
//        }
//        ansButtons[2].setOnClickListener {
//            enableQue[num].checkAnswer(2)
//            for (buttons in ansButtons)
//                buttons.isEnabled = false
//        }
//        ansButtons[3].setOnClickListener {
//            enableQue[num].checkAnswer(3)
//            for (buttons in ansButtons)
//                buttons.isEnabled = false
//        }
//        enableQue.removeAt(num)
//
//        GlobalScope.launch(Dispatchers.IO) {
//            try {
//                writer.println(enableQue[num].correct)
//                writer.flush()
//                var str = reader.readLine()
//                if (str == "$player_id")
//                    ret = 1
//                else if (str == "333")
//                    ret = 0
//                else
//                    ret -1
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//        setContentView(R.layout.activity_quiz)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("Debug", "Вошли в функцию обработки результата")
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val ret = data?.getStringExtra("ret")?.toIntOrNull() // String обратно в Int
            if (ret != null) {
                extracted(ret)
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

    private fun getButton(id: Pair<Int, Int>): CustomButton {
        if (id.first == 0)
            return buttons[0]
        if (id.first == MAX_ROW)
            return buttons.last()
        return buttons[1 + (id.first - 1) * (MAX_COLUMN + 1) + id.second]
    }

    private fun disableButtons(ids: MutableList<Pair<Int, Int>>) {
        var flag = true
        for (buttonsId in ids) {
            for (neighborsId in getButton(buttonsId).getNeighbors()) {
                if (getButton(neighborsId).status == 1) {
                    flag = false
                    break
                }
                if (!flag) {
                    flag = true
                } else {
                    getButton(buttonsId).bt.isEnabled = false
                }
            }
        }
    }

    private fun disableAllButtons() {
        enButtons.clear()
        for (button in buttons) {
            if (button.bt.isEnabled == true)
                enButtons.add(button)
            button.bt.isEnabled = false
        }
    }

    private fun enableAllButtons() {
        for (button in enButtons) {
            if (button.status != 1)
                button.bt.isEnabled = true
        }
    }

    private fun enableButtons(ids: MutableList<Pair<Int, Int>>) {
        for (buttonsId in ids) {
            if (getButton(buttonsId).status != 1) {
                getButton(buttonsId).bt.isEnabled = true
                enButtons.add(getButton(buttonsId))
            } else if (enButtons.contains(getButton(buttonsId)))
                enButtons.remove(getButton(buttonsId))
        }
    }

    private fun onWin(id: Pair<Int, Int>) {
        Log.d("Debug", "Игрок официально выиграл один вопрос!")

        getButton(id).status = 1

        if (getButton(id) == enemyCastle) {
            // Toast.makeText(this@GameNull, "Игра окончена. Игрок ${currentPlayer} победил!", Toast.LENGTH_SHORT).show()
            gameOver = true
            showDialog()
        } else {
            enableButtons(getButton(id).getNeighbors())
            // Смена текущего игрока
            currentPlayer = if (currentPlayer == 1) 2 else 1
            tv.text = "Сейчас ход игрока " +
                    if (currentPlayer == player_id + 1) playerEmail?.removeSuffix("@whatever.ru")
                    else enemyEmail?.removeSuffix("@whatever.ru")


            disableAllButtons()

            // Через сервер ждём ответ другого игрока
            waitForOtherPlayer()
        }
    }

    private fun onLose(id: Pair<Int, Int>) {
        var flag: Boolean = true
        getButton(id).status = -1

        if (getButton(id).status == 1) {
            disableButtons(getButton(id).getNeighbors())
//            for (button in getButton(id).getNeighbors()) {
//                for (button2 in getButton(button).getNeighbors()) {
//                    if (getButton(button2).status == 1) {
//                        flag = false
//                        break
//                    }
//                }
//                if (!flag)
//                    flag = true
//                else
//                    disableButtons(mutableListOf(button))
//            }
            for (button in getButton(id).getNeighbors()) {
                // TODO: gui @a1sarpi
            }
        }

        if (getButton(id) == myCastle) {
            // Toast.makeText(this@GameNull, "Игра окончена. Игрок ${currentPlayer} победил!", Toast.LENGTH_SHORT).show()
            Log.d("Debug", "Вражеский игрок победил!")
            gameOver = true
            showDialog()
        }
    }

    private fun onButtonClick(id: Pair<Int, Int>) {
        currentBattleId = id
        Log.d("Debug", "Данный игрок нажал на кнопку с id = ${currentBattleId}")


        GlobalScope.launch(Dispatchers.IO) {
            try {
                writer.println("${currentBattleId.first}")
                writer.flush()
                writer.println("${currentBattleId.second}")
                writer.flush()

                if (getButton(currentBattleId).status == -1) {
                    writer.println("duel")
                    writer.flush()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (getButton(currentBattleId).status == -1) {
            toDuelQuestion()
        } else
            toQuestion()
    }

    private fun extracted(ret: Int?) {
        Log.d("Debug", "ret = ${ret}")
        if (ret == 1)
            onWin(currentBattleId)
        if (ret == -1)
            onLose(currentBattleId)
    }

    private fun toQuestion() {
        SocketHelper.clientSocket = clientSocket
//        val someActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val data: Intent? = result.data
//                ret = data?.getIntExtra("ret", -1)
//            }
//        }
        val intent = Intent(this, QuizQuestion::class.java)
        intent.putExtra("flag", false) // !!! нужно ли?
        Log.d("Debug", "Пытаемся войти в класс QuizQuestion")
        startActivityForResult(intent, 1)

//        while (flag) { }
//        flag = true
        //someActivityResultLauncher.launch(intent)
    }


    private fun resetGame() {
        Log.d("Debug", "Вошли в функцию resetGame()")
        enButtons.clear()
        // TODO: ожидание сервера @a1sarpi
        for (bt in buttons)
            bt.bt.isEnabled = false
        gameOver = false

        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (clientSocket.isConnected) {
                    // Получаем идентификатор игрока от сервера
                    player_id = reader.readLine().toInt()
                    if (player_id == 1) {
                        myCastle = buttons.last()
                        enemyCastle = buttons[0]
                    } else {
                        myCastle = buttons[0]
                        enemyCastle = buttons.last()
                    }
                    myCastle.status = 1
                    enButtons.clear()
                    enemyEmail = reader.readLine()
                    Log.d("Debug.", "Id ${player_id}, Почта ${playerEmail}")

                    // Обновляем UI в основном потоке
                    launch(Dispatchers.Main) {
                        // TODO: конец ожидания сервера (фронтенд) @a1sarpi
                        currentPlayer = 1
                        tv = findViewById(R.id.currentPlayerTV)


                        if (player_id != 0) {
                            // Через сервер ждём ответ другого игрока
                            tv.text = "Сейчас ход игрока ${enemyEmail?.removeSuffix("@whatever.ru")}"
                            for (bt in myCastle.getNeighbors()) {
                                if (getButton(bt).status != 1)
                                    enButtons.add(getButton(bt))
                            }
                            waitForOtherPlayer()
                        } else {
                            tv.text = "Сейчас ход игрока ${playerEmail}"
                            enableButtons(myCastle.getNeighbors())
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

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Начать новую игру?")
            .setCancelable(false)
            .setPositiveButton("Да") { dialog, id ->
                GlobalScope.launch(Dispatchers.IO) {
                    //sendData("${player_id}")
                    writer.println("1234567")
                    writer.flush()
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

    suspend fun sendData(message: String) {
        try {
            writer.println(message)
            writer.flush()
        } catch (e: IOException) {
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

class Question(val que: String, val ans: Array<String>, val correctAnswerIndex: Int) {
    var correct: Boolean = false
        private set

    fun checkAnswer(answerIndex: Int): Boolean {
        val isCorrect = answerIndex == correctAnswerIndex
        correct = isCorrect
        return isCorrect
    }
}

object SocketHelper {
    var clientSocket: Socket? = null
}

