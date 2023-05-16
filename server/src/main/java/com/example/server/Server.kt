package com.example.server;

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class Server {
    private val serverSocket: ServerSocket
    private val players: MutableList<Player>
    private var currentPlayer: Player? = null
    private var guessedNumber: Int = 0

    init {
        serverSocket = ServerSocket(1234) // Укажите нужный порт для прослушивания
        players = mutableListOf()
    }

    fun start() {
        println("Сервер запущен. Ожидание подключений...")
        val address = InetAddress.getLocalHost().hostAddress
        println("Server is running on $address:${serverSocket.localPort}")
        while (players.size < 2) {
            val clientSocket = serverSocket.accept()
            val player = Player(clientSocket)
            players.add(player)
            Thread(player).start()
            println("Игрок ${player.id} подключился")
        }
        println("Игра началась")

        currentPlayer = players[0]
        guessedNumber = (1..9).random()

        for (player in players) {
            player.sendPlayerId(player.id)
            player.sendGuessedNumber(guessedNumber)
        }
        println("Загадано число ${guessedNumber}")
    }

    private inner class Player(val clientSocket: Socket) : Runnable {
        val id: Int = players.size + 1
        private val reader: BufferedReader
        private val writer: PrintWriter

        init {
            reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            writer = PrintWriter(clientSocket.getOutputStream(), true)
        }

        fun sendPlayerId(id: Int) {
            writer.println(id.toString())
        }

        fun sendGuessedNumber(number: Int) {
            writer.println(number.toString())
        }

        fun sendOpponentNumber(number: Int) {
            writer.println(number.toString())
        }

        override fun run() {
            try {
                while (true) {
                    println("Сервер ждёт загаданного числа")
                    val number = reader.readLine().toInt()
                    println("Игрок $id выбрал число $number")

                    for (player in players) {
                        if (player != this) {
                            player.sendOpponentNumber(number)
                        }
                    }

                    if (number == guessedNumber) {
                        println("Игрок $id угадал число!")
                        for (player in players) {
                            player.sendOpponentNumber(number)
                        }
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                reader.close()
                writer.close()
                clientSocket.close()
            }
        }
    }
}

fun main() {
    val server = Server()
    server.start()
}
