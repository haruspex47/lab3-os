package com.example.server

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class Server {
    private val serverSocket: ServerSocket = ServerSocket(1234)
    private var guessedNumber: Int = 0
    private var all_players: Array<MutableList<Player>> = arrayOf(
        mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())
    private var id_count: Int = 0

    fun start() {
        println("Сервер запущен. Ожидание подключений...")
        val address = InetAddress.getLocalHost().hostAddress
        println("Server is running on $address:${serverSocket.localPort}")
        while (true) {
            val clientSocket = serverSocket.accept()
            val player = Player(clientSocket)
            all_players[player.gm.id].add(player)
            check_queue(player.gm.id, true)
        }
    }

    private fun check_queue(game_id: Int, flag: Boolean): Boolean {
        println("Запущена проверка очереди. Размер очереди (должен быть 2): ${all_players[game_id].size}")
        if (all_players[game_id].size == 2) {
            val (p1, p2) = Pair(all_players[game_id][0], all_players[game_id][1])
            all_players[game_id].clear()
            println("Игрок ${p1.id} с почтой ${p2.email} подключился")
            p1.sendPlayerId(p1.id, p2.email)
            println("Игрок ${p2.id} с почтой ${p1.email} подключился")
            p2.sendPlayerId(p2.id, p1.email)
            p2.enemy = p1
            p1.enemy = p2
            guessedNumber = (1..9).random() // TODO: исправить
            println("Загадано число ${guessedNumber}")

            Thread(p2).start()
            Thread(p1).start()

            println("Два врага найдены")
            println("Игра началась")
            return true
        } else return false
    }


    private inner class Player(val clientSocket: Socket) : Runnable {
        val id: Int = id_count // TODO @a1sarpi
        var gm: Game
        private val reader: BufferedReader
        private val writer: PrintWriter
        val email: String
        var win: Boolean = false
        lateinit var enemy: Player

        init {
            reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            writer = PrintWriter(clientSocket.getOutputStream(), true)

            gm = Game.fromInt(reader.readLine().toInt())
            email = reader.readLine()

            id_count++
        }

        fun sendPlayerId(id: Int, email: String) {
            writer.println(id.toString())
            writer.println(email)
        }

        fun sendGuessedNumber(number: Int) {
            writer.println(number.toString())
        }

        fun sendOpponentNumber(number: Int) {
            writer.println(number.toString())
        }

        override fun run() {
            try {
                when(gm) {
                    Game.RANNUM -> rannum()
                    else -> {println("Ошибка! Игры ${gm} не существует")}
                }
            } catch (e: Exception) {
                e.printStackTrace()
                reader.close()
                writer.close()
                clientSocket.close()
            }
        }

        fun rannum() {
            println("Игрок ${id} вошёл в игру")
            win = false
            enemy.win = false
            sendGuessedNumber(guessedNumber)
            while (true) {
                println("Сервер ждёт выбранное число")
                val line = reader.readLine()
                if (enemy.win) {
                    onWin(line.toInt())
                    break
                }
                if (line == null) {
                    println("fuckup")
                    val number = 0
                    println("Игрок $id выбрал число $number")
                    enemy.sendOpponentNumber(number)
                    reader.close()
                    writer.close()
                    clientSocket.close()
                    enemy.reader.close()
                    enemy.writer.close()
                    enemy.clientSocket.close()
                    break
                }
                val number = line.toInt()
                println("Игрок $id выбрал число $number")
                enemy.sendOpponentNumber(number)
                if (number == guessedNumber) {
                    println("Игрок $id угадал число!")
                    win = true
                    val ans = reader.readLine().toInt()
                    onWin(ans)
                    break
                }
            }
        }

        private fun onWin(ans: Int) {
            println("Игрок на вопрос о начале новой игры ответил ${ans}")
            if (ans == id) {
                println("Размер очереди на данный момент равен ${all_players[gm.id].size}")
                all_players[gm.id].add(this)
                // TODO: возможность отключиться или кнопку выхода в меню @a1sarpi
                Thread {
                    // Код, выполняемый в ином потоке
                    check_queue(gm.id, false)
                }.start()
            } else {
                reader.close()
                writer.close()
                clientSocket.close()
            }
        }
    }
}

enum class Game(val id: Int) {
    RANNUM(0), XO(1), KNB(2), QUIZ(3);
    companion object {
        fun fromInt(id: Int) = Game.values().first { it.id == id }
    }
}



fun main() {
    val server = Server()
    server.start()
}
