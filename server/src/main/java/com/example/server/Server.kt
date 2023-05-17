package com.example.server;

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class Server {
    private val serverSocket: ServerSocket
    private var guessedNumber: Int = 0
    private var all_players: Array<MutableList<Player>> = arrayOf(
        mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())
    private var id_count: Int = 0

    init {
        serverSocket = ServerSocket(1234)
    }

    fun start() {
        println("Сервер запущен. Ожидание подключений...")
        val address = InetAddress.getLocalHost().hostAddress
        println("Server is running on $address:${serverSocket.localPort}")
        while (true) {
            val clientSocket = serverSocket.accept()
            // TODO: пусть игрок ждёт
            val player = Player(clientSocket)
            all_players[player.gm.id].add(player)
            if (all_players[player.gm.id].size == 2) {
                var (p1, p2) = Pair(all_players[player.gm.id][0], all_players[player.gm.id][1])
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
            }
        }
        println("Игра началась")
    }



    private inner class Player(val clientSocket: Socket) : Runnable {
        val id: Int = id_count // TODO @a1sarpi
        var gm: Game
        private val reader: BufferedReader
        private val writer: PrintWriter
        val email: String

        var enemy: Player ? = null

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
            } finally {
                reader.close()
                writer.close()
                clientSocket.close()
            }
        }

        fun rannum() {
            // currentPlayer = players[0]
            sendGuessedNumber(guessedNumber)
            while (true) {
                println("Сервер ждёт выбранное число")
                val line = reader.readLine()
                if (line == null) {
                    println("fuck")
                    val number = 0
                    println("Игрок $id выбрал число $number")
                    enemy?.sendOpponentNumber(number)
                    break
                }
                val number = line.toInt()
                println("Игрок $id выбрал число $number")
                enemy?.sendOpponentNumber(number)
                if (number == guessedNumber) {
                    println("Игрок $id угадал число!")
                    var ans = reader.readLine().toInt()
                    ans *= reader.readLine().toInt()
                    writer.println(ans)
                    writer.println(ans)
                    break
                }
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
