package com.example.fitnesstracker.data

import com.example.fitnesstracker.viewModel.StepsCounterViewModel

class FitnessRepo(stepViewModel: StepsCounterViewModel) {
    private val model=stepViewModel
    private val steps=model.steps
    val distance=model.distance
    init{
        println("FitnessRepo Created")
    }
    fun reset(){
        model.resetDistance()
    }
    fun stop(){
        model.stop()
    }
}