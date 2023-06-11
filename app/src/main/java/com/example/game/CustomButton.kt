package com.example.game

import android.content.Context
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class CustomButton {
    var bt: Button?

    private var row: Int = -1
    private var column: Int = -1
    private var neighbors: MutableList<Pair<Int, Int>> = mutableListOf()

    var status: Int // принимает значения -1, 0 и 1 в зависимости от принадлежности точки

    constructor() { status = 0; bt = null}
    constructor(_bt: Button, ids: Pair<Int, Int>, _status: Int = 0)  {
        bt = _bt

        row = ids.first
        column = ids.second
        status = _status

        updateNeighbors()

        init()
    }

    private fun updateNeighbors() {
        for (i: Int in -1..1)
            for (j: Int in -1 .. 1)
                if (check(row + i, column + j))
                    neighbors.add(Pair<Int, Int>(row + i, column + j))
    }

    private fun init() {
        // TODO?: настройка внешнего вида кнопки
    }

    private fun check(row: Int, column: Int): Boolean {
        return (row >= 1) and (column >= 0) and (row <= MAX_ROW-1) and (column <= MAX_COLUMN) or
                (((row == MAX_ROW) or (row == 0)) and (column == 1))
    }

    fun getRow(): Int {
        return row
    }

    fun getColumn(): Int {
        return column
    }

    fun getNeighbors(): MutableList<Pair<Int, Int>> {
        return neighbors
    }

    fun setId(id: Pair<Int, Int>) {
        row = id.first; column = id.second
        updateNeighbors()
    }
}