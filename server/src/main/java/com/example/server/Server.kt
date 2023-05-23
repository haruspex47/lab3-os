package com.example.server

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class Server {
    private var round: Int = 3
    private var _indexes: Array<Int> = arrayOf()
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
            println("Произведено новое подключение к серверу")
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
            _indexes = arrayOf(0, 1, 2)
            _indexes.shuffle()


            Thread(p2).start()
            Thread(p1).start()

            println("Два врага найдены")
            println("Игра началась")
            return true
        } else return false
    }


    private inner class Player(val clientSocket: Socket) : Runnable {
        private var wins: Int = 0

        private val indexes: MutableList<Int> = mutableListOf(0, 1, 2)
        private var countDuelQue: Int = 3
        val id: Int = id_count // TODO @a1sarpi
        var gm: Game
        private val reader: BufferedReader
        private val writer: PrintWriter
        val email: String
        var win: Boolean = false
        lateinit var enemy: Player

        private var correct = "-111"

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
                    Game.QUIZ -> quiz()
                    Game.XO -> ttt()
                    Game.KNB -> knb()
                    else -> {println("Ошибка! Игры ${gm} не существует")}
                }
            } catch (e: Exception) {
                e.printStackTrace()
                reader.close()
                writer.close()
                clientSocket.close()
            }
        }

        private fun knb() {
            println("Игрок ${id} вошёл в игру ${gm}")
            win = false
            enemy.win = false
            while (true) {
                println("Сервер ждёт данные")
                val line = reader.readLine()
                if (enemy.win) {
                    onWin(line.toInt())
                    break
                }
                if (line == null) {
                    println("fuckup")
                    val number = -111 // !!! TODO
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
                enemy.writer.println(line.toInt())
                enemy.writer.flush()
                var correct = reader.readLine()
                println("Игрок $id прислал данные $correct")
                if (correct == "win") {
                    println("Игрок $id выиграл раунд!")
                    wins++
                    round--
                    if (round == 0) {
                        if (wins > 1) {
                            println("Игрок $id выиграл игру!")
                            writer.println("win")
                            writer.flush()
                            win = true
                            val ans = reader.readLine().toInt()
                            onWin(ans)
                            break
                        } else if (enemy.wins > 1) {
                            println("Игрок $id проиграл игру!")
                            enemy.win = true
                            writer.println("lose")
                            writer.flush()
                            val ans = reader.readLine().toInt()
                            onWin(ans)
                            break
                        } else {
                            println("Ничья!")
                            win = true
                            enemy.win = true
                            val ans = reader.readLine().toInt()
                            onWin(ans)
                            break
                        }
                    }
                }
                if (correct == "dontwin") {
                    println("Ничья в раунде!")
                }
            }
        }

        private fun ttt() {
            println("Игрок ${id} вошёл в игру ${gm}")
            win = false
            enemy.win = false
            while (true) {
                println("Сервер ждёт данные")
                val line = reader.readLine()
                if (enemy.win) {
                    onWin(line.toInt())
                    break
                }
                if (line == null) {
                    println("fuckup")
                    val number = -111 // !!! TODO
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
                val number = mutableListOf<Int>(line.toInt(), reader.readLine().toInt())
                println("Игрок $id прислал данные $number")
                enemy.writer.println("${number[0]}") // врагу
                enemy.writer.flush()
                enemy.writer.println("${number[1]}") // врагу
                enemy.writer.flush()
                var correct = reader.readLine()
                println("Игрок $id прислал данные $correct")
                enemy.writer.println("${correct}") // врагу
                enemy.writer.flush()
                if (correct == "win") {
                    println("Игрок $id выиграл!")
                    win = true
                    val ans = reader.readLine().toInt()
                    onWin(ans)
                    break
                }
                if (correct == "nobody") {
                    println("Ничья!")
                    win = true
                    enemy.win = true
                    val ans = reader.readLine().toInt()
                    onWin(ans)
                    break
                }
            }
        }

        private fun quiz() {
            println("Игрок ${id} вошёл в игру ${gm}")
            win = false
            enemy.win = false
            while (true) {
                println("Сервер ждёт данные")
                var line = reader.readLine()
                if (line == "duel") {
                    println("Второй игрок ($id) согласился на дуэль")
                    onDuel()
                    line = reader.readLine()
                    line = reader.readLine()
                }
                if (enemy.win) {
                    onWin(line.toInt())
                    break
                }
                if (line == null) {
                    println("fuckup")
                    val number = -111 // !!! TODO
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
                val number = mutableListOf<Int>(line.toInt(), reader.readLine().toInt())
                println("Игрок $id прислал данные $number")
                enemy.writer.println("${number[0]}") // врагу
                enemy.writer.flush()
                enemy.writer.println("${number[1]}") // врагу
                enemy.writer.flush()
                correct = reader.readLine()
                println("Игрок $id прислал данные $correct")
                enemy.writer.println("${correct}") // врагу
                enemy.writer.flush()
                if (correct == "duel") {// !!!
//                    onDuel()
                    writer.println(_indexes[countDuelQue - 1].toString())
                    writer.flush()
                    countDuelQue--
                    correct = reader.readLine()
                    println("Началась дуэль, при этом ответ игрока $id $correct ($number)")
                    enemy.writer.println(correct)
                    enemy.writer.flush()
                    correct = reader.readLine()
                }
                if ((correct == "true") and
                        ((number[0].toInt() == 0) or (number[0].toInt() == 5))) { // !!! MAX_ROW == 5
                    println("Игрок $id выиграл!")
                    win = true
                    val ans = reader.readLine().toInt()
                    onWin(ans)
                    break
                }
            }
        }

        private fun onDuel() : String {
            writer.println(_indexes[countDuelQue - 1].toString())
            writer.flush()
            countDuelQue--
            var correct = reader.readLine()
            println("Началась дуэль, при этом ответ игрока $id $correct")
            enemy.writer.println(correct)
            enemy.writer.flush()

            return correct
        }

        fun rannum() {
            println("Игрок ${id} вошёл в игру ${gm}")
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
            println("Игрок {$id} на вопрос о начале новой игры ответил ${ans}")
            if (ans != -1) {
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
//            if (ans == id) {
//                println("Размер очереди на данный момент равен ${all_players[gm.id].size}")
//                all_players[gm.id].add(this)
//                // TODO: возможность отключиться или кнопку выхода в меню @a1sarpi
//                Thread {
//                    // Код, выполняемый в ином потоке
//                    check_queue(gm.id, false)
//                }.start()
//            } else {
//                reader.close()
//                writer.close()
//                clientSocket.close()
//            }
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
