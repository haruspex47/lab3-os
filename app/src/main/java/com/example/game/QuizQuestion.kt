package com.example.game

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter

class QuizQuestion : ComponentActivity() {
    private lateinit var Qtv: TextView
    private var ansButtons: MutableList<Button> = mutableListOf()

    val clientSocket = SocketHelper.clientSocket
    private var writer = PrintWriter(clientSocket?.getOutputStream())
    private var reader = BufferedReader(InputStreamReader(clientSocket?.getInputStream()))

    var num: Int = 0
    var ret: Int = -111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_question)

        Log.d("Debug", "Запущен класс QuizQuestion")
//        GlobalScope.launch(Dispatchers.IO) {
//            try {
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }

//        val b = intent.extras
//        val id = b!!.getInt("id")

        Qtv = findViewById(R.id.QuestionTV)
        ansButtons.add(findViewById(R.id.ans1))
        ansButtons.add(findViewById(R.id.ans2))
        ansButtons.add(findViewById(R.id.ans3))
        ansButtons.add(findViewById(R.id.ans4))

        num = (0 until enableQue.size).random()
        Qtv.text = enableQue[num].que
        val ans = enableQue[num].ans
        ansButtons[0].text = ans[0]
        ansButtons[1].text = ans[1]
        ansButtons[2].text = ans[2]
        ansButtons[3].text = ans[3]
        ansButtons[0].setOnClickListener {
            enableQue[num].checkAnswer(0)
            for (buttons in ansButtons)
                buttons.isEnabled = false
            end()
        }
        ansButtons[1].setOnClickListener {
            enableQue[num].checkAnswer(1)
            for (buttons in ansButtons)
                buttons.isEnabled = false
            end()
        }
        ansButtons[2].setOnClickListener {
            enableQue[num].checkAnswer(2)
            for (buttons in ansButtons)
                buttons.isEnabled = false
            end()
        }
        ansButtons[3].setOnClickListener {
            enableQue[num].checkAnswer(3)
            for (buttons in ansButtons)
                buttons.isEnabled = false
            end()
        }

    }

    private fun end() {
        Log.d("Debug", "В классе QuizQUestion запущена функция end(), при этом правильность ответа: ${enableQue[num].correct}")
        var bool_ret: Boolean = enableQue[num].correct
        if (bool_ret)
            ret = 1
        else
            ret = 0
        GlobalScope.launch(Dispatchers.IO) {
            try {
                writer.println("${enableQue[num].correct}")
                writer.flush()

                val b = intent.extras

                if (b != null) {
                    if (b.getBoolean("flag", false)) {
                        var str = reader.readLine()
                        if (str == "true") {
                            if (enableQue[num].correct == false) {
                                Log.d("Debug", "кто-то проиграл вопрос в дуэли")
                                ret = -1
                            } else {
                                ret = 0
                                Log.d("Debug", "здесь ничья")
                            }
                        }
                        else if (str == "false") {
                            if (enableQue[num].correct == true) {
                                Log.d("Debug", "кто-то выиграл вопрос в дуэли")
                                ret = 1
                            } else {
                                ret = 0
                                Log.d("Debug", "здесь ничья")
                            }
                        }
//                        else if (str == "-1") { // сервер решил, что кто-то проиграл
//                            ret = -1
//                            if (enableQue[num].correct)
//                                ret = 1
//                        }
//                        if ((ret == -1) and (enableQue[num].correct))
//                            ret = 1
//                        if ((ret == 1) and !enableQue[num].correct)
//                            ret = -1
                        Log.d("Debug", "Пройден особый вопрос")
                    } else {
                        Log.d("Debug", "Пройден обычный вопрос")
                    }
                }

                enableQue.removeAt(num)

                val resultCode = Activity.RESULT_OK
                val intent = Intent()
                intent.putExtra("ret", ret.toString()) // Int в String
                setResult(resultCode, intent)
                Log.d("Debug", "Выход из функции end(), при этом ret = $ret")

                flag = false
                finish()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
