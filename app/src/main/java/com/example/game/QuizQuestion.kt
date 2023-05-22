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
    private var bool_ret: Boolean = false
    private var extr: Bundle? = null
    private lateinit var Qtv: TextView
    private var ansButtons: MutableList<Button> = mutableListOf()

    val clientSocket = SocketHelper.clientSocket
    private var writer = PrintWriter(clientSocket?.getOutputStream())
    private var reader = BufferedReader(InputStreamReader(clientSocket?.getInputStream()))

    var num: Int = 0
    var ret: Int = -111

    var serverRandom: Int = -1

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


        extr = intent.extras
        var Que = enableDuelQue[0]
        if (extr != null) {
            if (!extr!!.getBoolean("flag", false)) {
                num = (0 until enableQue.size).random()
                Que = enableQue[num]
                Log.d("Debug", Que.que)
                Qtv.text = Que.que
                val ans = Que.ans
                ansButtons[0].text = ans[0]
                ansButtons[1].text = ans[1]
                ansButtons[2].text = ans[2]
                ansButtons[3].text = ans[3]
                ansButtons[0].setOnClickListener {
                    Que.checkAnswer(0)
                    for (buttons in ansButtons)
                        buttons.isEnabled = false
                    bool_ret = Que.correct
                    end()
                }
                ansButtons[1].setOnClickListener {
                    Que.checkAnswer(1)
                    for (buttons in ansButtons)
                        buttons.isEnabled = false
                    bool_ret = Que.correct
                    end()
                }
                ansButtons[2].setOnClickListener {
                    Que.checkAnswer(2)
                    for (buttons in ansButtons)
                        buttons.isEnabled = false
                    bool_ret = Que.correct
                    end()
                }
                ansButtons[3].setOnClickListener {
                    Que.checkAnswer(3)
                    for (buttons in ansButtons)
                        buttons.isEnabled = false
                    bool_ret = Que.correct
                    end()
                }

            } else {
                GlobalScope.launch(Dispatchers.IO) {
                    serverRandom = reader.readLine().toInt()
                    Log.d("Debug", "Прислано serverRandom: $serverRandom, ${enableDuelQue[serverRandom].que}")
                    Que = enableDuelQue[serverRandom]
                    launch(Dispatchers.Main) {
                        Log.d("Debug", Que.que)
                        Qtv.text = Que.que
                        val ans = Que.ans
                        ansButtons[0].text = ans[0]
                        ansButtons[1].text = ans[1]
                        ansButtons[2].text = ans[2]
                        ansButtons[3].text = ans[3]
                        ansButtons[0].setOnClickListener {
                            Que.checkAnswer(0)
                            for (buttons in ansButtons)
                                buttons.isEnabled = false
                            bool_ret = Que.correct
                            end()
                        }
                        ansButtons[1].setOnClickListener {
                            Que.checkAnswer(1)
                            for (buttons in ansButtons)
                                buttons.isEnabled = false
                            bool_ret = Que.correct
                            end()
                        }
                        ansButtons[2].setOnClickListener {
                            Que.checkAnswer(2)
                            for (buttons in ansButtons)
                                buttons.isEnabled = false
                            bool_ret = Que.correct
                            end()
                        }
                        ansButtons[3].setOnClickListener {
                            Que.checkAnswer(3)
                            for (buttons in ansButtons)
                                buttons.isEnabled = false
                            bool_ret = Que.correct
                            end()
                        }
                    }
                }
            }
        }
    }

    private fun end() {
        if (extr!!.getBoolean("flag", false)) {
            Log.d("Debug", "server rndm: $serverRandom, ques: ${enableDuelQue[serverRandom].que}")
        }

        Log.d("Debug", "В классе QuizQUestion запущена функция end(), при этом правильность ответа: ${bool_ret}")
        if (bool_ret)
            ret = 1
        else
            ret = 0
        GlobalScope.launch(Dispatchers.IO) {
            try {
                writer.println("${bool_ret}")
                writer.flush()

                if (extr != null) {
                    if (extr!!.getBoolean("flag", false)) {
                        var str = reader.readLine()
                        if (str == "true") {
                            if (!bool_ret) {
                                Log.d("Debug", "кто-то проиграл вопрос в дуэли")
                                writer.println("false")
                                writer.flush()
                                ret = -1
                            } else {
                                ret = 0
                                writer.println("false")
                                writer.flush()
                                Log.d("Debug", "здесь ничья")
                            }
                        }
                        else if (str == "false") {
                            if (bool_ret) {
                                Log.d("Debug", "кто-то выиграл вопрос в дуэли")
                                writer.println("true")
                                writer.flush()
                                ret = 1
                            } else {
                                ret = 0
                                writer.println("false")
                                writer.flush()
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
                        enableQue.removeAt(num)
                        Log.d("Debug", "Пройден обычный вопрос")
                    }
                }

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
