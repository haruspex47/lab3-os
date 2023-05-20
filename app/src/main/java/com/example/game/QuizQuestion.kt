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
    private var ansButtons: Array<Button> = arrayOf()

    val clientSocket = SocketHelper.clientSocket
    private var writer = PrintWriter(clientSocket?.getOutputStream())
    private var reader = BufferedReader(InputStreamReader(clientSocket?.getInputStream()))

    var num: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Debug", "Вход в обычный вопрос")
        setContentView(R.layout.activity_quiz_question)
        Log.d("Debug", "Вход в обычный вопрос-2")

//        GlobalScope.launch(Dispatchers.IO) {
//            try {
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }

//        val b = intent.extras
//        val id = b!!.getInt("id")

        Qtv = findViewById<TextView>(R.id.QuestionTV)
        ansButtons[0] = findViewById<Button>(R.id.ans1)
        ansButtons[1] = findViewById<Button>(R.id.ans2)
        ansButtons[2] = findViewById<Button>(R.id.ans3)
        ansButtons[3] = findViewById<Button>(R.id.ans4)

        Log.d("Debug", "Вход в обычный вопрос-2.5")

        num = (0..enableQue.size).random()
        Qtv.text = enableQue[num].que
        val ans = enableQue[num].ans
        ansButtons[0].text = ans[0]
        ansButtons[1].text = ans[1]
        ansButtons[2].text = ans[2]
        ansButtons[3].text = ans[3]
        Log.d("Debug", "Вход в обычный вопрос-3")
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
        Log.d("Debug", "Выход из обычного вопроса")
        GlobalScope.launch(Dispatchers.IO) {
            try {
                writer.println("${enableQue[num].correct}")
                writer.flush()
                ret = enableQue[num].correct as Int

                val b = intent.extras

                if (b != null) {
                    if (b.getBoolean("flag", false)) {
                        var str = reader.readLine()
                        if (str == "1")
                            ret = 1
                        else if (str == "0")
                            ret = 0
                        else if (str == "-1")
                            ret = -1
                        if ((ret == -1) and (enableQue[num].correct))
                            ret = 1
                        if ((ret == 1) and !enableQue[num].correct)
                            ret = -1
                    } else {
                        Log.d("Debug", "Пройден обычный вопрос")
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        enableQue.removeAt(num)

        //val resultCode = Activity.RESULT_OK // Используйте код результата по вашему усмотрению
        //val intent = Intent()
        //intent.putExtra("ret", ret)
        //setResult(resultCode, intent)
        finish()
    }
}
